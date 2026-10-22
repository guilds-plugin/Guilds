package me.glaremasters.guilds.economy;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.taskchain.TaskChain;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.commands.bank.CommandBank;
import me.glaremasters.guilds.commands.management.CommandCreate;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.economy.FakeEconomy.Behaviour;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.messages.Messages;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers the money that moves between a player and a guild bank.
 *
 * <p>Vault's economy methods answer with a response that says whether the money moved. Guilds used
 * to throw that answer away and commit the guild side of the transaction anyway, so a provider
 * that refused the transaction still changed the bank: a refused deposit credited the bank with
 * money that was never taken, a refused withdrawal debited the bank for money the player never
 * received, and a refused guild creation handed out a guild for free.
 *
 * <p>Every test here drives the real command with a fake provider, so the ordering inside the
 * command is what is being checked, not a copy of it.
 */
class EconomyTransactionTest {

    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final double STARTING_BALANCE = 1000.0;
    private static final double CREATION_COST = 100.0;

    private Player player;
    private CommandIssuer issuer;
    private FakeEconomy economy;
    private Guild guild;
    private AcfCommandContext context;

    @BeforeEach
    void setUp() {
        player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(PLAYER_ID);
        Mockito.when(player.getName()).thenReturn("Tester");

        issuer = Mockito.mock(CommandIssuer.class);
        final CommandManager manager = Mockito.mock(CommandManager.class);
        Mockito.when(manager.getCommandIssuer(player)).thenReturn(issuer);
        context = new AcfCommandContext(manager, issuer);

        economy = new FakeEconomy(STARTING_BALANCE);
        guild = guildWithBalance(0.0);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("a deposit that goes through moves the money into the bank")
    void successfulDeposit() {
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.deposit(player, guild, 250.0);
        }

        assertEquals(STARTING_BALANCE - 250.0, economy.balance(), 0.001,
                "the player should have paid 250");
        assertEquals(250.0, guild.getBalance(), 0.001,
                "the bank should have been credited with 250");
        Mockito.verify(issuer, Mockito.never()).sendInfo(Mockito.eq(Messages.ERROR__ECONOMY_TRANSACTION_FAILED),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @DisplayName("a withdrawal that goes through moves the money out of the bank")
    void successfulWithdrawal() {
        guild.setBalance(500.0);
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.withdraw(player, guild, 150.0);
        }

        assertEquals(STARTING_BALANCE + 150.0, economy.balance(), 0.001,
                "the player should have received 150");
        assertEquals(350.0, guild.getBalance(), 0.001,
                "the bank should have been debited with 150");
        Mockito.verify(issuer, Mockito.never()).sendInfo(Mockito.eq(Messages.ERROR__ECONOMY_TRANSACTION_FAILED),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @DisplayName("a refused deposit leaves the player and the bank where they were")
    void refusedDeposit() {
        economy.withdrawing(Behaviour.FAILURE);
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.deposit(player, guild, 250.0);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a refused withdrawal must not take money from the player");
        assertEquals(0.0, guild.getBalance(), 0.001,
                "a refused withdrawal must not credit the bank, or the money exists twice");
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "Insufficient funds");
    }

    @Test
    @DisplayName("a refused withdrawal leaves the player and the bank where they were")
    void refusedWithdrawal() {
        guild.setBalance(500.0);
        economy.depositing(Behaviour.FAILURE);
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.withdraw(player, guild, 150.0);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a refused deposit must not pay the player");
        assertEquals(500.0, guild.getBalance(), 0.001,
                "a refused deposit must not debit the bank, or the money is gone");
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "Insufficient funds");
    }

    @Test
    @DisplayName("a deposit the provider throws on changes nothing")
    void throwingProviderOnDeposit() {
        economy.withdrawing(Behaviour.THROW);
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.deposit(player, guild, 250.0);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a provider that blew up must not take money from the player");
        assertEquals(0.0, guild.getBalance(), 0.001,
                "a provider that blew up must not credit the bank");
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "the economy provider blew up");
    }

    @Test
    @DisplayName("a withdrawal the provider throws on changes nothing")
    void throwingProviderOnWithdrawal() {
        guild.setBalance(500.0);
        economy.depositing(Behaviour.THROW);
        final CommandBank command = new CommandBank();
        command.setEconomy(economy);

        try (MockedStatic<Bukkit> bukkit = mockBukkit()) {
            command.withdraw(player, guild, 150.0);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a provider that blew up must not pay the player");
        assertEquals(500.0, guild.getBalance(), 0.001,
                "a provider that blew up must not debit the bank");
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "the economy provider blew up");
    }

