package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
import java.util.ArrayList;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class CommandInfo extends CommandBase implements Listener {
    FileConfiguration config = Guilds.getInstance().getConfig();
    public CommandInfo() {
        super("info", Guilds.getInstance().getConfig().getString("commands.description.info"),
                "guilds.command.info", false, null, null, 0, 0);
    }

    public ItemStack createItemStack(Material mat, String name, ArrayList<String> Lore) {
        ItemStack paper = new ItemStack(mat);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Lore);

        paper.setItemMeta(meta);
        return paper;
    }

    public ItemStack createSkull(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        final Guilds instance = Guilds.getInstance();
        meta.setDisplayName(color(config.getString("info.playername").replace("{player-name}", player.getName())));

        ArrayList<String> info = new ArrayList<String>();
        info.add(color(config.getString("info.kills").replace("{kills}", Integer.toString(player.getStatistic(Statistic.PLAYER_KILLS)))));
        info.add(color(config.getString("info.deaths").replace("{deaths}", Integer.toString(player.getStatistic(Statistic.DEATHS)))));
        meta.setLore(info);

        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        Inventory heads = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GREEN + config.getString("gui-name.info"));
        // Skull: From player
        heads.setItem(1, createSkull(player));

        // Item 1: Paper
        ArrayList<String> paperlore = new ArrayList<String>();
        paperlore.add(color(config.getString("info.guildname").replace("{guild-name}", guild.getName())));
        paperlore.add(color(config.getString("info.prefix").replace("{guild-prefix}", guild.getPrefix())));
        paperlore.add(color(config.getString("info.role").replace("{guild-role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName())));
        paperlore.add(color(config.getString("info.master").replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())));
        paperlore.add(color(config.getString("info.member-count").replace("{member-count}", Integer.toString(guild.getMembers().size()))));
        paperlore.add(color(config.getString("info.guildstatus").replace("{guild-status}", guild.getStatus())));
        paperlore.add(color(config.getString("info.guildtier").replace("{guild-tier}", Integer.toString(guild.getTier()))));
        heads.setItem(2, createItemStack(Material.PAPER, config.getString("info.info"), paperlore));

        // Item 2: Diamond
        ArrayList<String> diamondlore = new ArrayList<String>();
        diamondlore.add(color(config.getString("info.balance").replace("{guild-balance}", Double.toString(guild.getBankBalance()))));
        diamondlore.add(color(config.getString("info.max-balance").replace("{guild-max-balance}", Double.toString(guild.getMaxBankBalance()))));
        heads.setItem(3, createItemStack(Material.DIAMOND, config.getString("info.money"), diamondlore));

        // Open inventory
        player.openInventory(heads);


    }
}
