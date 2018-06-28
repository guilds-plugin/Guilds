package me.glaremasters.guilds.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.utils.IHandler;

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

            }
        }));
    }

}
