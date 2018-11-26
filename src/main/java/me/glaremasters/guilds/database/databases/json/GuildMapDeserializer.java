package me.glaremasters.guilds.database.databases.json;

import co.aikar.commands.ACFBukkitUtil;
import com.google.gson.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.Serialization;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:44 AM
 */
public class GuildMapDeserializer implements JsonDeserializer<Map<String, Guild>> {

    File homes, status, tiers, banks;
    YamlConfiguration homeC = null;
    YamlConfiguration statusC = null;
    YamlConfiguration tiersC = null;
    YamlConfiguration banksC = null;

    @Override
    public Map<String, Guild> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        status = new File(Guilds.getGuilds().getDataFolder(), "data/guild-status.yml");
        homes = new File(Guilds.getGuilds().getDataFolder(), "data/guild-homes.yml");
        tiers = new File(Guilds.getGuilds().getDataFolder(), "data/guild-tiers.yml");
        banks = new File(Guilds.getGuilds().getDataFolder(), "data/guild-banks.yml");

        if (status.exists()) {
            statusC = YamlConfiguration.loadConfiguration(status);
        }
        if (homes.exists()) {
            homeC = YamlConfiguration.loadConfiguration(homes);
        }
        if (tiers.exists()) {
            tiersC = YamlConfiguration.loadConfiguration(tiers);
        }
        if (banks.exists()) {
            banksC = YamlConfiguration.loadConfiguration(banks);
        }

        Map<String, Guild> guilds = new HashMap<>();
        obj.entrySet().forEach(entry -> {
            JsonObject guild = entry.getValue().getAsJsonObject();
            guild.addProperty("name", entry.getKey());
            if (status.exists() && homes.exists() && tiers.exists() && banks.exists()) {
                convertOldData(guild);
            }
            guilds.put(entry.getKey(), context.deserialize(guild, Guild.class));
        });
        Bukkit.getServer().getScheduler().runTaskLater(Guilds.getGuilds(), () -> Guilds.getGuilds().getDatabase().updateGuild(), 120L);
        if (status.exists() && homes.exists() && tiers.exists() && banks.exists()) {

            String zipFile = "plugins/Guilds/Old-Guild-Data.zip";
            String[] srcFiles = {
                    "data/guild-status.yml",
                    "data/guild-homes.yml",
                    "data/guild-tiers.yml",
                    "data/guild-banks.yml"
            };


            try {
                byte[] buffer = new byte[1024];
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos);
                for (int i = 0; i < srcFiles.length; i++) {
                    File srcFile = new File(Guilds.getGuilds().getDataFolder(), srcFiles[i]);
                    FileInputStream fis = new FileInputStream(srcFile);
                    zos.putNextEntry(new ZipEntry(srcFile.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                    fis.close();

                }
                zos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Files.delete(status.toPath());
                Files.delete(homes.toPath());
                Files.delete(tiers.toPath());
                Files.delete(banks.toPath());
                FileUtils.deleteDirectory(new File(Guilds.getGuilds().getDataFolder(), "data/vaults/"));
                FileUtils.deleteDirectory(new File(Guilds.getGuilds().getDataFolder(), "languages/"));
                FileUtils.deleteDirectory(new File(Guilds.getGuilds().getDataFolder(), "old-languages/"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return guilds;
    }

    public void convertOldData(JsonObject object) {

        String guildName = object.get("name").getAsString();

        File vault = new File(Guilds.getGuilds().getDataFolder(), "data/vaults/" + guildName + ".yml");
        if (vault.exists()) {
            YamlConfiguration vaultConfig = YamlConfiguration.loadConfiguration(vault);
            Inventory inv = Bukkit.createInventory(null, 54, guildName + "'s Guild Vault");
            for (int i = 0; i < inv.getSize(); i++) {
                if (vaultConfig.isSet("items.slot" + i)) {
                    inv.setItem(i, vaultConfig.getItemStack("items.slot" + i));
                }
            }
            if (!object.has("inventory")) {
                object.addProperty("inventory", Serialization.serializeInventory(inv));
            }
        }

        if (!object.has("status")) {
            if (statusC != null) {
                object.addProperty("status", statusC.getString(guildName));
            } else {
                object.addProperty("status", "Private");
            }
        }
        if (!object.has("home")) {
            if (homeC != null) {
                Location oldHome = ACFBukkitUtil.stringToLocation(homeC.getString(guildName));
                object.addProperty("home", ACFBukkitUtil.fullLocationToString(oldHome));
            } else {
                object.addProperty("home", "");
            }
        }
        if (!object.has("tier")) {
            if (tiersC != null) {
                object.addProperty("tier", tiersC.getInt(guildName));
            } else {
                object.addProperty("tier", 1);
            }
        }
        if (!object.has("balance")) {
            if (banksC != null) {
                object.addProperty("balance", banksC.getDouble(guildName));
            } else {
                object.addProperty("balance", 0.0);
            }
        }
    }

}
