package me.glaremasters.guilds.listeners;


import java.util.stream.Stream;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by GlareMasters on 7/24/2017.
 */
public class GuildBuffListener implements Listener {


    @EventHandler
    public void onBuffBuy(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (event.getInventory().getTitle().equals("Guild Buffs")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) {
                return;
            }
            GuildBuff buff = GuildBuff.get(event.getCurrentItem().getType());
            double balance = guild.getBankBalance();
            if (buff != null) {
                if (Guilds.getInstance().getConfig().getBoolean("use-guild-bank")) {
                    if (balance < buff.cost) {
                        Message.sendMessage(player, Message.COMMAND_BANK_WITHDRAW_FAILURE);
                        return;
                    }
                    if (Guilds.getInstance().getConfig().getBoolean("disable-buff-stacking")
                            && !player
                            .getActivePotionEffects().isEmpty()) {
                        return;
                    }

                    guild.getMembers().stream()
                            .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                            .filter(OfflinePlayer::isOnline)
                            .forEach(member -> {

                                ((Player) member).addPotionEffect(
                                        new PotionEffect(buff.potion, buff.time,
                                                buff.amplifier));

                            });

                    Guilds.getInstance().guildBanksConfig
                            .set(guild.getName(), balance - buff.cost);
                    Guilds.getInstance().saveGuildData();
                    guild.updateGuild("");
                } else {

                    if (Guilds.vault && buff.cost != -1) {
                        if (Guilds.getInstance().getEconomy().getBalance(player) < buff.cost) {
                            Message.sendMessage(player, Message.COMMAND_BUFF_NOT_ENOUGH_MONEY);
                            return;
                        }
                        if (Guilds.getInstance().getConfig().getBoolean("disable-buff-stacking")
                                && !player
                                .getActivePotionEffects().isEmpty()) {
                            return;
                        }

                        EconomyResponse response =
                                Guilds.getInstance().getEconomy().withdrawPlayer(player, buff.cost);
                        if (!response.transactionSuccess()) {
                            Message.sendMessage(player, Message.COMMAND_BUFF_NOT_ENOUGH_MONEY);
                            return;
                        }

                        guild.getMembers().stream()
                                .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                                .filter(OfflinePlayer::isOnline)
                                .forEach(member -> {

                                    ((Player) member).addPotionEffect(
                                            new PotionEffect(buff.potion, buff.time,
                                                    buff.amplifier));

                                });
                        guild.sendMessage(Message.COMMAND_BUFF_SUCCESS.replace("{player}",
                                player.getName(), "{buff}", buff.name, "{time}",
                                Integer.toString(buff.time), "{guild}", guild.getName(), "{cost}",
                                Double.toString(buff.cost), "{amplifier}", Integer.toString(buff.amplifier)));
                    }
                }
            }
        }
    }

    public enum GuildBuff {

        HASTE(PotionEffectType.FAST_DIGGING, Material.FEATHER, "haste"),
        SPEED(PotionEffectType.SPEED, Material.SUGAR, "speed"),
        FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, Material.BLAZE_POWDER, "fire-resistance"),
        NIGHT_VISION(PotionEffectType.NIGHT_VISION, Material.REDSTONE_TORCH_ON, "night-vision"),
        INVISIBILITY(PotionEffectType.INVISIBILITY, Material.EYE_OF_ENDER, "invisibility"),
        STRENGTH(PotionEffectType.INCREASE_DAMAGE, Material.DIAMOND_SWORD, "strength"),
        JUMP(PotionEffectType.JUMP, Material.DIAMOND_BOOTS, "jump"),
        WATER_BREATHING(PotionEffectType.WATER_BREATHING, Material.BUCKET, "water-breathing"),
        REGENERATION(PotionEffectType.REGENERATION, Material.EMERALD, "regeneration");


        public final PotionEffectType potion;
        public final Material itemType;
        public final int time;
        public final double cost;
        public final String name;
        public final int amplifier;

        GuildBuff(PotionEffectType potion, Material itemType, String configValueName) {
            this.time =
                    Guilds.getInstance().getConfig().getInt("buff.time." + configValueName) * 20;
            this.cost = Guilds.getInstance().getConfig().getDouble("buff.price." + configValueName);
            this.itemType = itemType;
            this.potion = potion;
            this.name = Guilds.getInstance().getConfig().getString("buff.name." + configValueName);
            this.amplifier = Guilds.getInstance().getConfig()
                    .getInt("buff.amplifier." + configValueName);
        }

        public static GuildBuff get(Material itemType) {

            return Stream.of(values()).filter(it -> it.itemType == itemType).findAny().orElse(null);
        }

    }
}
