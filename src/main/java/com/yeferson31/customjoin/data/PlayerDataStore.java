package com.yeferson31.customjoin.data;

import com.yeferson31.customjoin.CustomJoinMessages;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/** Stores which message each player has equipped, in data.yml. */
public final class PlayerDataStore {

    private final CustomJoinMessages plugin;
    private final File file;
    private final Map<UUID, String> selected = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    public PlayerDataStore(CustomJoinMessages plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        selected.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read data.yml", e);
            return;
        }
        for (String key : yaml.getKeys(false)) {
            try {
                selected.put(UUID.fromString(key), yaml.getString(key));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
    }

    public String getSelected(UUID uuid) {
        return selected.get(uuid);
    }

    public void setSelected(UUID uuid, String id) {
        if (id == null) {
            selected.remove(uuid);
        } else {
            selected.put(uuid, id);
        }
        saveAsync();
    }

    public void saveAsync() {
        String contents = serialize();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> write(contents));
    }

    public void saveSync() {
        write(serialize());
    }

    private String serialize() {
        YamlConfiguration yaml = new YamlConfiguration();
        selected.forEach((uuid, id) -> yaml.set(uuid.toString(), id));
        return yaml.saveToString();
    }

    private void write(String contents) {
        synchronized (writeLock) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.writeString(file.toPath(), contents, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
            }
        }
    }
}
