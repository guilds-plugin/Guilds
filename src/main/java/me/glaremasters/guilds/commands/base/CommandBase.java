package me.glaremasters.guilds.commands.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public abstract class CommandBase {

    private String name;
    private String description;
    private String permission;

    private boolean allowConsole;

    private List<String> aliases;
    private String arguments;

    private int minArgs;
    private int maxArgs;

    public CommandBase(String name, String description, String permission, boolean allowConsole,
            String[] aliases, String arguments, int minArgs, int maxArgs) {
        this.name = name;
        this.description = description;
        this.permission = permission;

        this.allowConsole = allowConsole;

        this.aliases = aliases == null ? new ArrayList<>() : Arrays.asList(aliases);
        this.arguments = arguments == null ? "" : arguments;

        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public void execute(CommandSender sender, String[] args) { throw new UnsupportedOperationException("Method not implemented"); }
    public void execute(Player sender, String[] args) { throw new UnsupportedOperationException("Method not implemented"); }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public boolean allowConsole() {
        return allowConsole;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getArguments() {
        return arguments;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }
}
