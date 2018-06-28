package me.glaremasters.guilds.database.databases.yaml;

import java.util.HashMap;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class YAML implements DatabaseProvider {

    private Guilds guilds;

    public YAML(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback) {

    }

}
