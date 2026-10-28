package me.customjoin.util;

import me.customjoin.CustomJoinMessages;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Lang {

    private Lang() {
    }

    /** Sends lang.<key> with the prefix. Replacements are pairs: "{name}", value, ... */
    public static void send(CustomJoinMessages plugin, CommandSender to, String key, String... replacements) {
        String raw = plugin.getConfig().getString("lang." + key, "");
        if (raw.isEmpty()) {
            return;
        }
        to.sendMessage(Text.color(plugin.getConfig().getString("lang.prefix", "") + replace(raw, replacements)));
    }

    public static String replace(String input, String... replacements) {
        String out = input;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            out = out.replace(replacements[i], replacements[i + 1]);
        }
        return out;
    }

    public static void sound(CustomJoinMessages plugin, Player player, String key) {
        String sound = plugin.getConfig().getString("settings.sounds." + key, "");
        if (sound == null || sound.isEmpty()) {
            return;
        }
        Location location = player.getLocation();
        player.playSound(location, sound, 1f, 1f);
    }
}
