package me.bramhaag.guilds.listeners;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.guild.Guild;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Guild guild = Guild.getGuild(e.getPlayer().getUniqueId());
        if (guild == null) {
            return;
        }

        String prefixFormat = ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("prefix.format").replace("{prefix}", guild.getPrefix()));
        String chatFormat = Main.getInstance().getConfig().getString("chat.format").replace("{guild}", prefixFormat);

        e.setFormat(chatFormat);
    }
}
