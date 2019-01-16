package me.glaremasters.guilds.guild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter @Setter @NoArgsConstructor
public class Guild {

    private transient String name;

    private String prefix, home = "", status , inventory = "", texture;

    private int tier = 1;

    private double balance = 0D;

    private List<GuildMember> members = new ArrayList<>();

    private List<String> allies = new ArrayList<>();

    private List<UUID> invitedMembers = new ArrayList<>();

    private List<String> pendingAllies = new ArrayList<>();

    public Guild(String name) {
        this.name = name;
    }

    public Guild(String name, String prefix, String status, String texture, UUID master) {
        this.name = name;
        this.prefix = prefix;
        this.status = status;
        this.texture = texture;
        this.members.add(new GuildMember(master, 0));
    }

    public GuildMember getGuildMaster() {
        return this.members.stream().filter(member -> member.getRole() == 0).findFirst().orElse(null);
    }

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

}
