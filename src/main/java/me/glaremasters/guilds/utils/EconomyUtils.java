package me.glaremasters.guilds.utils;

import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.messages.Messages;
import net.milkbowl.vault.economy.Economy;
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
