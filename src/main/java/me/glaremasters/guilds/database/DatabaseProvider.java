package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface DatabaseProvider {

    /**
     * Loads the guilds from the database
     * @return a list of the loaded guilds
     */
    List<Guild> loadGuilds() throws IOException;

    /**
     * Saves the guild data to the database
     * @param guilds the list of guilds to save
     */
    void saveGuilds(List<Guild> guilds) throws IOException;


}
