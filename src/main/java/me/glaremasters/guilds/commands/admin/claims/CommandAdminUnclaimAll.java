package me.glaremasters.guilds.commands.admin.claims;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

@CommandAlias("%guilds")
public class CommandAdminUnclaimAll extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    @Subcommand("admin unclaim-all")
    @Description("{@@descriptions.admin-unclaim-all}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(Player player) {

        if (!ClaimUtils.isEnable(settingsManager))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED));

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        guildHandler.getGuilds().forEach(g -> {
            if (ClaimUtils.checkAlreadyExist(wrapper, player, g)) {
                ClaimUtils.removeClaim(wrapper, g, player);
            }
        });
        getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__ALL_SUCCESS);
    }
}
