/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.acf;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.LoggingUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ACFHandler {

    private Guilds guilds;

    public ACFHandler(Guilds guilds, PaperCommandManager commandManager) {
        this.guilds = guilds;
        commandManager.usePerIssuerLocale(true, false);
        commandManager.enableUnstableAPI("help");

        registerLanguages(guilds, commandManager);
        registerCustomContexts(commandManager, guilds.getGuildHandler(), guilds.getArenaHandler());
        registerCustomCompletions(commandManager, guilds.getGuildHandler(), guilds.getArenaHandler());
        registerDependencyInjections(commandManager);

        commandManager.getCommandReplacements().addReplacement("guilds", guilds.getSettingsHandler().getSettingsManager().getProperty(PluginSettings.PLUGIN_ALIASES));
        commandManager.getCommandReplacements().addReplacement("syntax", guilds.getSettingsHandler().getSettingsManager().getProperty(PluginSettings.SYNTAX_NAME));

        registerCommands(commandManager);
    }

    /**
     * Load all the language files for the plugin
     * @param guilds the main class
     * @param commandManager command manager
     */
    public void registerLanguages(Guilds guilds, PaperCommandManager commandManager) {
        guilds.getLoadedLanguages().clear();
        try {
            File languageFolder = new File(guilds.getDataFolder(), "languages");
            for (File file : Objects.requireNonNull(languageFolder.listFiles())) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".yml")) {
                        String updatedName = file.getName().replace(".yml", "");
                        guilds.getLoadedLanguages().add(updatedName);
                        commandManager.addSupportedLanguage(Locale.forLanguageTag(updatedName));
                       commandManager.getLocales().loadYamlLanguageFile(new File(languageFolder, file.getName()), Locale.forLanguageTag(updatedName));
                    }
                }
            }
            commandManager.getLocales().setDefaultLocale(Locale.forLanguageTag(guilds.getSettingsHandler().getSettingsManager().getProperty(PluginSettings.MESSAGES_LANGUAGE)));
            LoggingUtils.info("Loaded successfully!");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            LoggingUtils.info("Failed to load!");
        }
    }

    /**
     * Register the custom context objects used in the plugin
     * @param commandManager command manager
     * @param guildHandler guild handler
     */
    public void registerCustomContexts(PaperCommandManager commandManager, GuildHandler guildHandler, ArenaHandler arenaHandler) {

        commandManager.getCommandContexts().registerIssuerAwareContext(Guild.class, c-> {
            Guild guild;
            if (c.hasFlag("admin")) {
               guild = guildHandler.getGuild(c.popFirstArg());
            }
            else {
                guild = guildHandler.getGuild(c.getPlayer());
            }
            if (guild == null) {
                throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            }
            return guild;
        });

        commandManager.getCommandContexts().registerIssuerOnlyContext(GuildRole.class, c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return  null;
            }
            return guildHandler.getGuildRole(guild.getMember(c.getPlayer().getUniqueId()).getRole().getLevel());
        });

        commandManager.getCommandContexts().registerContext(Arena.class, c -> arenaHandler.getArena(c.popFirstArg()).get());
    }


    /**
     * Register the custom command completions used throughout the plugin
     * @param commandManager the command manager
     * @param guildHandler the guild handler
     * @param arenaHandler the arena handler
     */
    public void registerCustomCompletions(PaperCommandManager commandManager, GuildHandler guildHandler, ArenaHandler arenaHandler) {
        commandManager.getCommandCompletions().registerCompletion("online", c -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("invitedTo", c -> guildHandler.getInvitedGuilds(c.getPlayer()));
        commandManager.getCommandCompletions().registerCompletion("joinableGuilds", c -> guildHandler.getJoinableGuild(c.getPlayer()));
        commandManager.getCommandCompletions().registerCompletion("guilds", c -> guildHandler.getGuildNames());
        commandManager.getCommandCompletions().registerCompletion("arenas", c -> arenaHandler.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("locations", c -> Arrays.asList("challenger", "defender"));
        commandManager.getCommandCompletions().registerCompletion("languages", c -> guilds.getLoadedLanguages().stream().sorted().collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("sources", c -> Arrays.asList("JSON", "MYSQL", "SQLITE", "MARIADB"));

        commandManager.getCommandCompletions().registerCompletion("members", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }
            return guild.getMembers().stream().map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName()).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerCompletion("members-admin", c -> {
            Guild guild = c.getContextValue(Guild.class, 1);
           if (guild == null) {
               return null;
           }
            return guild.getMembers().stream().map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName()).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("allyInvites", c-> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }
            if (!guild.hasPendingAllies()) {
                return null;
            }
            return guild.getPendingAllies().stream().map(guildHandler::getNameById).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("allies", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }
            if (!guild.hasAllies()) {
                return null;
            }
            return guild.getAllies().stream().map(guildHandler::getNameById).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("activeCodes", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }
            if (guild.getCodes() == null) {
                return null;
            }
            return guild.getCodes().stream().map(GuildCode::getId).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("vaultAmount", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }
            if (guild.getVaults() == null) {
                return null;
            }
            List<Inventory> list = guildHandler.getCachedVaults().get(guild);
            if (list == null) {
                return null;
            }
            return IntStream.rangeClosed(1, list.size()).mapToObj(Objects::toString).collect(Collectors.toList());
        });

    }

    /**
     * Register all the commands in the plugin
     * @param commandManager the command manager
     */
    public void registerCommands(PaperCommandManager commandManager) {
        Reflections commandClasses = new Reflections("me.glaremasters.guilds.commands");
        Set<Class<? extends BaseCommand>> commands = commandClasses.getSubTypesOf(BaseCommand.class);

        commands.forEach(c -> {
            try {
                commandManager.registerCommand(c.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Register all the dependency injections for ACF
     * @param commandManager the command manger ACF uses
     */
    public void registerDependencyInjections(PaperCommandManager commandManager) {
        commandManager.registerDependency(GuildHandler.class, guilds.getGuildHandler());
        commandManager.registerDependency(SettingsManager.class, guilds.getSettingsHandler().getSettingsManager());
        commandManager.registerDependency(ActionHandler.class, guilds.getActionHandler());
        commandManager.registerDependency(Economy.class, guilds.getEconomy());
        commandManager.registerDependency(Permission.class, guilds.getPermissions());
        commandManager.registerDependency(CooldownHandler.class, guilds.getCooldownHandler());
        commandManager.registerDependency(ArenaHandler.class, guilds.getArenaHandler());
        commandManager.registerDependency(ChallengeHandler.class, guilds.getChallengeHandler());
        commandManager.registerDependency(DatabaseAdapter.class, guilds.getDatabase());
    }

}
