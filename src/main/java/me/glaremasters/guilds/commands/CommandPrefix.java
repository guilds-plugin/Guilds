package me.glaremasters.guilds.commands;

import com.nametagedit.plugin.NametagEdit;
import java.util.List;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.TitleHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandPrefix extends CommandBase {


    public CommandPrefix() {
        super("prefix", Main.getInstance().getConfig().getString("commands.description.prefix"),
                "guilds.command.prefix", false, null,
                "<new prefix>", 1, 1);
    }
    TitleHandler th = new TitleHandler(Main.getInstance());
    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangePrefix()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        FileConfiguration config = Main.getInstance().getConfig();
        if (!args[0].matches(Main.getInstance().getConfig().getString("prefix.regex"))) {
            Message.sendMessage(player, Message.COMMAND_PREFIX_REQUIREMENTS);
            return;
        }
        if (Main.getInstance().getConfig().getBoolean("enable-blacklist")) {
            List<String> blacklist = Main.getInstance().getConfig().getStringList("blacklist");

            for (String censor : blacklist) {
                if (args[0].toLowerCase().contains(censor)) {
                    Message.sendMessage(player, Message.COMMAND_ERROR_BLACKLIST);
                    return;
                }
            }
        }

        Message.sendMessage(player, Message.COMMAND_PREFIX_SUCCESSFUL);
        guild.updatePrefix(ChatColor.translateAlternateColorCodes('&', args[0]));

        th.prefixTitles(player);

        String name = config.getBoolean("tablist-use-display-name") ? player
                .getDisplayName() : player.getName();
        player.setPlayerListName(
                ChatColor.translateAlternateColorCodes('&',
                        config.getString("tablist")
                                .replace("{guild}", guild.getName())
                                .replace("{prefix}", guild.getPrefix())
                                + name));

        if (config.getBoolean("hooks.nametagedit")) {
            NametagEdit.getApi()
                    .setPrefix(player, ChatColor.translateAlternateColorCodes('&',
                            config
                                    .getString("nametagedit.name")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())));
        }


    }
}
