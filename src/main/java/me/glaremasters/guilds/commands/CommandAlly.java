package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildAddAllyEvent;
import me.glaremasters.guilds.api.events.GuildRemoveAllyEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:49 PM
 */
@CommandAlias("guild|guilds")
public class CommandAlly extends BaseCommand {

    @Dependency private Guilds guilds;

    @Subcommand("ally list")
    @Description("{@@descriptions.ally-list}")
    @CommandPermission("guilds.command.ally")
    public void onAllyList(Player player, Guild guild) {
        if (guild.getAllies().size() < 1) {
            // send message saying no allies
            return;
        }
        // send ally message heading
        for (String allies : guild.getAllies()) {
            player.sendMessage(allies);
        }
    }

    @Subcommand("ally accept")
    @Description("{@@descriptions.ally-accept}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyAccept(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canAddAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = Guild.getGuild(name);
        if (targetGuild == null) return;
        if (!guild.getPendingAllies().contains(targetGuild.getName())) return;
        guild.removePendingAlly(targetGuild);
        guild.addAlly(targetGuild);
        targetGuild.addAlly(guild);
        // send message to both guilds saying they are now allies
    }

    @Subcommand("ally decline")
    @Description("{@@descriptions.ally-decline}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyDeclince(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canRemoveAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = Guild.getGuild(name);
        if (targetGuild == null) return;

        if (!guild.getPendingAllies().contains(targetGuild.getName())) return;
        guild.removePendingAlly(targetGuild);
        // send message to both guilds saying the ally invite was declined
    }

    @Subcommand("ally add")
    @Description("{@@descriptions.ally-add}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyAdd(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canAddAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = Guild.getGuild(name);
        if (targetGuild == null) return;

        if (guild.getAllies().contains(targetGuild.getName()) || guild.getName().equalsIgnoreCase(targetGuild.getName())) return;

        GuildAddAllyEvent event = new GuildAddAllyEvent(player, guild, targetGuild);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // send message to user saying their request was sent

        // send message to target guild saying they've been sent a request

        targetGuild.addPendingAlly(guild);
    }

    @Subcommand("ally remove")
    @Description("{@@descriptions.ally-remove}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyRemove(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canRemoveAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = Guild.getGuild(name);
        if (targetGuild == null) return;

        if (!guild.getAllies().contains(targetGuild.getName())) return;

        GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(player, guild, targetGuild);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guild.removeAlly(targetGuild);
        targetGuild.removeAlly(guild);

        // send message to both guilds saying they have been removed from allies
    }

}
