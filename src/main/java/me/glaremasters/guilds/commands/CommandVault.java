package me.glaremasters.guilds.commands;

import java.io.File;
import java.io.IOException;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/20/2017.
 */
public class CommandVault extends CommandBase {

  private File vaultf;
  private FileConfiguration vault;


  public CommandVault() {
    super("vault", "Open your guild vault!", "guilds.command.vault", false, null,
        null, 0, 0);
  }

  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
      return;
    }
    vaultf = new File(Main.getInstance().getDataFolder(), "data/vault/" + guild.getName() + ".yml");

    if (!vaultf.exists()) {
      vaultf.getParentFile().mkdirs();
      try {
        vaultf.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    vault = new YamlConfiguration();
    try {
      vault.load(vaultf);

    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }

}