    @Test
    @DisplayName("a guild creation the provider refuses leaves no guild, no membership and no charge")
    void refusedGuildCreationPayment() {
        economy.withdrawing(Behaviour.FAILURE);
        final GuildHandler guildHandler = guildHandler();
        final ActionHandler actionHandler = new ActionHandler();

        try (MockedStatic<Bukkit> bukkit = mockBukkit();
             MockedStatic<Guilds> guilds = mockGuilds()) {
            createGuild(actionHandler, guildHandler, CREATION_COST);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a refused payment must not take money from the player");
        Mockito.verify(guildHandler, Mockito.never()).addGuild(Mockito.any());
        Mockito.verify(guildHandler, Mockito.never()).addToMemberCache(Mockito.any(), Mockito.any());
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "Insufficient funds");
        assertNull(actionHandler.getAction(player),
                "a refused payment must void the offer, so a later confirm cannot charge for it");
    }

    @Test
    @DisplayName("a guild creation the provider throws on leaves no guild, no membership and no charge")
    void throwingProviderOnGuildCreation() {
        economy.withdrawing(Behaviour.THROW);
        final GuildHandler guildHandler = guildHandler();
        final ActionHandler actionHandler = new ActionHandler();

        try (MockedStatic<Bukkit> bukkit = mockBukkit();
             MockedStatic<Guilds> guilds = mockGuilds()) {
            createGuild(actionHandler, guildHandler, CREATION_COST);
        }

        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a provider that blew up must not take money from the player");
        Mockito.verify(guildHandler, Mockito.never()).addGuild(Mockito.any());
        Mockito.verify(guildHandler, Mockito.never()).addToMemberCache(Mockito.any(), Mockito.any());
        Mockito.verify(issuer).sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED,
                "{reason}", "the economy provider blew up");
    }

    @Test
    @DisplayName("a guild creation that is paid for creates the guild and takes the money")
    void successfulGuildCreationPayment() {
        final GuildHandler guildHandler = guildHandler();
        final ActionHandler actionHandler = new ActionHandler();

        try (MockedStatic<Bukkit> bukkit = mockBukkit();
             MockedStatic<Guilds> guilds = mockGuilds()) {
            createGuild(actionHandler, guildHandler, CREATION_COST);
        }

        assertEquals(STARTING_BALANCE - CREATION_COST, economy.balance(), 0.001,
                "the player should have paid for the guild");
        Mockito.verify(guildHandler, Mockito.times(1)).addGuild(Mockito.any());
        Mockito.verify(guildHandler, Mockito.times(1)).addToMemberCache(Mockito.any(), Mockito.any());
        Mockito.verify(issuer, Mockito.never()).sendInfo(Mockito.eq(Messages.ERROR__ECONOMY_TRANSACTION_FAILED),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @DisplayName("a guild creation that costs nothing needs no transaction at all")
    void freeGuildCreation() {
        // The shipped default for the creation cost is 0.0. There is no money to move then, so a
        // provider that would refuse the transaction must not stand in the way of a free guild.
        economy.withdrawing(Behaviour.FAILURE);
        final GuildHandler guildHandler = guildHandler();
        final ActionHandler actionHandler = new ActionHandler();

        try (MockedStatic<Bukkit> bukkit = mockBukkit();
             MockedStatic<Guilds> guilds = mockGuilds()) {
            createGuild(actionHandler, guildHandler, 0.0);
        }

        assertEquals(0, economy.withdrawals(), "no money should have been moved");
        assertEquals(STARTING_BALANCE, economy.balance(), 0.001,
                "a free guild must not cost anything");
        Mockito.verify(guildHandler, Mockito.times(1)).addGuild(Mockito.any());
    }

    @Test
    @DisplayName("a guild creation the player can no longer afford charges nothing and leaves no guild")
    void unaffordableGuildCreation() {
        // The player is quoted a price they can pay, and spends the money before they confirm.
        // The affordability is checked again at that point, and nothing is taken and no guild is
        // made. This is the check that has to run before the payment.
        final GuildHandler guildHandler = guildHandler();
        final ActionHandler actionHandler = new ActionHandler();

        try (MockedStatic<Bukkit> bukkit = mockBukkit();
             MockedStatic<Guilds> guilds = mockGuilds()) {
            final ConfirmAction action = startGuildCreation(actionHandler, guildHandler, CREATION_COST);

            economy.setBalance(CREATION_COST - 1.0);

            assertThrows(ExpectationNotMet.class, action::accept);
        }

        assertEquals(CREATION_COST - 1.0, economy.balance(), 0.001,
                "a guild the player cannot afford must not be paid for");
        Mockito.verify(guildHandler, Mockito.never()).addGuild(Mockito.any());
        Mockito.verify(guildHandler, Mockito.never()).addToMemberCache(Mockito.any(), Mockito.any());
    }

    /**
     * Runs the create command and confirms it, the way a player would.
     *
     * @param actionHandler the handler that holds the confirmation
     * @param guildHandler  the handler that would hold the new guild
     * @param cost          what the guild costs
     */
    private void createGuild(ActionHandler actionHandler, GuildHandler guildHandler, double cost) {
        startGuildCreation(actionHandler, guildHandler, cost).accept();
    }

    /**
     * Runs the create command and leaves the confirmation pending.
     *
     * @param actionHandler the handler that holds the confirmation
     * @param guildHandler  the handler that would hold the new guild
     * @param cost          what the guild costs
     * @return the pending confirmation
     */
    private ConfirmAction startGuildCreation(ActionHandler actionHandler, GuildHandler guildHandler, double cost) {
        final CommandCreate command = new CommandCreate();
        command.setEconomy(economy);
        command.setGuildHandler(guildHandler);
        command.setActionHandler(actionHandler);
        command.setSettingsManager(settingsManager(cost));
        command.setCooldownHandler(new CooldownHandler(null));
        command.setPermission(Mockito.mock(Permission.class));

        command.create(player, "TestGuild", null);

        final ConfirmAction action = actionHandler.getAction(player);
        assertNotNull(action, "the create command should have asked for a confirmation");
        return action;
    }

    /**
     * @return a handler that knows about no guilds at all
     */
    private static GuildHandler guildHandler() {
        final GuildHandler guildHandler = Mockito.mock(GuildHandler.class);
        Mockito.when(guildHandler.getGuilds()).thenReturn(new HashMap<>());
        Mockito.when(guildHandler.getGuildRole(0)).thenReturn(new GuildRole("GuildMaster", "guilds.roles.master", 0));
        Mockito.when(guildHandler.getGuildTier(1)).thenReturn(tier());
        return guildHandler;
    }

    /**
     * @param cost what a guild costs
     * @return a settings manager that answers what the create command asks for
     */
    private static SettingsManager settingsManager(double cost) {
        final SettingsManager settingsManager = Mockito.mock(SettingsManager.class);
        Mockito.when(settingsManager.getProperty(CostSettings.CREATION)).thenReturn(cost);
        Mockito.when(settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE)).thenReturn(false);
        Mockito.when(settingsManager.getProperty(GuildSettings.NAME_REQUIREMENTS)).thenReturn("[a-zA-Z0-9]{1,64}");
        Mockito.when(settingsManager.getProperty(GuildSettings.INCLUDE_COLOR_CODES)).thenReturn(false);
        Mockito.when(settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)).thenReturn(true);
        return settingsManager;
    }

    /**
     * @param balance what the bank holds
     * @return a guild with that balance and no members
     */
    private static Guild guildWithBalance(double balance) {
        return Guild.builder()
                .id(UUID.randomUUID())
                .name("TestGuild")
                .prefix("")
                .balance(balance)
                .tier(tier())
                .members(new ArrayList<>())
                .build();
    }

    /**
     * @return a tier that allows a large bank balance
     */
    private static GuildTier tier() {
        return GuildTier.builder()
                .level(1)
                .name("Tier 1")
                .maxBankBalance(10_000.0)
                .build();
    }

    /**
     * @return a static mock of bukkit whose plugin manager swallows events
     */
    private static MockedStatic<Bukkit> mockBukkit() {
        final MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getPluginManager).thenReturn(Mockito.mock(PluginManager.class));
        return bukkit;
    }

    /**
     * @return a static mock of the plugin whose task chains do nothing
     */
    private static MockedStatic<Guilds> mockGuilds() {
        final TaskChain<?>[] holder = new TaskChain<?>[1];
        final TaskChain<?> chain = Mockito.mock(TaskChain.class, invocation -> {
            final Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType.isAssignableFrom(TaskChain.class)) {
                return holder[0];
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
        holder[0] = chain;

        final MockedStatic<Guilds> guilds = Mockito.mockStatic(Guilds.class);
        guilds.when(() -> Guilds.newChain()).thenReturn(holder[0]);
        return guilds;
    }
}
