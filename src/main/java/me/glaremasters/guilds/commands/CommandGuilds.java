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
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildInviteEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildHome;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildSkull;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//todo rewrite lol -> this has been rewritten mostly there are still quite a lot of todos due to things being unclear
// or not being added yet, make sure to fix these todos and commented out code and then we can start cleaning the warnings.
// xx lemmo.
@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandGuilds extends BaseCommand {

    private Guilds guilds;
    private GuildHandler guildHandler;
    public final static Inventory guildList = null;
    public final static Map<UUID, Integer> playerPages = new HashMap<>();
    //todo give me explanation pls @Glare
    public final List<Player> home = new ArrayList<>();
    public final List<Player> setHome = new ArrayList<>();
    public final Map<Player, Location> warmUp = new HashMap<>();
    private SettingsManager settingsManager;
    private ActionHandler actionHandler;
    private Economy economy;

    /**
     * List all the guilds on the server
     * @param player the player executing this command
     */
    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @CommandPermission(Constants.BASE_PERM + "list")
    public void onGuildList(Player player) {
        //todo after explanation waiting for @Glare
        playerPages.put(player.getUniqueId(), 1);
        // guildList = getSkullsPage(1);
        player.openInventory(guildList);
    }

    /**
     * Opens the guild vault
     * @param player the player opening the vault
     * @param guild the guild's vault which's being opened
     * @param role the role of the player
     */
    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @CommandPermission(Constants.BASE_PERM + "vault")
    public void onVault(Player player, Guild guild, GuildRole role, @Default("1") Integer vault) {
        if (!role.isOpenVault()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        GuildTier tier = guild.getTier();

        if (vault > tier.getVaultAmount()) {
            getCurrentCommandIssuer().sendInfo(Messages.VAULTS__MAXED);
            return;
        }

        try {
            guildHandler.getGuildVault(guild, vault);
        } catch (Exception ex) {
            guildHandler.getCachedVaults().get(guild).add(Bukkit.createInventory(null, 54, "PlaceholderText"));
        }

        player.openInventory(guildHandler.getGuildVault(guild, vault));
    }

    /**
     * Request an invite
     *
     * @param player the player requesting
     * @param name   the name of the guild
     */
    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission(Constants.BASE_PERM + "request")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onRequest(Player player, @Values("@guilds") @Single String name) {
        Guild guild = guildHandler.getGuild(player);
        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        Guild targetGuild = guildHandler.getGuild(name);
        if (targetGuild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        for (GuildMember member : targetGuild.getMembers()) {
            GuildRole role = member.getRole();
            if (role.isInvite()) {
                OfflinePlayer guildPlayer = Bukkit.getOfflinePlayer(member.getUuid());
                if (guildPlayer.isOnline()) {
                    getCurrentCommandManager().getCommandIssuer(guildPlayer).sendInfo(Messages.REQUEST__INCOMING_REQUEST, "{player}", player.getName());
                }
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.REQUEST__SUCCESS, "{guild}", targetGuild.getName());
    }

    /**
     * Open the guild buff menu
     * @param player the player opening the menu
     * @param guild the guild which's player is opening the menu
     * @param role the role of the player
     */
    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @CommandPermission(Constants.BASE_PERM + "buff")
    public void onBuff(Player player, Guild guild, GuildRole role) {
        if (!role.isActivateBuff()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Inventory buff = Bukkit.createInventory(null, 9, settingsManager.getProperty(GuiSettings.GUILD_BUFF_NAME));
        List<String> lore = new ArrayList<>();
/*        createBuffItem("haste", lore, buff, 0);
        createBuffItem("speed", lore, buff, 1);
        createBuffItem("fire-resistance", lore, buff, 2);
        createBuffItem("night-vision", lore, buff, 3);
        createBuffItem("invisibility", lore, buff, 4);
        createBuffItem("strength", lore, buff, 5);
        createBuffItem("jump", lore, buff, 6);
        createBuffItem("water-breathing", lore, buff, 7);
        createBuffItem("regeneration", lore, buff, 8);*/
        player.openInventory(buff);
    }

    /**
     * Create an item stack for the list
     * @param mat
     * @param name
     * @param lore
     * @return
     */
    private ItemStack createItemStack(Material mat, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(mat);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    //todo rewrite this + explanation plz @Glare
//    /**
//     * Create an item for buff
//     * @param buffName
//     * @param name
//     * @param buff
//     * @param slot
//     */
//    private void createBuffItem(String buffName, List<String> name, Inventory buff, int slot) {
//        getStringList("buff.description." + buffName).stream().map(ConfigUtils::StringUtils.color).forEach(name::add);
//        name.add("");
//        name.add(getString("buff.description.price") + getString("buff.price." + buffName));
//        name.add(getString("buff.description.length") + getString("buff.time." + buffName));
//        if (getBoolean("buff.display." + buffName)) {
//            buff.setItem(slot, createItemStack(Material.getMaterial(getString("buff.icon." + buffName)), getString("buff.name." + buffName), name));
//        }
//        name.clear();
//    }
//
//    /**
//     * Handling for the list page
//     * @param page
//     * @return
//     */
//    public static Inventory getSkullsPage(int page) {
//        Map<UUID, ItemStack> skulls = new HashMap<>();
//        Inventory inv = Bukkit.createInventory(null, 54, getString("guild-list.gui-name"));
//
//        int startIndex = 0;
//        int endIndex = 0;
//
//        Guilds.getGuilds().getGuildHandler().getGuilds().values().forEach(guild -> {
//            ItemStack skull = HeadUtils.getSkull(HeadUtils.getTextureUrl(guild.getGuildMaster().getUniqueId()));
//            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
//            List<String> lore = new ArrayList<>();
//
//            getStringList("guild-list.head-lore").forEach(line -> lore.add(StringUtils.color(line)
//                    .replace("{guild-name}", guild.getName())
//                    .replace("{guild-prefix}", guild.getPrefix())
//                    .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
//                    .replace("{guild-status}", guild.getStatus())
//                    .replace("{guild-tier}", String.valueOf(guild.getTier()))
//                    .replace("{guild-balance}", String.valueOf(guild.getBalance()))
//                    .replace("{guild-member-count}", String.valueOf(guild.getMembers().size()))));
//
//            skullMeta.setLore(lore);
//
//            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
//            skullMeta.setDisplayName(getString("guild-list.item-name").replace("{player}", name).replace("{guild-name}", guild.getName()));
//            skull.setItemMeta(skullMeta);
//            skulls.put(guild.getGuildMaster().getUniqueId(), skull);
//        });
//
//        ItemStack previous = new ItemStack(Material.getMaterial(getString("guild-list.previous-page-item")), 1);
//        ItemMeta previousMeta = previous.getItemMeta();
//        previousMeta.setDisplayName(getString("guild-list.previous-page-item-name"));
//        previous.setItemMeta(previousMeta);
//        ItemStack next = new ItemStack(Material.getMaterial(getString("guild-list.next-page-item")), 1);
//        ItemMeta nextMeta = next.getItemMeta();
//        nextMeta.setDisplayName(getString("guild-list.next-page-item-name"));
//        next.setItemMeta(nextMeta);
//        ItemStack barrier = new ItemStack(Material.getMaterial(getString("guild-list.page-number-item")), 1);
//        ItemMeta barrierMeta = barrier.getItemMeta();
//        barrierMeta.setDisplayName(getString("guild-list.page-number-item-name").replace("{page}", String.valueOf(page)));
//        barrier.setItemMeta(barrierMeta);
//        inv.setItem(53, next);
//        inv.setItem(49, barrier);
//        inv.setItem(45, previous);
//
//        startIndex = (page - 1) * 45;
//        endIndex = startIndex + 45;
//
//        if (endIndex > skulls.values().size()) {
//            endIndex = skulls.values().size();
//        }
//
//        int iCount = 0;
//        for (int i1 = startIndex; i1 < endIndex; i1++) {
//            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
//            iCount++;
//        }
//
//        return inv;
//    }
//
//    /**
//     * Create player skull
//     * @param player
//     * @return
//     */
//    public ItemStack createSkull(Player player) {
//        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
//
//        SkullMeta meta = (SkullMeta) skull.getItemMeta();
//        meta.setOwner(player.getName());
//        meta.setDisplayName(getString("info.playername").replace("{player-name}", player.getName()));
//
//        List<String> info = new ArrayList<>();
//        info.add(getString("info.kills").replace("{kills}", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS))));
//        info.add(getString("info.deaths").replace("{deaths}", String.valueOf(player.getStatistic(Statistic.DEATHS))));
//        meta.setLore(info);
//
//        skull.setItemMeta(meta);
//        return skull;
//    }
//
//
    /**
     * Checks the name requirements from the config.
     * @param name the name to check
     * @return a boolean if the name is wrong
     */
    private boolean nameMeetsRequirements(String name) {
        String regex = settingsManager.getProperty(GuildSettings.NAME_REQUIREMENTS);

        if (!name.matches(regex)) {
            getCurrentCommandIssuer().sendInfo(Messages.CREATE__REQUIREMENTS);
            return true;
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE)) {
            for (String word : settingsManager.getProperty(GuildSettings.BLACKLIST_WORDS)) {
                if (name.toLowerCase().contains(word)) {
                    getCurrentCommandIssuer().sendInfo(Messages.ERROR__BLACKLIST);
                    return true;
                }
            }
        }

        return false;
    }

}
