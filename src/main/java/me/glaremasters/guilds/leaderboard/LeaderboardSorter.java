package me.glaremasters.guilds.leaderboard;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;

import java.util.*;

/**
 * @author tmux
 */
public final class LeaderboardSorter implements Comparator<Guild> {

	private final Main plugin;

	public LeaderboardSorter(Main plugin) {
		this.plugin = plugin;
	}

	/**
	 * Gets the top <code>x</code> guilds.
	 *
	 * @param limit the amount of entries to limit the result to
	 * @return a set containing <code>x</code> of the top guilds.
	 */
	public Set<Guild> getTopGuilds(int limit) {
		return getTop(limit).keySet();
	}

	/**
	 * Automatically sorts a map of {@link me.glaremasters.guilds.guild.Guild} by their bank balance.
	 *
	 * @param limit amount of entries to limit the {@link TreeMap} to
	 * @return a map containing the results in order from highest to lowest, cutting off all results above the limit, if any
	 */
	public TreeMap<Guild, Double> getTop(int limit) {
		TreeMap<Guild, Double> ordered = getTop();
		while (ordered.size() > limit) {
			ordered.pollFirstEntry();
		}
		return ordered;
	}

	/**
	 * Automatically sorts a map of {@link me.glaremasters.guilds.guild.Guild} by their bank balance.
	 *
	 * @return a map containing the results in order from highest to lowest
	 */
	private TreeMap<Guild, Double> getTop() {
		TreeMap<Guild, Double> ordered = new TreeMap<>(reversed());
		ordered.putAll(getGuilds());
		return ordered;
	}

	/**
	 * Gets all of the guilds and collects them in a {@link Map} ready for use with {@link LeaderboardSorter#getTop()}.
	 *
	 * @return the guilds collected in map format.
	 */
	Map<Guild, Double> getGuilds() {
		return new HashMap<Guild, Double>() {{
			plugin.getGuildHandler().getGuilds().values().forEach(guild -> put(guild, guild.getBankBalance()));
		}};
	}

	/**
	 * Gets the top <code>x</code> guild's balances.
	 *
	 * @param limit the amount of entries to limit the result to
	 * @return a set containing <code>x</code> of the top guild's balances.
	 */
	public Collection<Double> getTopBalances(int limit) {
		return getTop(limit).values();
	}

	/**
	 * Gets the guild with the highest balance.
	 *
	 * @return the guild with the highest balance
	 */
	public Guild getWinningGuild() {
		return getTop().firstKey();
	}

	@Override
	public int compare(Guild one, Guild two) {
		return Double.compare(one.getBankBalance(), two.getBankBalance());
	}

}
