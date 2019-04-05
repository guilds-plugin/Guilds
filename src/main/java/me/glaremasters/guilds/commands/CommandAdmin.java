/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.commands;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

//todo this todo is for all command classes.
//I have noticed that you only send certain messages on certain commands however these messages should also be sent in other places.
//Example I have seen is that you sent a ERROR__GUILD_NO_EXIST message in this admin command but not in the ally commands (haven't checked all commands classes)
//I've also noticed a lot of differences between Bukkit.getOfflinePlayer(String) and Bukkit.getPlayerExact(String) and Bukkit.getPlayer(String)
//You should really only be using the first one or the last 2 ones.

@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandAdmin extends BaseCommand {

    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private SettingsManager settingsManager;

    /**
     * Admin command to change the prefix of a guild
     * @param player the admin running the command
     * @param name the name of a guild
     * @param prefix the new prefix of the guild
     */
    @Subcommand("admin prefix")
    @Description("{@@descriptions.admin-prefix}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<name> <prefix>")
    public void onAdminGuildPrefix(Player player, @Values("@guilds") @Single String name, String prefix) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        guild.setPrefix(StringUtils.color(prefix));

        getCurrentCommandIssuer().sendInfo(Messages.PREFIX__SUCCESSFUL);
    }

    /**
     * Admin command to rename a guild
     * @param player the admin running the command
     * @param name the name of the guild
     * @param newName the new name of the guild
     */
    @Subcommand("admin rename")
    @Description("{@@descriptions.admin-prefix}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<name> <new name>")
    public void onAdminGuildRename(Player player, @Values("@guilds") @Single String name, String newName) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        guild.setName(StringUtils.color(newName));

        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL, "{name}", newName);
    }

    /**
     * Admin command to turn on Guild Chat Spy
     * @param player the player executing the command
     */
    @Subcommand("admin spy")
    @Description("{@@descriptions.admin-spy}")
    @CommandPermission("guilds.command.admin")
    public void onAdminSpy(Player player) {
        guildHandler.toggleSpy(getCurrentCommandManager(), player);
    }

    @Subcommand("admin vault")
    @Description("{@@descriptions.admin-vault}")
    @CommandPermission("guilds.command.admin")
    @CommandCompletion("@guilds")
    @Syntax("<guild> <vault #>")
    public void onAdminVault(Player player, @Values("@guilds") @Single String name, Integer vault) {
        Guild guild = guildHandler.getGuild(name);
        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }
        Inventory inv = guildHandler.getGuildVault(guild, vault);
        if (inv != null) player.openInventory(inv);

    }

    /**
     * Give a player upgrade tickets
     * @param sender the executor of this command
     * @param player the player receiving the tickets
     * @param amount amount of tickets
     */
    @Subcommand("give")
    @Description("{@@descriptions.give}")
    @CommandPermission("guilds.command.admin")
    @Syntax("<player> <amount>")
    public void onTicketGive(CommandSender sender, Player player, @Default("1") Integer amount) {
        if (player == null) return;

        /* todo add back in the config @Glare
        String ticketName = getString("upgrade-ticket.name");
        String ticketMaterial = getString("upgrade-ticket.material");
        String ticketLore = getString("upgrade-ticket.lore");

        ItemStack upgradeTicket = new ItemStack(Material.matchMaterial(settingsManager.getProperty()), amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ticketLore);
        meta.setDisplayName(ticketName);
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
        */
    }

    /**
     * Reload the config
     */
    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission("guilds.command.admin")
    public void onReload() {
        settingsManager.reload();
        getCurrentCommandIssuer().sendInfo(Messages.RELOAD__RELOADED);
    }

}
