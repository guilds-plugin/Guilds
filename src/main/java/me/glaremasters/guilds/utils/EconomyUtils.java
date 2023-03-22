/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
