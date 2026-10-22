package me.glaremasters.guilds.utils;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.messages.Messages;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A utility class to perform economy-related operations, such as checking player balances and formatting money values.
 */
public class EconomyUtils {

    private static final DecimalFormat df = new DecimalFormat("###,###.##", new DecimalFormatSymbols(Locale.US));

    /**
     * What a player is told when the economy provider refused a transaction without saying why.
     */
    private static final String GENERIC_FAILURE_REASON = "Please contact your server administrator.";

    /**
     * Checks if a player has enough money to perform an action.
     *
     * @param manager the CommandManager instance
     * @param economy the vault economy
     * @param player  the player being checked
     * @param amount  the amount required
     * @return `true` if the player has enough money, `false` otherwise
     */
    public static boolean hasEnough(CommandManager manager, Economy economy, Player player, double amount) {
        try {
            return economy.getBalance(player) >= amount;
        } catch (NullPointerException ex) {
            manager.getCommandIssuer(player).sendInfo(Messages.ERROR__ECONOMY_REQUIRED);
            return false;
        }
    }

    /**
     * Takes money out of a player's account.
     *
     * <p>Vault's economy methods answer with a response that says whether the money actually moved.
     * The response has to be read: a provider that refuses the withdrawal leaves the balance alone,
     * so treating a refused withdrawal as a successful one takes money out of a guild bank without
     * ever taking it out of the player.
     *
     * @param issuer  the issuer to tell about a failure
     * @param economy the vault economy
     * @param player  the player being charged
     * @param amount  the amount to withdraw
     * @return `true` if the money was withdrawn, `false` if the transaction did not go through
     */
    public static boolean withdraw(CommandIssuer issuer, Economy economy, OfflinePlayer player, double amount) {
        if (!isMovable(issuer, amount)) {
            return false;
        }
        if (amount == 0.0) {
            return true;
        }

        final EconomyResponse response;
        try {
            response = economy.withdrawPlayer(player, amount);
        } catch (NullPointerException ex) {
            // The same failure hasEnough reports: there is no economy plugin to talk to.
            issuer.sendInfo(Messages.ERROR__ECONOMY_REQUIRED);
            return false;
        } catch (Exception ex) {
            LoggingUtils.severe("The economy provider threw while withdrawing money.", ex);
            reportFailure(issuer, ex.getMessage());
            return false;
        }

        if (response == null || !response.transactionSuccess()) {
            reportFailure(issuer, response == null ? null : response.errorMessage);
            return false;
        }

        return true;
    }

    /**
     * Puts money into a player's account.
     *
     * <p>The mirror image of {@link #withdraw(CommandIssuer, Economy, OfflinePlayer, double)}. A
     * provider that refuses the deposit leaves the balance alone, so treating a refused deposit as
     * a successful one takes money out of a guild bank without ever handing it to the player.
     *
     * @param issuer  the issuer to tell about a failure
     * @param economy the vault economy
     * @param player  the player being paid
     * @param amount  the amount to deposit
     * @return `true` if the money was deposited, `false` if the transaction did not go through
     */
    public static boolean deposit(CommandIssuer issuer, Economy economy, OfflinePlayer player, double amount) {
        if (!isMovable(issuer, amount)) {
            return false;
        }
        if (amount == 0.0) {
            return true;
        }

        final EconomyResponse response;
        try {
            response = economy.depositPlayer(player, amount);
        } catch (NullPointerException ex) {
            // The same failure hasEnough reports: there is no economy plugin to talk to.
            issuer.sendInfo(Messages.ERROR__ECONOMY_REQUIRED);
            return false;
        } catch (Exception ex) {
            LoggingUtils.severe("The economy provider threw while depositing money.", ex);
            reportFailure(issuer, ex.getMessage());
            return false;
        }

        if (response == null || !response.transactionSuccess()) {
            reportFailure(issuer, response == null ? null : response.errorMessage);
            return false;
        }

        return true;
    }

    /**
     * Checks that an amount is one a provider can be asked to move.
     *
     * <p>A negative amount is refused rather than handed to the provider. Providers disagree on
     * what a negative withdrawal means, and the ones that treat it as a deposit hand the player
     * money instead of taking it.
     *
     * @param issuer the issuer to tell about a failure
     * @param amount the amount that is about to be moved
     * @return `true` if the amount is one that can be moved
     */
    private static boolean isMovable(CommandIssuer issuer, double amount) {
        if (amount >= 0.0) {
            return true;
        }

        LoggingUtils.severe("Refused to move a negative amount of money (" + amount + ").");
        reportFailure(issuer, GENERIC_FAILURE_REASON);
        return false;
    }

    /**
     * Tells the player that a transaction did not go through.
     *
     * @param issuer the issuer to tell
     * @param reason what the economy provider said, if anything
     */
    private static void reportFailure(CommandIssuer issuer, String reason) {
        issuer.sendInfo(Messages.ERROR__ECONOMY_TRANSACTION_FAILED, "{reason}",
                reason == null || reason.trim().isEmpty() ? GENERIC_FAILURE_REASON : reason);
    }


    /**
     * Checks if the first amount is greater than or equal to the second amount.
     *
     * @param val1 the first amount
     * @param val2 the second amount
     * @return `true` if `val1` is greater than or equal to `val2`, `false` otherwise
     */
    public static boolean hasEnough(double val1, double val2) {
        return val1 >= val2;
    }

    /**
     * Formats an integer value as a string with a currency-style comma separator.
     *
     * @param input the integer value to format
     * @return the formatted string
     */
    public static String format(int input) {
        return df.format(input);
    }

    /**
     * Formats a double value as a string with a currency-style comma separator.
     *
     * @param input the double value to format
     * @return the formatted string
     */
    public static String format(double input) {
        return df.format(input);
    }

}
