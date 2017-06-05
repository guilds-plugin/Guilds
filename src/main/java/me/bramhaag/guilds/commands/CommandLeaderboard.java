package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.commands.base.CommandBase;

public class CommandLeaderboard extends CommandBase {

    public CommandLeaderboard(String name, String description, String permission, boolean allowConsole, String[] aliases, String arguments, int minimumArguments, int maximumArguments) {
        super(name, description, permission, allowConsole, aliases, arguments, minimumArguments, maximumArguments);
    }
}
