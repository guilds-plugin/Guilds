package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.LeaderboardSorter;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;


public class CommandLeaderboard extends CommandBase {

	private final Main plugin;
	private final LeaderboardSorter sorter;

	public CommandLeaderboard() {
		super("leaderboard", Main.getInstance().getConfig().getString("commands.description.leaderboard"),
				"guilds.command.leaderboard", false, null, null, 0, 0);
		this.plugin = Main.getInstance();
		this.sorter = new LeaderboardSorter(plugin);
	}

	@Override
	public void execute(Player sender, String[] args) {
		int amount = plugin.getConfig().getInt("leaderboard.amount", 10);
		StringBuilder builder = new StringBuilder(plugin.getConfig().getString("leaderboard.header").replace("{amount}", Integer.toString(amount)));
		TreeMap<Guild, Double> top = sorter.getTop(amount);
		if (!top.isEmpty()) {
			for (int i = 1; !top.isEmpty(); i++) {
				Map.Entry<Guild, Double> entry = top.pollFirstEntry();
				String tmp = "\n    #" + i + ": " + entry.getKey().getName() + " (" + entry.getValue() + ")";
				builder.append(tmp);
			}
		} else {
			builder.append(plugin.getConfig().getString("leaderboard.error"));
		}
		Message.sendMessage(sender, builder.toString().trim());
	}

}
