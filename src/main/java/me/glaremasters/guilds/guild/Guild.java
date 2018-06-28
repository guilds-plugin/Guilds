package me.glaremasters.guilds.guild;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.Inventory;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class Guild {

    private String name;
    private String prefix;
    // private List<GuildMember> members;
    private List<String> allies;
    private List<UUID> invitedMembers;
    private List<String> pendingAllies;
    private Inventory inventory;

    public Guild(String name) {
        this.name = name;
        // this.members = new ArrayList<>();
        this.invitedMembers = new ArrayList<>();
    }

    public Guild(String name, UUID master) {
        this.name = name;
        // this.prefix=name.substring(0, getInt("prefix.max-length") > name.length() ? name.length() : getInt("prefix.max-length");
        // this.members = new ArrayList<>();
        // this.members.add(new GuildMember(master, 0));
        this.invitedMembers = new ArrayList<>();
        this.pendingAllies = new ArrayList<>();
    }



}
