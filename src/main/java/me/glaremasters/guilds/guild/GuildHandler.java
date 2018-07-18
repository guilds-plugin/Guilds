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

    @Override
    public void enable() {
        guilds = new HashMap<>();
        roles = new ArrayList<>();
    }

    @Override
    public void disable() {
        guilds.clear();
        guilds = null;

        roles.clear();
        roles = null;
    }

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



    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }

    public void removeGuild(Guild guild) {
        guilds.remove(guild.getName());
    }

    public HashMap<String, Guild> getGuilds() {
        return guilds;
    }

    public void setGuilds(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    public void addRole(GuildRole role) {
        roles.add(role);
    }

    public List<GuildRole> getRoles() {
        return roles;
    }

}
