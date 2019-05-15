package me.glaremasters.guilds.guis;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandManager;
import lombok.Getter;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.GuildHandler;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 10:58 AM
 */
public class GUIHandler {

    @Getter private BuffGUI buffGUI;
    @Getter private ListGUI listGUI;
    @Getter private InfoGUI infoGUI;
    @Getter private InfoMembersGUI infoMembersGUI;
    @Getter private VaultGUI vaultGUI;

    private Guilds guilds;
    private SettingsManager settingsManager;
    private GuildHandler guildHandler;
    private CommandManager commandManager;

    public GUIHandler(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler, CommandManager commandManager) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
        this.commandManager = commandManager;

        buffGUI = new BuffGUI(this.guilds, this.settingsManager, this.guildHandler, this.commandManager);
        listGUI = new ListGUI(this.guilds, this.settingsManager, this.guildHandler);
        infoGUI = new InfoGUI(this.guilds, this.settingsManager, this.guildHandler);
        infoMembersGUI = new InfoMembersGUI(this.guilds, this.settingsManager, this.guildHandler);
        vaultGUI = new VaultGUI(this.guilds, this.settingsManager, this.guildHandler);
    }

}
