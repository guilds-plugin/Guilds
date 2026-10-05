package me.glaremasters.guilds.tasks;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.WarUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Glare
 * Date: 7/13/2019
 * Time: 6:42 PM
 */
public class GuildWarJoinTask extends BukkitRunnable {

    private final Guilds guilds;
    private int timeLeft;
    private final int readyTime;
    private final List<UUID> players;
    private final String joinMsg;
    private final String readyMsg;
    private final GuildChallenge challenge;
    private final ChallengeHandler challengeHandler;
    private final String notifyType;

    public GuildWarJoinTask(Guilds guilds, int timeLeft, int readyTime, List<UUID> players, String joinMsg, String readyMsg, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.timeLeft = timeLeft;
        this.readyTime = readyTime;
        this.players = players;
        this.joinMsg = joinMsg;
        this.readyMsg = readyMsg;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
        this.notifyType = guilds.getSettingsHandler().getMainConf().getProperty(WarSettings.NOTIFY_TYPE);
    }


    @Override
    public void run() {
        players.forEach(p -> {
            final Player player = Bukkit.getPlayer(p);
            if (player != null) {
                WarUtils.notify(notifyType, joinMsg.replace("{amount}", String.valueOf(timeLeft)), guilds.getAdventure().player(player));
            }
        });
        timeLeft--;
        if (timeLeft == 0) {
            challenge.setJoinble(false);
            if (!challengeHandler.checkEnoughJoined(challenge)) {
                challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
                challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
                challenge.getArena().setInUse(false);
                challengeHandler.removeChallenge(challenge.getId());
                cancel();
                return;
            }
            List<UUID> warReady = Stream.concat(challenge.getChallengePlayers().stream(), challenge.getDefendPlayers().stream()).collect(Collectors.toList());
            new GuildWarReadyTask(guilds, readyTime, warReady, readyMsg, challenge, challengeHandler).runTaskTimer(guilds, 0L, 20L);
            cancel();
        }
    }
}
