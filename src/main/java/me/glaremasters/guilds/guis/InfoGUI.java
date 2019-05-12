package me.glaremasters.guilds.guis;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.GuildInfoSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Glare
 * Date: 5/11/2019
 * Time: 11:17 PM
 */
@AllArgsConstructor
public class InfoGUI {

    private Guilds guilds;
    private SettingsManager settingsManager;

    public Gui getInfoGUI(Guild guild) {

        // Create the GUI with the desired name from the config
        Gui gui = new Gui(guilds, 3, ACFBukkitUtil.color(settingsManager.getProperty(GuildInfoSettings.GUI_NAME)));

        // Create the background pane which will just be stained glass
        OutlinePane backgroundPane = new OutlinePane(0, 0, 9, 1, Pane.Priority.LOW);

        // Add the items to the background pane
        createBackgroundItems(backgroundPane);

        // Add the glass panes to the main GUI background pane
        gui.addPane(backgroundPane);

        // Return the new info GUI object
        return gui;
    }

    /**
     * Create the background panes
     * @param pane the pane to add to
     */
    private void createBackgroundItems(OutlinePane pane) {
        ItemBuilder builder = new ItemBuilder(new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7));
        for (int i = 0; i < 9; i++) {
            pane.addItem(new GuiItem(builder.build(), event -> event.setCancelled(true)));
        }
    }

}
