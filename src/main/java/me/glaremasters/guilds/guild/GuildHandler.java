package me.glaremasters.guilds.guild;

import me.glaremasters.guilds.IHandler;
import me.glaremasters.guilds.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class GuildHandler implements IHandler {

    private HashMap<String, Guild> guilds;
    private List<GuildRole> roles;

    @Override
    public void enable() {
        guilds = new HashMap<>();
        roles = new ArrayList<>();

        initialize();
    }

    @Override
    public void disable() {
        guilds.clear();
        guilds = null;

        roles.clear();
        roles = null;
    }

    private void initialize() {
        Main.getInstance().getDatabaseProvider().getGuilds((result, exception) -> {
            if (result == null && exception != null) {
                Main.getInstance().getLogger()
                        .log(Level.SEVERE, "An error occurred while loading guilds");
                exception.printStackTrace();
                return;
            }

            if (result != null) {
                guilds = result;
                Main.getInstance().getScoreboardHandler().enable();
            }
        });

        guilds.values().forEach(this::addGuild);

        ConfigurationSection section =
                Main.getInstance().getConfig().getConfigurationSection("roles");
        for (String s : section.getKeys(false)) {
            String path = s + ".permissions.";

            String name = section.getString(s + ".name");

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
            boolean changeHome = section.getBoolean(path + "change-home");
            boolean changeMaster = section.getBoolean(path + "change-master");
            boolean removeGuild = section.getBoolean(path + "remove-guild");
            boolean toggleGuild = section.getBoolean(path + "toggle-guild");
            boolean openVault = section.getBoolean(path + "open-vault");
            boolean transferGuild = section.getBoolean(path + "transfer-guild");
            boolean activateBuff = section.getBoolean(path + "activate-buff");
            boolean upgradeGuild = section.getBoolean(path + "upgrade-guild");
            boolean depositMoney = section.getBoolean(path + "deposit-money");
            boolean withdrawMoney = section.getBoolean(path + "withdraw-money");

            GuildRole role =
                    new GuildRole(name, level, chat, allyChat, invite, kick, promote, demote,
                            addAlly,
                            removeAlly, changePrefix, changeHome, changeMaster, removeGuild,
                            toggleGuild,
                            openVault, transferGuild, activateBuff, upgradeGuild, depositMoney, withdrawMoney);
            roles.add(role);
        }
    }

    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }

    public void removeGuild(Guild guild) {
        guilds.remove(guild.getName());
    }

    public HashMap<String, Guild> getGuilds() {
        return guilds;
    }

    public void setGuilds(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    public void addRole(GuildRole role) {
        roles.add(role);
    }

    public List<GuildRole> getRoles() {
        return roles;
    }
}
