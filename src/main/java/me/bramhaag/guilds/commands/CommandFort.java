package me.bramhaag.guilds.commands;


import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitServerInterface;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * Created by GlareMasters on 6/21/2017.
 */
public class CommandFort extends CommandBase {
    public CommandFort() {
        super("fort", "Spawn your guild fort!", "guilds.command.fort", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        File file = new File(Main.getInstance().getDataFolder(), "test.schematic");
        if (file.exists() && !file.isDirectory()) {
     /* Paste schematic */
            SchematicFormat format = SchematicFormat.getFormat(file);
            CuboidClipboard WEclipboard = null;
            try {
                WEclipboard = format.load(file);
            } catch (IOException | DataException e) {
                e.printStackTrace();
            }
            if (WEclipboard != null) {
                BukkitServerInterface WEinterface = new BukkitServerInterface((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"), Bukkit.getServer());
                EditSession WEsessionEdit = null;
                for (LocalWorld WEworld : WEinterface.getWorlds()) {
                    if (WEworld.getName().equalsIgnoreCase(player.getWorld().getName())) {
                        WEsessionEdit = new EditSession(WEworld, -1);
                        break;
                    }
                }
                Vector WEvector = new Vector(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                try {
                    WEclipboard.paste(WEsessionEdit, WEvector, false);
                } catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
