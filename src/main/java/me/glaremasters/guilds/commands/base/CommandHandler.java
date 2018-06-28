package me.glaremasters.guilds.commands.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import me.glaremasters.guilds.utils.ConfirmAction;
import me.glaremasters.guilds.utils.IHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class CommandHandler implements CommandExecutor, TabCompleter, IHandler {

    private List<CommandBase> commands;
    private HashMap<CommandSender, ConfirmAction> actions;

    @Override
    public void enable() {
        commands = new ArrayList<>();
        actions = new HashMap<>();
    }

    @Override
    public void disable() {
        commands.clear();
        commands = null;

        actions.clear();
        actions = null;
    }

    public void register(CommandBase command) { commands.add(command); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("guild")) return true;
        if (args.length == 0 || args[0].isEmpty()) {
            getCommand("help").execute(sender, args);
            return true;
        }

        for (CommandBase command : commands) {
            if (!command.getName().equalsIgnoreCase(args[0]) && !command.getAliases().contains(args[0].toLowerCase())) continue;
            if (!command.allowConsole() && !(sender instanceof Player)) {
                // Possibly send a message here saying console isn't allowed for it
                return true;
            }
            if(!sender.hasPermission(command.getPermission())) {
                // Possibly send a message here saying no permission for this command
                return true;
            }
            args = Arrays.copyOfRange(args, 1, args.length);

            if ((command.getMinArgs() != -1 && command.getMinArgs() > args.length) || (command.getMaxArgs() != -1 && command.getMaxArgs() < args.length)) {
                // Possibly send a message here about an issue with the args provided
                return true;
            }
            if (command.allowConsole()) {
                command.execute(sender, args);
                return true;
            } else {
                command.execute((Player) sender, args);
                return true;
            }
        }
        // Possibly send a message here about command not being found
        return true;
    }

    private CommandBase getCommand(String name) {
        return commands.stream().filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<CommandBase> getCommands() { return commands; }

    public HashMap<CommandSender, ConfirmAction> getActions() { return actions; }

    public ConfirmAction addAction(CommandSender sender, ConfirmAction action) {
        actions.put(sender, action);
        return action;
    }

    public void removeAction(CommandSender sender) { actions.remove(sender); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("guild")) {
            if (args.length == 1) {
                List<String> commandNames = new ArrayList<>();
                if (!args[0].equals("")) {
                    for (String commandName : commands.stream().map(CommandBase::getName).collect(Collectors.toList())) {
                        if (!commandName.startsWith(args[0].toLowerCase())) continue;
                        commandNames.add(commandName);
                    }
                } else {
                    commandNames = commands.stream().map(CommandBase::getName).collect(Collectors.toList());
                }
                Collections.sort(commandNames);
                return commandNames;
            }
        }
        return null;
    }

}
