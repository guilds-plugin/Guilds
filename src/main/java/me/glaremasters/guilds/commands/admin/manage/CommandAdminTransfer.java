package me.glaremasters.guilds.commands.admin.manage;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandAdminTransfer extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Transfer a guild to another player
     * @param player The player executing the command
     * @param name the name of the guild that's being modified
     * @param newMaster The new master of the guild
     */
    @Subcommand("admin transfer")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.admin-transfer}")
    @CommandCompletion("@guilds @members-admin")
    @Syntax("<guild> <new master>")
    public void execute(Player player, @Values("@guilds") @Single String name, @Values("@members-admin") @Single String newMaster) {

        Guild guild = guildHandler.getGuild(name);

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        OfflinePlayer transferPlayer = Bukkit.getOfflinePlayer(newMaster);

        if (transferPlayer == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND));
        }

        if (guild.getGuildMaster().getUuid().equals(transferPlayer.getUniqueId())) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__TRANSFER_SAME_PERSON));
        }

        guild.transferGuildAdmin(transferPlayer, guildHandler);

        getCurrentCommandIssuer().sendInfo(Messages.TRANSFER__SUCCESS);
    }

}
