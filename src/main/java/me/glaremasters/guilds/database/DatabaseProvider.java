package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;

import java.util.HashMap;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 1:47 AM
 */
public interface DatabaseProvider {

    void initialize();

    void createGuild(Guild guild);

    void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

    void updateGuild(Guild guild);

    void removeGuild(Guild guild);

    void addAlly(Guild guild, Guild targetGuild);

    void removeAlly(Guild guild, Guild targetGuild);


}
