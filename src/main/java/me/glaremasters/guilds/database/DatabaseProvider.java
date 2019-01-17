package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;

import java.util.List;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface DatabaseProvider {

    /**
     * Create a guild
     * @param guild
     */
    @Deprecated
    void createGuild(Guild guild);

    /**
     * Get all the guilds that are on the server
     * @param callback
     */
    @Deprecated
    void getGuilds(Callback<Map<String, Guild>, Exception> callback);


    /**
     * Loads the guilds from the database
     * @return a list of the loaded guilds
     */
    List<Guild> loadGuilds();

    /**
     * Saves the guild data to the database
     * @param guilds the list of guilds to save
     */
    void saveGuilds(List<Guild> guilds);

    /**
     * Update a guild in the database
     * @param guild
     */
    @Deprecated
    void updateGuild(Guild guild);

    /**
     * Remove a guild from the database
     * @param guild
     */
    @Deprecated
    void removeGuild(Guild guild);

    /**
     * Add an ally to another guild
     * @param guild
     * @param targetGuild
     */
    @Deprecated
    void addAlly(Guild guild, Guild targetGuild);

    /**
     * Remove an ally from a guild
     * @param guild
     * @param targetGuild
     */
    @Deprecated
    void removeAlly(Guild guild, Guild targetGuild);

    @Deprecated
    void updateGuild();


}
