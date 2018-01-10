package me.glaremasters.guilds.util;


import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageSentEvent;
import org.bukkit.Bukkit;

public class DiscordSRVUtil {

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        Bukkit.getLogger().info("Received a chat message on Discord: " + event.getMessage());
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void aMessageWasSentInADiscordGuildByTheBot(DiscordGuildMessageSentEvent event) {
        Bukkit.getLogger().info("A message was sent to Discord: " + event.getMessage());
    }

    @Subscribe
    public void accountsLinked(AccountLinkedEvent event) {
        Bukkit.broadcastMessage(
                event.getPlayer().getName() + " just linked their MC account to their Discord user "
                        + event.getUser() + "!");
    }

    @Subscribe
    public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
        event.setProcessedMessage(event.getProcessedMessage()
                .replace("cat", "dog")); // dogs are superior to cats, obviously
    }
/*
    public static void createGuildChannel(Guild guild) {
        github.scarsz.discordsrv.dependencies.jda.core.entities.Guild mainGuild = DiscordSRV
                .getPlugin().getMainGuild();
        //you can make an optional if statement here to check if the text channel already exists
        //also another optional if statement is to check if the category exists (in case someone deleted it after startup)
        mainGuild.getController().createTextChannel(guild.getName()).setParent(mainGuild.getCategoriesByName(
                Guilds.getInstance().getConfig().getString("hooks.discordsrv.category-name"), false)
                .get(0)).queue();
    }
*/
}