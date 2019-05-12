package me.glaremasters.guilds.commands.admin;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.LanguageUpdateUtils;
import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;

/**
 * Created by Glare
 * Date: 4/27/2019
 * Time: 11:17 AM
 */
@AllArgsConstructor
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandAdminUpdateLanguages extends BaseCommand {

    private ActionHandler actionHandler;
    private SettingsManager settingsManager;
    private Guilds guilds;

    /**
     * This command will update all the languages on the server
     * @param issuer the person running the command
     */
    @Subcommand("admin update-language")
    @Description("{@@descriptions.admin-update-languages}")
    @CommandPermission(Constants.ADMIN_PERM)
    public void execute(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            if (!settingsManager.getProperty(PluginSettings.UPDATE_LANGUAGES)) {
                ACFUtil.sneaky(new ExpectationNotMet(Messages.LANGUAGES__CONSOLE_ONLY));
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.LANGUAGES__WARNING);
        actionHandler.addAction(issuer.getIssuer(), new ConfirmAction() {
            @Override
            public void accept() {
                Guilds.newChain().async(() -> {
                    try {
                        LanguageUpdateUtils.updateLanguages();
                    } catch (ZipException | IOException e) {
                        e.printStackTrace();
                    }
                }).sync(() -> guilds.loadLanguages(guilds.getCommandManager())).execute();
                actionHandler.removeAction(issuer.getIssuer());
            }

            @Override
            public void decline() {
                actionHandler.removeAction(issuer.getIssuer());
            }
        });
    }

}