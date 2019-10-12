package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArenaAdapter {
    private final ArenaProvider provider;
    private String sqlTablePrefix;

    public ArenaAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch(backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "arenas");
                provider = new ArenaJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
            case MARIADB:
                sqlTablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getArenaProvider());
        }
    }

    public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
    }

    public boolean arenaExists(@NotNull String id) throws IOException {
        return provider.arenaExists(sqlTablePrefix, id);
    }

    public List<String> getAllArenaIds() throws IOException {
        return provider.getAllArenaIds(sqlTablePrefix);
    }

    public List<Arena> getAllArenas() throws IOException {
        return provider.getAllArenas(sqlTablePrefix);
    }

    public void saveArenas(@NotNull List<Arena> arenas) throws IOException {
        List<String> savedIds = new ArrayList<>();

        for (Arena guild : arenas) {
            saveArena(guild);
            savedIds.add(guild.getId().toString());
        }

        for (String arenaId : getAllArenaIds()) {
            boolean keep = savedIds.stream().anyMatch(id -> id.equals(arenaId));
            if (!keep) {
                deleteArena(arenaId);
            }
        }

        savedIds.clear();
    }

    public void saveArena(@NotNull Arena arena) throws IOException {
        if (!arenaExists(arena.getId().toString())) {
            createArena(arena);
        } else {
            updateArena(arena);
        }
    }

    public void createArena(@NotNull Arena arena) throws IOException {
        provider.createArena(sqlTablePrefix, arena.getId().toString(), Guilds.getGson().toJson(arena, Arena.class));
    }

    public void updateArena(@NotNull Arena arena) throws IOException {
        provider.updateArena(sqlTablePrefix, arena.getId().toString(), Guilds.getGson().toJson(arena, Arena.class));
    }

    public void deleteArena(@NotNull String id) throws IOException {
        provider.deleteArena(sqlTablePrefix, id);
    }
}
