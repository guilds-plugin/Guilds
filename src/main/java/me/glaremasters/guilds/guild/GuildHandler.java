package me.glaremasters.guilds.guild;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.CommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.messages.Messages;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class GuildHandler {


    private List<Guild> guilds;
    private List<GuildRole> roles;
    private List<GuildTier> tiers;

    private final DatabaseProvider databaseProvider;
    private final CommandManager commandManager;
    private final Permission permission;

    //todo taskchain
    public GuildHandler(DatabaseProvider databaseProvider, CommandManager commandManager, Permission permission, YamlConfiguration config){
        this.databaseProvider = databaseProvider;
        this.commandManager = commandManager;
        this.permission = permission;

        roles = new ArrayList<>();
        tiers = new ArrayList<>();
        guilds = databaseProvider.loadGuilds();

        //GuildRoles objects
        ConfigurationSection roleSection = config.getConfigurationSection("roles");

        for (String s : roleSection.getKeys(false)) {
            String path = s + ".permissions.";

            roles.add(GuildRole.builder().name(roleSection.getString(s + ".name"))
                    .node(roleSection.getString(s + ".permission-node"))
                    .level(Integer.parseInt(s))
                    .chat(roleSection.getBoolean(path + "chat"))
                    .allyChat(roleSection.getBoolean(path + "ally-chat"))
                    .invite(roleSection.getBoolean(path + "invite"))
                    .kick(roleSection.getBoolean(path + "kick"))
                    .promote(roleSection.getBoolean(path + "promote"))
                    .demote(roleSection.getBoolean(path + "demote"))
                    .addAlly(roleSection.getBoolean(path + "add-ally"))
                    .removeAlly(roleSection.getBoolean(path + "remove-ally"))
                    .changePrefix(roleSection.getBoolean(path + "change-prefix"))
                    .changeName(roleSection.getBoolean(path + "rename"))
                    .changeHome(roleSection.getBoolean(path + "change-home"))
                    .removeGuild(roleSection.getBoolean(path + "remove-guild"))
                    .changeStatus(roleSection.getBoolean(path + "toggle-guild"))
                    .openVault(roleSection.getBoolean(path + "open-vault"))
                    .transferGuild(roleSection.getBoolean(path + "transfer-guild"))
                    .activateBuff(roleSection.getBoolean(path + "activate-buff"))
                    .upgradeGuild(roleSection.getBoolean(path + "upgrade-guild"))
                    .depositMoney(roleSection.getBoolean(path + "deposit-money"))
                    .withdrawMoney(roleSection.getBoolean(path + "withdraw-money"))
                    .claimLand(roleSection.getBoolean(path + "claim-land"))
                    .unclaimLand(roleSection.getBoolean(path + "unclaim-land"))
                    .destroy(roleSection.getBoolean(path + "destroy"))
                    .place(roleSection.getBoolean(path + "place"))
                    .interact(roleSection.getBoolean(path + "interact"))
                    .build());
        }

        //GuildTier objects
        ConfigurationSection tierSection = config.getConfigurationSection("tiers");
        for (String key : tierSection.getKeys(false)){
            tiers.add(GuildTier.builder()
                    .level(tierSection.getInt(key + ".level"))
                    .name(tierSection.getString(key + ".name"))
                    .cost(tierSection.getDouble(key + ".cost"))
                    .maxMembers(tierSection.getInt(key + ".max-members"))
                    .mobXpMultiplier(tierSection.getInt(key + ".mob-xp-multiplier"))
                    .damageMultiplier(tierSection.getInt(key + ".damage-multiplier"))
                    .maxBankBalance(tierSection.getDouble(key + ".max-bank-balance"))
                    .membersToRankup(tierSection.getInt(key + ".members-to-rankup"))
                    .permissions(tierSection.getStringList(key + ".permissions"))
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
        return guilds.stream().filter(guild -> guild.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Retrieve a guild by a player
     * @return the guild object by player
     */
    public Guild getGuild(@NotNull Player p){
        return guilds.stream().filter(guild -> guild.getMember(p.getUniqueId()) != null).findFirst().orElse(null);
    }


    //todo just added need to change it up and fix shit V

    //what is this used for?
    @Deprecated
    public Guild getGuild2(String name) {
        return Guilds.getGuilds().getGuildHandler().getGuilds().values().stream().filter(guild -> getNameColorless(guild).equalsIgnoreCase(guild.getName())).findAny().orElse(null);
    }
    @Deprecated
    public String getNameColorless(Guild guild) {
        return ACFBukkitUtil.removeColors(guild.getName());
    }


//    READ PLEASE GLARE.
//    todo change this, guilds should not be grabbing the config constantly but rather know this information on startup.
//    public String getTierName(Guild guild) {
//        return guilds.getConfig().getString("tier" + guild.getTier() + ".name");
//    }
//
//    public int getTierCost(Guild guild) {
//        if (guild.getTier() >= guilds.getConfig().getInt("max-number-of-tiers")) return 0;
//        int newTier = guild.getTier() + 1;
//        return guilds.getConfig().getInt("tier" + newTier + ".cost");
//    }
//
//    public int getMaxMembers(Guild guild) {
//        return guilds.getConfig().getInt("tier" + guild.getTier() + ".max-members");
//    }
//
//    public int getMembersToRankup(Guild guild) {
//        return guilds.getConfig().getInt("tier" + guild.getTier() + ".members-to-rankup");
//    }
//
//    public double getExpMultiplier(Guild guild) {
//        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".mob-xp-multiplier");
//    }
//
//    public List<String> getGuildPerms(Guild guild) {
//        return guilds.getConfig().getStringList("tier" + guild.getTier() + ".permissions");
//    }
//
//    public double getDamageMultiplier(Guild guild) {
//        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".damage-multiplier");
//    }
//
//    public double getMaxBankBalance(Guild guild) {
//        return guilds.getConfig().getDouble("tier" + guild.getTier() + ".max-bank-balance");
//    }
//
//    public int getMaxTier() {
//        return guilds.getConfig().getInt("max-number-of-tiers");
//    }
//
//
//
//    public void removeGuildPerms(Guild guild) {
//        guild.getMembers().forEach(member ->  {
//            OfflinePlayer op = Bukkit.getOfflinePlayer(member.getUniqueId());
//            getGuildPerms(guild).forEach(perm -> permission.playerRemove(null, op, perm));
//        });
//    }
//
//    public void removeGuildPerms(Guild guild, OfflinePlayer player) {
//        getGuildPerms(guild).forEach(perm -> permission.playerRemove(player, perm));
//    }
//
//    private List<String> getGuildPerms(Guild guild) {
//        return guilds.getConfig().getStringList("tier" + guild.getTier() + ".permissions");
//    }
//
//    public void addGuildPerms(Guild guild) {
//        guild.getMembers().forEach(member ->  {
//            OfflinePlayer op = Bukkit.getOfflinePlayer(member.getUniqueId());
//            getGuildPerms(guild).forEach(perm -> permission.playerAdd(op, perm));
//        });
//    }
//
//    public void addGuildPerms(Guild guild, OfflinePlayer player) {
//        getGuildPerms(guild).forEach(perm -> permission.playerAdd(player, perm));
//    }

    public void sendMessage(Guild guild, Messages key, String... replacements) {
        guild.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUuid())).filter(Objects::nonNull).forEach(p -> commandManager.getCommandIssuer(p).sendInfo(key, replacements));
    }

    public void addAlly(Guild guild, Guild targetGuild) {
        guild.addAlly(targetGuild);
        targetGuild.addAlly(guild);
        guild.removePendingAlly(targetGuild);
        targetGuild.removePendingAlly(guild);
    }


    public void removeAlly(Guild guild, Guild targetGuild) {
        guild.removeAlly(targetGuild);
        targetGuild.removeAlly(guild);
    }

    public int getGuildsSize(){
        return guilds.size();
    }

}
