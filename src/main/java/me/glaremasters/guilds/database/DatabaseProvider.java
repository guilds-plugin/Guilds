package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;

import java.util.HashMap;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface DatabaseProvider {

    /**
     * Start the database
     */
    void initialize();

    /**
     * Create a guild
     * @param guild
     */
    void createGuild(Guild guild);

    /**
     * Get all the guilds that are on the server
     * @param callback
     */
    void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

    /**
     * Update a guild in the database
     * @param guild
     */
    void updateGuild(Guild guild);

    /**
     * Remove a guild from the database
     * @param guild
     */
    void removeGuild(Guild guild);

    /**
     * Add an ally to another guild
     * @param guild
     * @param targetGuild
     */
    void addAlly(Guild guild, Guild targetGuild);

    /**
     * Remove an ally from a guild
     * @param guild
     * @param targetGuild
     */
    void removeAlly(Guild guild, Guild targetGuild);


}
