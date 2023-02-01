package me.glaremasters.guilds.utils;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.conf.GuildBuffSettings;
import me.glaremasters.guilds.configuration.SettingsHandler;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class BstatsUtils {

    public void initialize(final Guilds guilds, final GuildHandler guildHandler, final SettingsHandler settingsHandler) {
        Metrics metrics = new Metrics(guilds, 881);
        metrics.addCustomChart(new SingleLineChart("guilds", () -> guildHandler.getGuildsSize()));
        metrics.addCustomChart(new SingleLineChart("tiers", () -> guildHandler.getTiers().size()));
        metrics.addCustomChart(new SingleLineChart("roles", () -> guildHandler.getRoles().size()));
        metrics.addCustomChart(new SingleLineChart("buffs", () -> settingsHandler.getBuffConf().getProperty(GuildBuffSettings.BUFFS).size()));
        metrics.addCustomChart(new SimplePie("language", () -> settingsHandler.getMainConf().getProperty(PluginSettings.MESSAGES_LANGUAGE)));
    }
}
