package me.glaremasters.guilds.guild;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter @Setter
public class Guild {

    public enum Status {
        Public,
        Private
    }

    private final UUID id;
    private String name, prefix;
    private GuildMember guildMaster;

    private Location home;
    private String home = "",  inventory = "", texture;
    private Status status;
    private GuildTier tier;
    private double balance = 0;

    private List<GuildMember> members = new ArrayList<>();
    private List<UUID> invitedMembers = new ArrayList<>();

    private List<String> allies = new ArrayList<>();
    private List<String> pendingAllies = new ArrayList<>();

    @Builder
    public Guild(UUID id, String name, String prefix, Status status, String texture, GuildMember guildMaster) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.status = status;
        this.texture = texture;
        this.guildMaster = guildMaster;
        addMember(guildMaster);
    }

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

