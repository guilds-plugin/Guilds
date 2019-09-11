package me.glaremasters.guilds.arena;

import co.aikar.commands.ACFBukkitUtil;
import org.bukkit.Location;

import java.util.UUID;

public class Arena {

    private UUID id;
    private String name;
    private String challenger;
    private String defender;
    private transient boolean inUse;

    public Arena(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Arena(UUID id, String name, String challenger, String defender, boolean inUse) {
        this.id = id;
        this.name = name;
        this.challenger = challenger;
        this.defender = defender;
        this.inUse = inUse;
    }

    public static ArenaBuilder builder() {
        return new ArenaBuilder();
    }

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

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getChallenger() {
        return this.challenger;
    }

    public String getDefender() {
        return this.defender;
    }

    public boolean isInUse() {
        return this.inUse;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChallenger(String challenger) {
        this.challenger = challenger;
    }

    public void setDefender(String defender) {
        this.defender = defender;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public static class ArenaBuilder {
        private UUID id;
        private String name;
        private String challenger;
        private String defender;
        private boolean inUse;

        ArenaBuilder() {
        }

        public Arena.ArenaBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public Arena.ArenaBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Arena.ArenaBuilder challenger(String challenger) {
            this.challenger = challenger;
            return this;
        }

        public Arena.ArenaBuilder defender(String defender) {
            this.defender = defender;
            return this;
        }

        public Arena.ArenaBuilder inUse(boolean inUse) {
            this.inUse = inUse;
            return this;
        }

        public Arena build() {
            return new Arena(id, name, challenger, defender, inUse);
        }

        public String toString() {
            return "Arena.ArenaBuilder(id=" + this.id + ", name=" + this.name + ", challenger=" + this.challenger + ", defender=" + this.defender + ", inUse=" + this.inUse + ")";
        }
    }
}
