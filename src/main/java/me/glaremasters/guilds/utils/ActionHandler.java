package me.glaremasters.guilds.utils;

import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 6:16 PM
 */
public class ActionHandler implements IHandler {

    private HashMap<CommandSender, ConfirmAction> actions;

    @Override
    public void enable() {
        actions = new HashMap<>();
    }

    @Override
    public void disable() {
        actions.clear();
        actions = null;
    }

    public HashMap<CommandSender, ConfirmAction> getActions() {
        return actions;
    }

    public ConfirmAction addAction(CommandSender sender, ConfirmAction action) {
        actions.put(sender, action);
        return action;
    }

    public void removeAction(CommandSender sender) {
        actions.remove(sender);
    }
}
