package me.glaremasters.guilds.database;

import java.util.HashMap;
import me.glaremasters.guilds.guild.Guild;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public interface DatabaseProvider {

    void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

}
