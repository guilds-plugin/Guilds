package me.glaremasters.guilds.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 11:35 AM
 */
public class ACFManager {

    private PaperCommandManager commandManager;

    public ACFManager(PaperCommandManager commandManager) {
        this.commandManager = commandManager;

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

}
