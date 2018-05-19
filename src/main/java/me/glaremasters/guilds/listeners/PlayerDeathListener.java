package me.glaremasters.guilds.listeners;

import static me.glaremasters.guilds.util.ConfigUtil.getInt;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Created by GlareMasters on 8/7/2017.
 */
public class PlayerDeathListener implements Listener {

    private Guilds guilds;
    private Economy economy;

    public PlayerDeathListener(Guilds guilds, Economy economy) {
        this.guilds = guilds;
        this.economy = economy;
    }


    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (killer == null) {
            return;
        }

        Guild guild = Guild.getGuild(player.getUniqueId());
        Guild guild2 = Guild.getGuild(killer.getUniqueId());

        if (guild == null || guild2 == null || guild.equals(guild2)) return;

        if (Guild.areAllies(player.getUniqueId(), killer.getUniqueId())) return;

        if (economy.getBalance(player.getName()) < getInt("reward-on-kill.take-from-killed-player")) return;

        EconomyResponse withdrawPlayer = economy.withdrawPlayer(player, getInt("reward-on-kill.take-from-killed-player"));
        EconomyResponse depositPlayer = economy.depositPlayer(killer, guilds.getConfig().getInt("reward-on-kill.reward"));
        if (depositPlayer.transactionSuccess()) {
            Message.sendMessage(killer, Message.COMMAND_KILLREWARD_KILLER.replace("{amount}", Integer.toString(getInt("reward-on-kill.reward"))));
        }
        if (withdrawPlayer.transactionSuccess()) {
            Message.sendMessage(player, Message.COMMAND_KILLREWARD_PLAYER_WHO_DIED.replace("{amount}", Integer.toString(getInt("reward-on-kill.take-from-killed-player"))));
        }
    }

}
