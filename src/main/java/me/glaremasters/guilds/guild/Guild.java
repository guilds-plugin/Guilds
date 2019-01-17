package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Guild {

    public enum Status {
        Public("Public"),
        Private("Private");

        Status(String s) {
        }
    }

    private final UUID id;
    private String name, prefix;
    private GuildMember guildMaster;

    private Location home;
    private Inventory inventory;
    private String textureUrl;
    private Status status;
    private GuildTier tier;
    private double balance = 0;

    private List<GuildMember> members = new ArrayList<>();
    private List<UUID> invitedMembers = new ArrayList<>();

    private List<String> allies = new ArrayList<>();
    private List<String> pendingAllies = new ArrayList<>();

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void addMember(GuildMember guildMember){
        if (members.contains(guildMember)) return;
        members.add(guildMember);
    }

    public void removeMember(GuildMember guildMember){
        members.remove(guildMember);
    }

    public void removeMember(OfflinePlayer player){
        removeMember(getMember(player.getUniqueId()));
    }

    public void removeAlly(Guild guild){
        allies.remove(guild.getName());
    }

    public void addAlly(Guild guild) {
        allies.add(guild.getName());
    }

    public void addPendingAlly(Guild guild){
        pendingAllies.add(guild.getName());
    }

    public void removePendingAlly(Guild guild){
        pendingAllies.remove(guild.getName());
    }

    public int getSize() {
        return members.size();
    }
}

