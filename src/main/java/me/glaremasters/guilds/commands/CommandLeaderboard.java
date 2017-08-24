package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.LeaderboardSorter;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author tmux
 */
public class CommandLeaderboard extends CommandBase {

    private final LeaderboardSorter sorter;

    public CommandLeaderboard(Main plugin) {
        super("leaderboard", plugin.getConfig().getString("commands.description.leaderboard"),
                "guilds.command.leaderboard", false, null, null, 0, 1);
        this.sorter = new LeaderboardSorter(plugin);
    }

    @Override // This is just template text, is this even the correct class?
    public void execute(Player sender, String[] args) {
        int limit = args.length == 1 ? Integer.parseInt(args[0]) : 10;
        StringBuilder builder = new StringBuilder("This is some magical list of the top guilds.");
        TreeMap<Guild, Double> top = sorter.getTop(limit);
        if (!top.isEmpty()) {
            for (int i = 1; i <= limit; i++) {
                Map.Entry<Guild, Double> entry = top.pollFirstEntry();
                String tmp = "\n&r  #" + i + ": " + entry.getKey().getName() + " (" + entry.getValue() + ")";
                builder.append(tmp);
            }
        } else {
            builder.append("No guilds could be found?!");
        }
        Message.sendMessage(sender, builder.toString().trim());
    }

}
