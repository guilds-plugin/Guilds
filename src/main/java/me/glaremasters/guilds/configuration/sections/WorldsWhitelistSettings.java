package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class WorldsWhitelistSettings implements SettingsHolder {

    @Comment({"Do we want to enable worlds whitelist?", "This allow pvp with allies and disable teleport to war in other worlds."})
    public static final Property<Boolean> WORLDS_WHITELIST =
            newProperty("worlds-whitelist.enabled", false);
    
    @Comment("What worlds would you like to whitelisted")
    public static final Property<List<String>> WHITELISTED_WORLDS =
            newListProperty("worlds-whitelist.worlds", "world", "world_nether", "world_the_end");
    
    private WorldsWhitelistSettings() {
    }
}
