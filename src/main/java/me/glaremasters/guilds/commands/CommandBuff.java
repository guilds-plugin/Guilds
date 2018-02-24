package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by GlareMasters on 7/24/2017.
 */
public class CommandBuff extends CommandBase {

    Inventory buff = Bukkit.createInventory(null, 9, "Guild Buffs");

    public CommandBuff() {
        super("buff", Guilds.getInstance().getConfig().getString("commands.description.buff"),
                "guilds.command.buff", false,
                null, null, 0, 0);
    }


    public ItemStack createItemStack(Material mat, String name, ArrayList<String> Lore) {
        ItemStack paper = new ItemStack(mat);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Lore);

        paper.setItemMeta(meta);
        return paper;
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canActivateBuff()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        // Buff 1: Haste
        ArrayList<String> haste = new ArrayList<String>();
        FileConfiguration config = Guilds.getInstance().getConfig();
        config.getStringList("buff.description.haste").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(haste::add);
        haste.add("");
        haste.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.haste")));
        haste.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.haste")));
        if (config.getBoolean("buff.display.haste")) {
            buff.setItem(0, createItemStack(Material.getMaterial(config.getString("buff.icon.haste")),
                    config.getString("buff.name.haste"), haste));
        }
        // Buff 2: Walk Speed
        ArrayList<String> speed = new ArrayList<String>();
        config.getStringList("buff.description.speed").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(speed::add);
        speed.add("");
        speed.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.speed")));
        speed.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.speed")));
        if (config.getBoolean("buff.display.speed")) {
            buff.setItem(4,
                    createItemStack(Material.getMaterial(config.getString("buff.icon.speed")),
                            config.getString("buff.name.speed"), speed));
        }

        // Buff 3: Fire Resistance
        ArrayList<String> fireResistance = new ArrayList<String>();
        config.getStringList("buff.description.fire-resistance").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(fireResistance::add);
        fireResistance.add("");
        fireResistance.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.fire-resistance")));
        fireResistance.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.fire-resistance")));
        if (config.getBoolean("buff.display.fire-resistance")) {
            buff.setItem(2, createItemStack(
                    Material.getMaterial(config.getString("buff.icon.fire-resistance")),
                    config.getString("buff.name.fire-resistance"),
                    fireResistance));
        }

        // Buff 4: Night Vision
        ArrayList<String> nightvision = new ArrayList<String>();
        config.getStringList("buff.description.night-vision").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(nightvision::add);
        nightvision.add("");
        nightvision.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.night-vision")));
        nightvision.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.night-vision")));
        if (config.getBoolean("buff.display.night-vision")) {
            buff.setItem(6, createItemStack(
                    Material.getMaterial(config.getString("buff.icon.night-vision")),
                    config.getString("buff.name.night-vision"), nightvision));
        }

        // Buff 5: Invisibility
        ArrayList<String> invisibility = new ArrayList<String>();
        config.getStringList("buff.description.invisibility").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(invisibility::add);
        invisibility.add("");
        invisibility.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.invisibility")));
        invisibility.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.invisibility")));
        if (config.getBoolean("buff.display.invisibility")) {
            buff.setItem(8, createItemStack(
                    Material.getMaterial(config.getString("buff.icon.invisibility")),
                    config.getString("buff.name.invisibility"), invisibility));
        }

        // Buff 6: Strength
        ArrayList<String> strength = new ArrayList<String>();
        config.getStringList("buff.description.strength").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(strength::add);
        strength.add("");
        strength.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.strength")));
        strength.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.strength")));
        if (config.getBoolean("buff.display.strength")) {
            buff.setItem(1,
                    createItemStack(Material.getMaterial(config.getString("buff.icon.strength")),
                            config.getString("buff.name.strength"), strength));
        }

        // Buff 7: Jump
        ArrayList<String> jump = new ArrayList<String>();
        config.getStringList("buff.description.jump").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(jump::add);
        jump.add("");
        jump.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.jump")));
        jump.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.jump")));
        if (config.getBoolean("buff.display.jump")) {
            buff.setItem(3,
                    createItemStack(Material.getMaterial(config.getString("buff.icon.jump")),
                            config.getString("buff.name.jump"), jump));
        }

        // Buff 8: Water Breathing
        ArrayList<String> waterbreathing = new ArrayList<String>();
        config.getStringList("buff.description.water-breathing").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(waterbreathing::add);
        waterbreathing.add("");
        waterbreathing.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.water-breathing")));
        waterbreathing.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.water-breathing")));
        if (config.getBoolean("buff.display.water-breathing")) {
            buff.setItem(5, createItemStack(
                    Material.getMaterial(config.getString("buff.icon.water-breathing")),
                    config.getString("buff.name.water-breathing"),
                    waterbreathing));
        }

        // Buff 9: Luck
        ArrayList<String> regeneration = new ArrayList<String>();
        config.getStringList("buff.description.regeneration").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(regeneration::add);
        regeneration.add("");
        regeneration.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.price") + config.getInt("buff.price.regeneration")));
        regeneration.add(ChatColor.translateAlternateColorCodes('&',
                config.getString("buff.description.length") + config.getInt("buff.time.regeneration")));
        if (config.getBoolean("buff.display.regeneration")) {
            buff.setItem(7, createItemStack(
                    Material.getMaterial(config.getString("buff.icon.regeneration")),
                    config.getString("buff.name.regeneration"), regeneration));
        }

        // Open inventory
        player.openInventory(buff);
    }
}
