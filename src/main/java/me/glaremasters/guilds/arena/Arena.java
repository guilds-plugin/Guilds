package me.glaremasters.guilds.arena;

import co.aikar.commands.ACFBukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Arena {

    private UUID id;
    private String name;
    private String challenger;
    private String defender;
    private transient boolean inUse;

    /**
     * Get the challenger as a location object
     * @return loc object
     */
    public Location getChallengerLoc() {
        return ACFBukkitUtil.stringToLocation(challenger);
    }

    /**
     * Get the defender as a location object
     * @return loc object
     */
    public Location getDefenderLoc() {
        return ACFBukkitUtil.stringToLocation(defender);
    }

}
