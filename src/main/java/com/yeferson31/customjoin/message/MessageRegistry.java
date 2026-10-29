package com.yeferson31.customjoin.message;

import com.yeferson31.customjoin.CustomJoinMessages;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MessageRegistry {

    private final CustomJoinMessages plugin;
    private final Map<String, JoinMessage> messages = new LinkedHashMap<>();

    public MessageRegistry(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messages.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages");
        if (section == null) {
            plugin.getLogger().warning("No 'messages' section found in config.yml");
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            String id = key.toLowerCase(Locale.ROOT);
            String materialName = entry.getString("material", "NAME_TAG");
            Material material = Material.matchMaterial(materialName);
            if (material == null || !material.isItem()) {
                plugin.getLogger().warning("Message '" + key + "' has invalid material '" + materialName + "', using NAME_TAG");
                material = Material.NAME_TAG;
            }
            messages.put(id, new JoinMessage(
                    id,
                    entry.getString("permission", "customjoin.message." + id),
                    entry.getString("name", key),
                    material,
                    entry.getStringList("description"),
                    entry.getString("join", ""),
                    entry.getString("leave", "")
            ));
        }
    }

    public JoinMessage get(String id) {
        return id == null ? null : messages.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<JoinMessage> all() {
        return Collections.unmodifiableCollection(messages.values());
    }

    /** Messages the player has permission for, in config order. */
    public List<JoinMessage> available(Player player) {
        List<JoinMessage> out = new ArrayList<>();
        for (JoinMessage message : messages.values()) {
            if (player.hasPermission(message.permission())) {
                out.add(message);
            }
        }
        return out;
    }

    /** The equipped message, or null if none is equipped or the player lost access to it. */
    public JoinMessage active(Player player) {
        JoinMessage message = get(plugin.dataStore().getSelected(player.getUniqueId()));
        if (message == null || !player.hasPermission(message.permission())) {
            return null;
        }
        return message;
    }
}
