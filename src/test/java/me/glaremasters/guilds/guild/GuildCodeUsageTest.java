package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.CommandOperationContext;
import co.aikar.locales.MessageKey;
import me.glaremasters.guilds.commands.codes.CommandCode;
import me.glaremasters.guilds.configuration.sections.CodeSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Stack;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the number of uses an invite code may be created with.
 *
 * <p>{@code /guild code create} takes the number of uses as an argument. Nothing checked it, so a
 * guild master could ask for zero uses or for a negative number. Both produced a code that the
 * plugin could never redeem: {@code Guild#getActiveCodes()} only counts codes with uses above
 * zero, so such a code was invisible to the active-code limit and to the code list, and
 * {@code GuildHandler#handleInvite} refuses to redeem anything with no uses left. A guild could
 * therefore fill its storage with codes that neither counted towards its limit nor ever worked.
 *
 * <p>The rule is that a code has to be created with at least one use. A code that has been
 * redeemed down to zero is a normal, spent code and stays exactly as it is.
 */
class GuildCodeUsageTest {

    /** The message key the refusal carries, so the player sees the same thing everywhere. */
    private static final String INVALID_USES_KEY = "codes.invalid-uses";

    /** The active-code limit the tests pretend the server has configured. */
    private static final int ACTIVE_CODE_LIMIT = 2;

    @SuppressWarnings("rawtypes")
    private ThreadLocal<Stack<CommandOperationContext>> commandContext;

    @AfterEach
    void clearCommandContext() throws Exception {
        if (commandContext != null) {
            commandContext.remove();
        }
    }

    @Test
    @DisplayName("a code with no uses is refused and nothing is stored")
    void zeroUsesIsRefused() throws Exception {
        final Guild guild = guild();
        final CommandCode command = command();

        final ExpectationNotMet refusal = assertThrows(ExpectationNotMet.class,
                () -> command.create(player(), guild, 0),
                "a code with no uses must be refused");

        assertEquals(INVALID_USES_KEY, messageKeyOf(refusal));
        assertTrue(guild.getCodes().isEmpty(), "the refused code must not be stored");
        assertFalse(guild.getActiveCheck(ACTIVE_CODE_LIMIT),
                "the refused code must not count against the active-code limit");
    }

    @Test
    @DisplayName("a code with a negative number of uses is refused and nothing is stored")
    void negativeUsesIsRefused() throws Exception {
        final Guild guild = guild();
        final CommandCode command = command();

        final ExpectationNotMet refusal = assertThrows(ExpectationNotMet.class,
                () -> command.create(player(), guild, -1),
                "a code with a negative number of uses must be refused");

        assertEquals(INVALID_USES_KEY, messageKeyOf(refusal));
        assertTrue(guild.getCodes().isEmpty(), "the refused code must not be stored");
        assertFalse(guild.getActiveCheck(ACTIVE_CODE_LIMIT),
                "the refused code must not count against the active-code limit");
    }

    @Test
    @DisplayName("a refused code leaves the guild's whole allowance of codes")
    void refusedCodeLeavesTheAllowanceAlone() throws Exception {
        final Guild guild = guild();
        final CommandCode command = command();

        assertThrows(ExpectationNotMet.class, () -> command.create(player(), guild, 0));
        assertThrows(ExpectationNotMet.class, () -> command.create(player(), guild, -1));

        // The guild can still create as many codes as the limit allows.
        for (int index = 0; index < ACTIVE_CODE_LIMIT; index++) {
            command.create(player(), guild, 1);
        }

        assertEquals(ACTIVE_CODE_LIMIT, guild.getCodes().size());
        assertTrue(guild.getActiveCheck(ACTIVE_CODE_LIMIT));
    }

    @Test
    @DisplayName("a code with one use is created and can be redeemed once")
    void oneUseCodeIsCreated() throws Exception {
        final Guild guild = guild();
        final CommandCode command = command();

        command.create(player(), guild, 1);

        assertEquals(1, guild.getCodes().size());
        assertEquals(1, guild.getCodes().get(0).getUses());
        assertEquals(1, guild.getActiveCodes().size());

        guild.getCodes().get(0).addRedeemer(player());

        assertEquals(0, guild.getCodes().get(0).getUses());
        assertTrue(guild.getActiveCodes().isEmpty(), "a spent code is no longer an active code");
    }

    @Test
    @DisplayName("a code with two uses can be redeemed twice")
    void twoUseCodeIsCreated() throws Exception {
        final Guild guild = guild();
        final CommandCode command = command();

        command.create(player(), guild, 2);

        assertEquals(1, guild.getCodes().size());
        assertEquals(2, guild.getCodes().get(0).getUses());

        final GuildCode code = guild.getCodes().get(0);
        code.addRedeemer(player());
        assertEquals(1, code.getUses());
        code.addRedeemer(player());
        assertEquals(0, code.getUses());
    }

    @Test
    @DisplayName("the guild refuses to store a code with no uses of its own accord")
    void guildRefusesToStoreAnImpossibleCode() {
        final Guild guild = guild();

        assertThrows(IllegalArgumentException.class, () -> guild.addCode("DEAD1", 0, player()),
                "a code with no uses cannot be stored");
        assertThrows(IllegalArgumentException.class, () -> guild.addCode("DEAD2", -1, player()),
                "a code with a negative number of uses cannot be stored");

        assertTrue(guild.getCodes().isEmpty());
        assertTrue(guild.getActiveCodes().isEmpty());
        assertFalse(guild.getActiveCheck(ACTIVE_CODE_LIMIT));
    }

    /**
     * Builds a guild with an empty set of codes, the way a guild that has never made one looks.
     *
     * @return a guild whose code list is empty
     */
    private static Guild guild() {
        final Guild guild = new Guild(UUID.randomUUID());
        guild.setCodes(new ArrayList<>());
        return guild;
    }

    /**
     * Builds the command under test, with the settings and the command issuer it needs.
     *
     * <p>The issuer is what the command sends its confirmation through. ACF keeps the issuer for
     * the command being executed in a thread local, so the test puts one there.
     *
     * @return a command ready to be called
     * @throws Exception if ACF's internals cannot be reached
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private CommandCode command() throws Exception {
        final SettingsManager settings = mock(SettingsManager.class);
        when(settings.getProperty(CodeSettings.ACTIVE_CODE_AMOUNT)).thenReturn(ACTIVE_CODE_LIMIT);
        when(settings.getProperty(CodeSettings.CODE_LENGTH)).thenReturn(7);

        final CommandCode command = new CommandCode();
        command.setSettingsManager(settings);

        final Field field = CommandManager.class.getDeclaredField("commandOperationContext");
        field.setAccessible(true);
        this.commandContext = (ThreadLocal<Stack<CommandOperationContext>>) field.get(null);

        final CommandOperationContext context = mock(CommandOperationContext.class);
        when(context.getCommandIssuer()).thenReturn(mock(CommandIssuer.class));

        final Stack<CommandOperationContext> stack = new Stack<>();
        stack.push(context);
        commandContext.set(stack);

        return command;
    }

    /**
     * A player who created a code. Nothing but the identity is ever asked of them.
     *
     * @return a player with a fresh identity
     */
    private static Player player() {
        final Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        return player;
    }

    /**
     * The message key an ACF argument failure carries.
     *
     * <p>ACF keeps the key in a package private field, so it has to be read reflectively.
     *
     * @param failure the failure the command raised
     * @return the key the player is shown
     * @throws Exception if the field cannot be read
     */
    private static String messageKeyOf(ExpectationNotMet failure) throws Exception {
        final Field field = failure.getClass().getSuperclass().getDeclaredField("key");
        field.setAccessible(true);
        return ((MessageKey) field.get(failure)).getKey();
    }
}
