package me.glaremasters.guilds.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.utils.IHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildHandler implements IHandler {

    private static final Guilds guild = Guilds.getGuilds();

    private HashMap<String, Guild> guilds;
    private List<GuildRole> roles;

    /**
     * This method enables the GuildHandler and creates the maps needed
     */
    @Override
    public void enable() {
        guilds = new HashMap<>();
        roles = new ArrayList<>();

        initialize();
    }

    /**
     * This method disables the GuildHandler and clears all the data
     */
    @Override
    public void disable() {
        guilds.clear();
        guilds = null;

        roles.clear();
        roles = null;
    }

    /**
     * This method is called in the enable method to load all the values for the guilds
     */
    private void initialize() {
        guild.getDatabase().getGuilds(((result, exception) ->  {
            if (result == null && exception != null) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading Guilds.");
                return;
            }
            if (result != null) guilds = result;

            guilds.values().forEach(this::addGuild);
        }));
    }


    /**
     * This method is used to add a Guild to the map
     * @param guild the guild being added
     */
    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }

    /**
     * This method is used to remove a Guild from the map
     * @param guild the guild being removed
     */
    public void removeGuild(Guild guild) {
        guilds.remove(guild.getName());
    }

    /**
     * This method  gets a list of all the current guilds
     * @return a list of all currently loaded guilds
     */
    public HashMap<String, Guild> getGuilds() {
        return guilds;
    }

    /**
     * This method is used to set all the guilds in the map
     * @param guilds the guilds being added to the map
     */
    public void setGuilds(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    /**
     * The method adds new roles to the guild map
     * @param role the role being added
     */
    public void addRole(GuildRole role) {
        roles.add(role);
    }

    /**
     * THis method gets all the roles in the guild map
     * @return the list of roles
     */
    public List<GuildRole> getRoles() {
        return roles;
    }

}
