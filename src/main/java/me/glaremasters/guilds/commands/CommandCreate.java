package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.*;
import static me.glaremasters.guilds.utils.DiscordUtils.sendEmbed;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class CommandCreate extends CommandBase {

    public CommandCreate() {
        super("create", "",
                "guilds.command.create", false, new String[]{"c"},
                "<name>", 1, 1);
    }

    @Override
    public void execute(Player player,String[] args) {
        Guild guild = new Guild(color(args[0]), player.getUniqueId());
        GuildCreateEvent event = new GuildCreateEvent(player, guild);
        if (event.isCancelled()) return;
        Guilds.getGuilds().getDatabase().createGuild(guild);
        if (!getBoolean("discord.enabled")) return;
        sendEmbed(getString("discord.webhook-url"), getString("discord.guild-create.description").replace("{player}", player.getName()).replace("{guild}", guild.getName()), getString("discord.guild-create.username"), getString("discord.avatar-url"));
    }

}
