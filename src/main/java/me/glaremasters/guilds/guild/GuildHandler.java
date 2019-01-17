package me.glaremasters.guilds.guild;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildHandler {


    private List<Guild> guilds;
    private List<GuildRole> roles;
    private DatabaseProvider databaseProvider;

    //todo taskchain
    public GuildHandler(DatabaseProvider databaseProvider){
        this.databaseProvider = databaseProvider;
        roles = new ArrayList<>();
        guilds = databaseProvider.loadGuilds();

        ConfigurationSection section = Guilds.getGuilds().getConfig().getConfigurationSection("roles");

        for (String s : section.getKeys(false)) {
            String path = s + ".permissions.";

            roles.add(GuildRole.builder().name(section.getString(s + ".name"))
                    .node(section.getString(s + ".permission-node"))
                    .level(Integer.parseInt(s))
                    .chat(section.getBoolean(path + "chat"))
                    .allyChat(section.getBoolean(path + "ally-chat"))
                    .invite(section.getBoolean(path + "invite"))
                    .kick(section.getBoolean(path + "kick"))
                    .promote(section.getBoolean(path + "promote"))
                    .demote(section.getBoolean(path + "demote"))
                    .addAlly(section.getBoolean(path + "add-ally"))
                    .removeAlly(section.getBoolean(path + "remove-ally"))
                    .changePrefix(section.getBoolean(path + "change-prefix"))
                    .changeName(section.getBoolean(path + "rename"))
                    .changeHome(section.getBoolean(path + "change-home"))
                    .removeGuild(section.getBoolean(path + "remove-guild"))
                    .changeStatus(section.getBoolean(path + "toggle-guild"))
                    .openVault(section.getBoolean(path + "open-vault"))
                    .transferGuild(section.getBoolean(path + "transfer-guild"))
                    .activateBuff(section.getBoolean(path + "activate-buff"))
                    .upgradeGuild(section.getBoolean(path + "upgrade-guild"))
                    .depositMoney(section.getBoolean(path + "deposit-money"))
                    .withdrawMoney(section.getBoolean(path + "withdraw-money"))
                    .claimLand(section.getBoolean(path + "claim-land"))
                    .unclaimLand(section.getBoolean(path + "unclaim-land"))
                    .destroy(section.getBoolean(path + "destroy"))
                    .place(section.getBoolean(path + "place"))
                    .interact(section.getBoolean(path + "interact"))
                    .build());
        }
    }

    public void saveData(){
        databaseProvider.saveGuilds(guilds);
    }


    /**
     * This method is used to add a Guild to the list
     * @param guild the guild being added
     */
    public void addGuild(@NotNull Guild guild) {
        guilds.add(guild);
    }

    /**
     * This method is used to remove a Guild from the list
     * @param guild the guild being removed
     */
    public void removeGuild(@NotNull Guild guild) {
        guilds.remove(guild);
    }

    /**
     * Retrieve a guild by it's name
     * @return the guild object with given name
     */
    public Guild getGuild(@NotNull String name) {
        for (Guild guild : guilds) {
            if (guild.getName().equals(name)) return guild;
        }
        return null;
    }

}
