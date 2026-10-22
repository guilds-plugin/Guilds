package me.glaremasters.guilds.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

/**
 * A vault economy provider a test can aim.
 *
 * <p>Vault's economy methods answer with an {@link EconomyResponse} that says whether the money
 * actually moved. A provider that refuses the transaction leaves the balance alone, and a broken
 * provider throws instead of answering at all. This fake can be told to do either, so the failure
 * paths can be driven from a test.
 */
public class FakeEconomy implements Economy {

    /**
     * What the provider does when it is asked to move money.
     */
    public enum Behaviour {
        /** The money moves and the response says so. */
        SUCCESS,
        /** The money stays put and the response says the transaction failed. */
        FAILURE,
        /** The provider blows up instead of answering. */
        THROW
    }

    private static final String DEFAULT_FAILURE = "Insufficient funds";

    private double balance;
    private Behaviour withdrawBehaviour = Behaviour.SUCCESS;
    private Behaviour depositBehaviour = Behaviour.SUCCESS;
    private String failureMessage = DEFAULT_FAILURE;
    private int withdrawals;
    private int deposits;

    /**
     * @param startingBalance the balance the player starts with
     */
    public FakeEconomy(double startingBalance) {
        this.balance = startingBalance;
    }

    /**
     * @param behaviour what a withdrawal should do
     * @return this fake, for chaining
     */
    public FakeEconomy withdrawing(Behaviour behaviour) {
        this.withdrawBehaviour = behaviour;
        return this;
    }

    /**
     * @param behaviour what a deposit should do
     * @return this fake, for chaining
     */
    public FakeEconomy depositing(Behaviour behaviour) {
        this.depositBehaviour = behaviour;
        return this;
    }

    /**
     * @param message the error the provider reports when it refuses a transaction
     * @return this fake, for chaining
     */
    public FakeEconomy failingWith(String message) {
        this.failureMessage = message;
        return this;
    }

    /**
     * @return the balance the player has right now
     */
    public double balance() {
        return balance;
    }

    /**
     * Changes the balance without going through a transaction.
     *
     * <p>Used to move the world on between the prompt and the confirmation, the way a player who
     * spends their money in another plugin does.
     *
     * @param balance the balance the player has from now on
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * @return how often a withdrawal was asked for
     */
    public int withdrawals() {
        return withdrawals;
    }

    /**
     * @return how often a deposit was asked for
     */
    public int deposits() {
        return deposits;
    }

    private EconomyResponse withdraw(double amount) {
        withdrawals++;
        switch (withdrawBehaviour) {
            case THROW:
                throw new IllegalStateException("the economy provider blew up");
            case FAILURE:
                return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, failureMessage);
            default:
                balance -= amount;
                return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }
    }

    private EconomyResponse deposit(double amount) {
        deposits++;
        switch (depositBehaviour) {
            case THROW:
                throw new IllegalStateException("the economy provider blew up");
            case FAILURE:
                return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, failureMessage);
            default:
                balance += amount;
                return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "FakeEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.valueOf(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "coins";
    }

    @Override
    public String currencyNameSingular() {
        return "coin";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    public double getBalance(String playerName) {
        return balance;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return balance;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return balance;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return balance;
    }

    @Override
    public boolean has(String playerName, double amount) {
        return balance >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return balance >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return balance >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return balance >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdraw(amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdraw(amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdraw(amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdraw(amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return deposit(amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return deposit(amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return deposit(amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return deposit(amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return unsupported();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return unsupported();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return unsupported();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return unsupported();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return unsupported();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return unsupported();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return unsupported();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return unsupported();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return unsupported();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return unsupported();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return unsupported();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    private static EconomyResponse unsupported() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This fake has no banks");
    }
}
