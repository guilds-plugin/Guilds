package me.glaremasters.guilds.commands.console;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

@CommandAlias("%guilds")
public class CommandUnclaimAll extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private ActionHandler actionHandler;

    @Subcommand("console unclaimall")
    @Description("{@@descriptions.console-unclaim-all}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__CONSOLE_COMMAND);
        }

        if (!ClaimUtils.isEnable(settingsManager))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED));

        getCurrentCommandIssuer().sendInfo(Messages.MIGRATE__WARNING);

        actionHandler.addAction(issuer.getIssuer(), new ConfirmAction() {
            @Override
            public void accept() {
                WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

                guildHandler.getGuilds().forEach(g -> {
                    if (ClaimUtils.checkAlreadyExist(wrapper, g)) {
                        ClaimUtils.removeClaim(wrapper, g);
                    }
                });
                getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__ALL_SUCCESS);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__ALL_CANCELLED);
                actionHandler.removeAction(issuer.getIssuer());
            }
        });
    }
}
