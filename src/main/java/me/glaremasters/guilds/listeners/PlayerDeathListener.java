package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Created by GlareMasters on 8/7/2017.
 */
public class PlayerDeathListener implements Listener {

  @EventHandler
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    Player player = event.getEntity();
    Player killer = player.getKiller();

    if (killer == null) {
      return;
    }

    Guild guild = Guild.getGuild(player.getUniqueId());
    Guild guild2 = Guild.getGuild(killer.getUniqueId());

    if (guild == null || guild2 == null) {
      return;
    }
    if (guild.equals(guild2)) {
      return;
    }
    if (Guild.areAllies(player.getUniqueId(), killer.getUniqueId())) {
      return;
    }
    if (Main.getInstance().getEconomy()
        .getBalance(player.getName()) < Main.getInstance().getConfig()
        .getInt("reward-on-kill.take-from-killed-player")) {
      return;
    }
    EconomyResponse response2 = Main.getInstance().getEconomy()
        .withdrawPlayer(player,
            Main.getInstance().getConfig().getInt("reward-on-kill.take-from-killed-player"));
    EconomyResponse response =
        Main.getInstance().getEconomy()
            .depositPlayer(killer, Main.getInstance().getConfig().getInt("reward-on-kill.reward"));
    if (response.transactionSuccess()) {
      Message.sendMessage(killer, Message.COMMAND_KILLREWARD_KILLER
          .replace("{amount}",
              Integer.toString(Main.getInstance().getConfig().getInt("reward-on-kill.reward"))));
    }
    if (response2.transactionSuccess()) {
      Message.sendMessage(player, Message.COMMAND_KILLREWARD_PLAYER_WHO_DIED.replace("{amount}",
          Integer.toString(Main.getInstance().getConfig().getInt("reward-on-kill.take-from-killed-player"))));
    }
  }

}
