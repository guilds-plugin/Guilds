package me.glaremasters.guilds.guild;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter @Setter
public class Guild {

    private final transient String name;
    private String prefix, home = "", status , inventory = "", texture;
    private int tier = 1;
    private double balance = 0;
    private GuildMember guildMaster;
    private List<GuildMember> members = new ArrayList<>();
    private List<String> allies = new ArrayList<>();
    private List<UUID> invitedMembers = new ArrayList<>();
    private List<String> pendingAllies = new ArrayList<>();

    @Builder
    public Guild(String name, String prefix, String status, String texture, GuildMember guildMaster) {
        this.name = name;
        this.prefix = prefix;
        this.status = status;
        this.texture = texture;
        this.members.add(guildMaster);
        this.guildMaster = guildMaster;
    }

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }
}

