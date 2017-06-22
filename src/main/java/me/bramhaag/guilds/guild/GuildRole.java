package me.bramhaag.guilds.guild;

import me.bramhaag.guilds.Main;

/*public enum GuildRole {
    MASTER(0, true, true, true, true, true, true, true, true),
    OFFICER(1, true, true, true, true, true, false, false, false),
    VETERAN(2, true, true, false, false, false, false, false, false),
    MEMBER(3, true, false, false, false, false, false, false, false);*/
public class GuildRole {
    private String name;

    private int level;

    private boolean chat;
    private boolean invite;
    private boolean kick;
    private boolean promote;
    private boolean demote;
    private boolean changePrefix;
    private boolean changeHome;
    private boolean changeMaster;
    private boolean removeGuild;
    private boolean addAlly;
    private boolean removeAlly;
    private boolean allyChat;


    GuildRole(String name, int level, boolean chat, boolean allyChat, boolean invite, boolean kick, boolean promote, boolean demote, boolean addAlly, boolean removeAlly, boolean changePrefix, boolean changeHome, boolean changeMaster, boolean removeGuild) {
        this.name = name;
        this.level = level;
        this.chat = chat;
        this.allyChat = allyChat;
        this.invite = invite;
        this.kick = kick;
        this.promote = promote;
        this.demote = demote;
        this.addAlly = addAlly;
        this.removeAlly = removeAlly;
        this.changePrefix = changePrefix;
        this.changeHome = changeHome;
        this.changeMaster = changeMaster;
        this.removeGuild = removeGuild;
    }


    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean canChat() {
        return chat;
    }

    public boolean canInvite() {
        return invite;
    }

    public boolean canKick() {
        return kick;
    }

    public boolean canPromote() {
        return promote;
    }

    public boolean canDemote() {
        return demote;
    }

    public boolean canChangePrefix() {
        return changePrefix;
    }

    public boolean canChangeHome() {
        return changeHome;
    }

    public boolean canChangeMaster() {
        return changeMaster;
    }

    public boolean canRemoveGuild() {
        return removeGuild;
    }

    public boolean canAddAlly() {
        return addAlly;
    }

    public boolean canRemoveAlly() {
        return removeAlly;
    }

    public boolean useAllyChat() {
        return allyChat;
    }



    public static GuildRole getRole(int level) {
        return Main.getInstance().getGuildHandler().getRoles().stream().filter(role -> role.getLevel() == level).findFirst().orElse(null);
    }

    public static GuildRole getLowestRole() {
        GuildRole lowest = null;

        for (GuildRole role : Main.getInstance().getGuildHandler().getRoles()) {
            if (lowest == null || lowest.getLevel() < role.getLevel()) {
                lowest = role;
            }
        }

        return lowest;
    }
}
