package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@AllArgsConstructor
@Getter
public class GuildMember {

    private UUID uuid;
    @Setter private GuildRole role;

    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }
}