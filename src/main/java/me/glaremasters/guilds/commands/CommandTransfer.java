package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 10:23 AM
 */
public class CommandTransfer extends CommandBase {

    private Guilds guilds;

    public CommandTransfer(Guilds guilds) {
        super(guilds, "transfer",false, null, "<name>", 1, 1);
        this.guilds = guilds;
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canTransfer()) return;

        Player transferPlayer = Bukkit.getPlayerExact(args[0]);
        if (transferPlayer == null) return;

        GuildMember oldMaster = guild.getMember(player.getUniqueId());
        GuildMember newMaster = guild.getMember(transferPlayer.getUniqueId());

        if (newMaster == null) return;
        if (newMaster.getRole() != 1) return;

        GuildRole newRole, oldRole;
        int currentLevel = newMaster.getRole();
        newRole = GuildRole.getRole(currentLevel - 1);
        oldRole = GuildRole.getRole(currentLevel + 1);
        if (oldMaster.getRole() == 0) {
            oldMaster.setRole(oldRole);
            newMaster.setRole(newRole);
            guild.updateGuild("", guild.getName(), Guild.getGuild(guild.getName()).getName());
        }
    }

}
