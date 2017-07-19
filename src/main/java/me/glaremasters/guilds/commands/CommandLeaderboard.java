package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;

public class CommandLeaderboard extends CommandBase {

    public CommandLeaderboard(String name, String description, String permission,
        boolean allowConsole, String[] aliases, String arguments, int minimumArguments,
        int maximumArguments) {
        super(name, description, permission, allowConsole, aliases, arguments, minimumArguments,
            maximumArguments);
    }
}
