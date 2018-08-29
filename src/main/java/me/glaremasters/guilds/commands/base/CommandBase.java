package me.glaremasters.guilds.commands.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.glaremasters.guilds.Guilds;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public abstract class CommandBase {

    private Guilds guilds;
    private String name;
    private String description;
    private String permission;

    private boolean allowConsole;

    private List<String> aliases;
    private String arguments;

    private int minArgs;
    private int maxArgs;

    public CommandBase(Guilds guilds, String name, boolean allowConsole, String[] aliases, String arguments, int minArgs, int maxArgs) {
        this.guilds = guilds;
        this.name = name;
        this.description = color(guilds.getConfig().getString("commands.description." + name));
        this.permission = "guilds.commands." + name;

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
