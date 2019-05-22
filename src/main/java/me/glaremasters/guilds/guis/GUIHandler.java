package me.glaremasters.guilds.guis;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandManager;
import lombok.Getter;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
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
    private CooldownHandler cooldownHandler;

    public GUIHandler(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler, CommandManager commandManager, CooldownHandler cooldownHandler) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
        this.commandManager = commandManager;
        this.cooldownHandler = cooldownHandler;

        buffGUI = new BuffGUI(this.guilds, this.settingsManager, this.guildHandler, this.commandManager, this.cooldownHandler);
        listGUI = new ListGUI(this.guilds, this.settingsManager, this.guildHandler);
        infoGUI = new InfoGUI(this.guilds, this.settingsManager, this.guildHandler, this.cooldownHandler);
        infoMembersGUI = new InfoMembersGUI(this.guilds, this.settingsManager, this.guildHandler);
        vaultGUI = new VaultGUI(this.guilds, this.settingsManager, this.guildHandler);
    }

}
