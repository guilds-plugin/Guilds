package me.glaremasters.guilds.guild;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.utils.IHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildHandler implements IHandler {

    private static final Guilds guild = Guilds.getGuilds();

    private HashMap<String, Guild> guilds;
    private List<GuildRole> roles;

    /**
     * This method enables the GuildHandler and creates the maps needed
     */
    @Override
    public void enable() {
        guilds = new HashMap<>();
        roles = new ArrayList<>();

        initialize();
    }

    /**
     * This method disables the GuildHandler and clears all the data
     */
    @Override
    public void disable() {
        guilds.clear();
        guilds = null;

        roles.clear();
        roles = null;
    }

    /**
     * This method is called in the enable method to load all the values for the guilds
     */
    private void initialize() {
        guild.getDatabase().getGuilds(((result, exception) ->  {
            if (result == null && exception != null) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading Guilds.");
                return;
            }
            if (result != null) guilds = result;

            guilds.values().forEach(this::addGuild);

            ConfigurationSection section = Guilds.getGuilds().getConfig().getConfigurationSection("roles");

            for (String s : section.getKeys(false)) {
                String path = s + ".permissions.";
                String name = section.getString(s + ".name");
                String node = section.getString(s + ".permission-node");

                int level = Integer.parseInt(s);
                boolean chat = section.getBoolean(path + "chat");
                boolean allyChat = section.getBoolean(path + "ally-chat");
                boolean invite = section.getBoolean(path + "invite");
                boolean kick = section.getBoolean(path + "kick");
                boolean promote = section.getBoolean(path + "promote");
                boolean demote = section.getBoolean(path + "demote");
                boolean addAlly = section.getBoolean(path + "add-ally");
                boolean removeAlly = section.getBoolean(path + "remove-ally");
                boolean changePrefix = section.getBoolean(path + "change-prefix");
                boolean changeName = section.getBoolean(path + "rename");
                boolean changeHome = section.getBoolean(path + "change-home");
                boolean removeGuild = section.getBoolean(path + "remove-guild");
                boolean changeStatus = section.getBoolean(path + "toggle-guild");
                boolean openVault = section.getBoolean(path + "open-vault");
                boolean transferGuild = section.getBoolean(path + "transfer-guild");
                boolean activateBuff = section.getBoolean(path + "activate-buff");
                boolean upgradeGuild = section.getBoolean(path + "upgrade-guild");
                boolean depositMoney = section.getBoolean(path + "deposit-money");
                boolean withdrawMoney = section.getBoolean(path + "withdraw-money");
                boolean claimLand = section.getBoolean(path + "claim-land");
                boolean unclaimLand = section.getBoolean(path + "unclaim-land");
                boolean canDestroy = section.getBoolean(path + "destroy");
                boolean canPlace = section.getBoolean(path + "place");
                boolean canInteract = section.getBoolean(path + "interact");

                GuildRole role =
                        new GuildRole(name, node, level, chat, allyChat, invite, kick, promote, demote,
                                addAlly, removeAlly, changePrefix, changeName, changeHome, removeGuild,
                                openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
                                withdrawMoney, claimLand, unclaimLand, canDestroy, canPlace, canInteract);
                roles.add(role);
            }

        }));
    }


    /**
     * This method is used to add a Guild to the map
     * @param guild the guild being added
     */
    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }

    /**
     * This method is used to remove a Guild from the map
     * @param guild the guild being removed
     */
    public void removeGuild(Guild guild) {
        guilds.remove(guild.getName());
    }

    /**
     * This method  gets a list of all the current guilds
     * @return a list of all currently loaded guilds
     */
    public HashMap<String, Guild> getGuilds() {
        return guilds;
    }

    /**
     * This method is used to set all the guilds in the map
     * @param guilds the guilds being added to the map
     */
    public void setGuilds(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    /**
     * The method adds new roles to the guild map
     * @param role the role being added
     */
    public void addRole(GuildRole role) {
        roles.add(role);
    }

    /**
     * THis method gets all the roles in the guild map
     * @return the list of roles
     */
    public List<GuildRole> getRoles() {
        return roles;
    }

}
