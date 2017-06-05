package me.bramhaag.guilds.scoreboard;

import me.bramhaag.guilds.IHandler;
import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.guild.Guild;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class GuildScoreboardHandler implements IHandler {

    private ScoreboardManager manager = Bukkit.getScoreboardManager();
    private Scoreboard board = manager.getNewScoreboard();

    @Override
    public void enable() {
        update();
    }

    @Override
    public void disable() {
        board.getTeams().forEach(Team::unregister);
    }

    public void update() {
        if (!Main.getInstance().getConfig().getBoolean("scoreboard.enable")) {
            return;
        }

        board.getTeams().forEach(Team::unregister);

        for (Guild guild : Main.getInstance().getGuildHandler().getGuilds().values()) {
            Team team = board.registerNewTeam(guild.getName());

            team.setPrefix(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("prefix.format").replace("{prefix}", guild.getPrefix())));

            team.setAllowFriendlyFire(Main.getInstance().getConfig().getBoolean("scoreboard.friendly-fire"));
            team.setCanSeeFriendlyInvisibles(Main.getInstance().getConfig().getBoolean("scoreboard.see-invisible"));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Guild guild = Guild.getGuild(player.getUniqueId());

            if (guild == null) {
                return;
            }

            //noinspection deprecation
            board.getTeam(guild.getName()).addPlayer(player);
        }
    }

    public void show(Player player) {
        if (!Main.getInstance().getConfig().getBoolean("scoreboard.enable")) {
            return;
        }

        player.setScoreboard(board);
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return;
        }

        //noinspection deprecation
        board.getTeam(guild.getName()).addPlayer(player);
        player.setScoreboard(board);
    }

    public void hide(Player player) {
        if (!Main.getInstance().getConfig().getBoolean("scoreboard.enable")) {
            return;
        }

        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            return;
        }

        //noinspection deprecation
        board.getTeam(guild.getName()).removePlayer(player);
    }
}
