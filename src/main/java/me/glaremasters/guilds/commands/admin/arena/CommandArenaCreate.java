package me.glaremasters.guilds.commands.admin.arena;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandArenaCreate extends BaseCommand {

    @Subcommand("arena create")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-create}")
    @Syntax("<name>")
    public void execute(Player player) {

    }

}
