package me.glaremasters.guilds.utils;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 6:16 PM
 */
//todo hmm?
public class ActionHandler {

    private Map<CommandSender, ConfirmAction> actions;

    public ActionHandler() {
        actions = new HashMap<>();
    }

    /**
     * Disable the ActionHandler
     */
    public void disable() {
        actions.clear();
        actions = null;
    }

    /**
     * Get all the current actions
     * @return current actions
     */
    public Map<CommandSender, ConfirmAction> getActions() {
        return actions;
    }

    /**
     * Adds an action to the action list
     * @param sender user
     * @param action action
     */
    public void addAction(CommandSender sender, ConfirmAction action) {
        actions.put(sender, action);
    }

    /**
     * Removes an action from the action list
     * @param sender user
     */
    public void removeAction(CommandSender sender) {
        actions.remove(sender);
    }
}
