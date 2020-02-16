/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by GlareMasters
 * Date: 11/12/2018
 * Time: 12:27 AM
 */
public class EssentialsChatListener implements Listener {

    private GuildHandler guildHandler;

    public EssentialsChatListener(GuildHandler guildHandler) {
        this.guildHandler = guildHandler;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        String message = event.getFormat();

        if (guild == null) {
            String regex = "(\\{GUILD(?:.*?)})";
            String formatted = "(\\\\{GUILD_FORMATTED\\\\})";

            event.setFormat(message.replace(formatted, guildHandler.getFormattedPlaceholder(player)));
            event.setFormat(message.replaceAll(regex, ""));
            return;
        }

        message = message
                .replace("{GUILD}", guild.getName())
                .replace("{GUILD_PREFIX}", guild.getPrefix())
                .replace("{GUILD_MASTER}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUuid()).getName())
                .replace("{GUILD_STATUS}", guild.getStatus().name())
                .replace("{GUILD_MEMBER_COUNT}",  String.valueOf(guild.getSize()))
                .replace("{GUILD_MEMBERS_ONLINE}", String.valueOf(guild.getOnlineMembers().size()))
                .replace("{GUILD_ROLE}", guild.getMember(player.getUniqueId()).getRole().getName())
                .replace("{GUILD_FORMATTED}", guildHandler.getFormattedPlaceholder(player))
                .replace("{GUILD_CHALLENGE_WINS}", String.valueOf(guild.getGuildScore().getWins()))
                .replace("{GUILD_CHALLENGE_LOSES}", String.valueOf(guild.getGuildScore().getLoses()));

        event.setFormat(message);
    }
}
