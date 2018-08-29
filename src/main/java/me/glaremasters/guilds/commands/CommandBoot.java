package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 9:52 AM
 */
public class CommandBoot extends CommandBase {

    private Guilds guilds;

    public CommandBoot(Guilds guilds) {
        super("boot", "", "guilds.command.boot", false, new String[]{"kick"}, "<player>", 1, 1);
        this.guilds = guilds;
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canKick()) return;

        OfflinePlayer bootedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (bootedPlayer == null) return;
        if (bootedPlayer.getUniqueId() == null) return;

        GuildMember kickedPlayer = guild.getMember(bootedPlayer.getUniqueId());
        if (kickedPlayer == null) return;

        Guild targetGuild = Guild.getGuild(kickedPlayer.getUniqueId());
        if (targetGuild == null) return;
        if (!guild.getName().equals(targetGuild.getName())) return;

        if (kickedPlayer.equals(guild.getGuildMaster())) return;
        guild.removeMember(kickedPlayer.getUniqueId());

    }

}
