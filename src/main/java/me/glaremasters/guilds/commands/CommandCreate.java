package me.glaremasters.guilds.commands;

import java.util.List;
import java.util.logging.Level;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.handlers.NameTagEditHandler;
import me.glaremasters.guilds.handlers.TablistHandler;
import me.glaremasters.guilds.handlers.TitleHandler;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandCreate extends CommandBase {

    public CommandCreate() {
        super("create", Guilds.getInstance().getConfig().getString("commands.description.create"),
                "guilds.command.create", false, new String[]{"c"},
                "<name>", 1, 1);
    }

    TitleHandler TitleHandler = new TitleHandler(Guilds.getInstance());
    TablistHandler TablistHandler = new TablistHandler(Guilds.getInstance());
    NameTagEditHandler NTEHandler = new NameTagEditHandler(Guilds.getInstance());

    @Override
    public void execute(Player player, String[] args) {

        if (Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }

        FileConfiguration config = Guilds.getInstance().getConfig();
        int minLength = config.getInt("name.min-length");
        int maxLength = config.getInt("name.max-length");
        String regex = config.getString("name.regex");

        if (args[0].length() < minLength || args[0].length() > maxLength || !args[0]
                .matches(regex)) {
            Message.sendMessage(player, Message.COMMAND_CREATE_NAME_REQUIREMENTS);
            return;
        }

        for (String name : Guilds.getInstance().getGuildHandler().getGuilds().keySet()) {
            if (name.equalsIgnoreCase(args[0])) {
                Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_NAME_TAKEN);
                return;
            }
        }
        if (Guilds.getInstance().getConfig().getBoolean("enable-blacklist")) {
            List<String> blacklist = Guilds.getInstance().getConfig().getStringList("blacklist");

            for (String censor : blacklist) {
                if (args[0].toLowerCase().contains(censor)) {
                    Message.sendMessage(player, Message.COMMAND_ERROR_BLACKLIST);
                    return;
                }
            }
        }

        double requiredMoney = Guilds.getInstance().getConfig().getDouble("Requirement.cost");

        if (Guilds.vault && requiredMoney != -1) {
            if (Guilds.getInstance().getEconomy().getBalance(player) < requiredMoney) {
                Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
                return;
            }

            Message.sendMessage(player, Message.COMMAND_CREATE_MONEY_WARNING
                    .replace("{amount}", String.valueOf(requiredMoney)));
        } else {
            Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
        }

        Guilds.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                Guild guild = new Guild(ChatColor.translateAlternateColorCodes('&', args[0]),
                        player.getUniqueId());

                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                if (event.isCancelled()) {
                    return;
                }

                if (Guilds.getInstance().getConfig().getBoolean("require-money")) {

                    EconomyResponse response = Guilds.getInstance().getEconomy().withdrawPlayer(player, requiredMoney);
                    if (!response.transactionSuccess()) {
                        Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
                        return;
                    }
                }

                Guilds.getInstance().getDatabaseProvider()
                        .createGuild(guild, (result, exception) -> {
                            if (result) {
                                Message.sendMessage(player,
                                        Message.COMMAND_CREATE_SUCCESSFUL
                                                .replace("{guild}", args[0]));

                                Guilds.getInstance().guildStatusConfig
                                        .set(guild.getName(), "Private");
                                Guilds.getInstance().guildTiersConfig.set(guild.getName(), 1);
                                Guilds.getInstance().saveGuildData();
                                for (String perms : guild.getGuildPerms()) {
                                    Guilds.getPermissions().playerAdd(null, player, perms);
                                }

                                TitleHandler.createTitles(player);
                                TablistHandler.addTablist(player);
                                NTEHandler.setTag(player);
                            } else {
                                Message.sendMessage(player, Message.COMMAND_CREATE_ERROR);

                                Guilds.getInstance().getLogger().log(Level.SEVERE, String.format(
                                        "An error occurred while player '%s' was trying to create guild '%s'",
                                        player.getName(), args[0]));
                                if (exception != null) {
                                    exception.printStackTrace();
                                }
                            }
                        });

                Guilds.getInstance().getCommandHandler().removeAction(player);
            }

            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_CREATE_CANCELLED);
                Guilds.getInstance().getCommandHandler().removeAction(player);
            }
        });
    }


}
