package me.glaremasters.guilds.commands.gui;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandMembers extends BaseCommand {

    @Dependency private Guilds guilds;

    @Subcommand("members")
    @Description("{@@descriptions.members}")
    @CommandPermission(Constants.BASE_PERM + "members")
    public void execute (Player player, Guild guild) {
        guilds.getGuiHandler().getInfoMembersGUI().getInfoMembersGUI(guild).show(player);
    }

}
