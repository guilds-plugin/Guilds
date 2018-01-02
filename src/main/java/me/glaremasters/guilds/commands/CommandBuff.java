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
        Guilds.getInstance().getConfig().getStringList("buff.description.haste").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(haste::add);
        haste.add("");
        haste.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.haste")));
        haste.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.haste")));
        buff.setItem(0, createItemStack(Material.FEATHER,
                Guilds.getInstance().getConfig().getString("buff.name.haste"), haste));

        // Buff 2: Walk Speed
        ArrayList<String> speed = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.speed").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(speed::add);
        speed.add("");
        speed.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.speed")));
        speed.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.speed")));
        buff.setItem(4, createItemStack(Material.SUGAR,
                Guilds.getInstance().getConfig().getString("buff.name.speed"), speed));

        // Buff 3: Fire Resistance
        ArrayList<String> fireResistance = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.fire-resistance").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(fireResistance::add);
        fireResistance.add("");
        fireResistance.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.fire-resistance")));
        fireResistance.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.fire-resistance")));
        buff.setItem(2, createItemStack(Material.BLAZE_POWDER,
                Guilds.getInstance().getConfig().getString("buff.name.fire-resistance"),
                fireResistance));

        // Buff 4: Night Vision
        ArrayList<String> nightvision = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.night-vision").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(nightvision::add);
        nightvision.add("");
        nightvision.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.night-vision")));
        nightvision.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.night-vision")));
        buff.setItem(6, createItemStack(Material.REDSTONE_TORCH_ON,
                Guilds.getInstance().getConfig().getString("buff.name.night-vision"), nightvision));

        // Buff 5: Invisibility
        ArrayList<String> invisibility = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.invisibility").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(invisibility::add);
        invisibility.add("");
        invisibility.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.invisibility")));
        invisibility.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.invisibility")));
        buff.setItem(8, createItemStack(Material.EYE_OF_ENDER,
                Guilds.getInstance().getConfig().getString("buff.name.invisibility"), invisibility));

        // Buff 6: Strength
        ArrayList<String> strength = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.strength").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(strength::add);
        strength.add("");
        strength.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.strength")));
        strength.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.strength")));
        buff.setItem(1, createItemStack(Material.DIAMOND_SWORD,
                Guilds.getInstance().getConfig().getString("buff.name.strength"), strength));

        // Buff 7: Jump
        ArrayList<String> jump = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.jump").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(jump::add);
        jump.add("");
        jump.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.jump")));
        jump.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.jump")));
        buff.setItem(3, createItemStack(Material.DIAMOND_BOOTS,
                Guilds.getInstance().getConfig().getString("buff.name.jump"), jump));

        // Buff 8: Water Breathing
        ArrayList<String> waterbreathing = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.water-breathing").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it))
                .forEach(waterbreathing::add);
        waterbreathing.add("");
        waterbreathing.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.water-breathing")));
        waterbreathing.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.water-breathing")));
        buff.setItem(5, createItemStack(Material.BUCKET,
                Guilds.getInstance().getConfig().getString("buff.name.water-breathing"),
                waterbreathing));

        // Buff 9: Luck
        ArrayList<String> regeration = new ArrayList<String>();
        Guilds.getInstance().getConfig().getStringList("buff.description.regeration").stream()
                .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(regeration::add);
        regeration.add("");
        regeration.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.price") + Guilds
                        .getInstance().getConfig().getInt("buff.price.regeration")));
        regeration.add(ChatColor.translateAlternateColorCodes('&',
                Guilds.getInstance().getConfig().getString("buff.description.length") + Guilds
                        .getInstance().getConfig().getInt("buff.time.regeration")));
        buff.setItem(7, createItemStack(Material.EMERALD,
                Guilds.getInstance().getConfig().getString("buff.name.regeration"), regeration));

        // Open inventory
        player.openInventory(buff);
    }
}
