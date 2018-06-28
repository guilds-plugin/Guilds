package me.glaremasters.guilds.database.databases.yaml;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseProvider;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class YAML implements DatabaseProvider {

    private Guilds guilds;

    public YAML(Guilds guilds) {
        this.guilds = guilds;
    }

}
