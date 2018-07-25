package me.glaremasters.guilds.utils;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

import java.util.Arrays;

/**
 * Created by GlareMasters
 * Date: 7/24/2018
 * Time: 11:19 PM
 */
public class DiscordUtils {

    public static void sendEmbed(String webhookUrl, String description, String username, String avatarUrl) {
        TemmieWebhook webhook = new TemmieWebhook(webhookUrl);
        DiscordEmbed.DiscordEmbedBuilder de = DiscordEmbed.builder();
        de.description(description);
        DiscordMessage dm = DiscordMessage.builder().username(username).content("").avatarUrl(avatarUrl).embeds(Arrays.asList(de.build())).build();
        webhook.sendMessage(dm);
    }

}
