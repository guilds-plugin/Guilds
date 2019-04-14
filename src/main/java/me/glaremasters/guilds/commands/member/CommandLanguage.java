package me.glaremasters.guilds.commands.member;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Created by Glare
 * Date: 4/14/2019
 * Time: 12:39 AM
 */
@AllArgsConstructor
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandLanguage extends BaseCommand {

    private Guilds guilds;

    @Subcommand("language")
    @Description("{@@descriptions.language}")
    @CommandPermission(Constants.BASE_PERM + "language")
    @CommandCompletion("@languages")
    public void execute(Player player, @Values("@languages") @Single String language) {
        guilds.getCommandManager().setIssuerLocale(player, Locale.forLanguageTag(language));
        getCurrentCommandIssuer().sendInfo(Messages.LANGUAGES__SET, "{language}", language);
    }

}