package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
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
    EconomyResponse response =
        Main.getInstance().getEconomy()
            .depositPlayer(killer, Main.getInstance().getConfig().getInt("reward-on-kill.reward"));
    if (response.transactionSuccess()) {
      killer.sendMessage("You just killed someone, here's some money.");
    }
  }

}
