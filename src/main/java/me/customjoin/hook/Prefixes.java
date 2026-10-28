package me.customjoin.hook;

import me.customjoin.CustomJoinMessages;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

/**
 * {prefix} / {suffix} from LuckPerms. Empty when LuckPerms is missing or settings.luckperms-prefix is off.
 * settings.prefix-format replaces the LuckPerms prefix with any text (PlaceholderAPI placeholders allowed).
 */
public final class Prefixes {

    private static boolean enabled;
    private static boolean papi;
    private static String format = "";

    private Prefixes() {
    }

    public static void load(CustomJoinMessages plugin) {
        enabled = plugin.getConfig().getBoolean("settings.luckperms-prefix", true)
                && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms");
        papi = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        format = plugin.getConfig().getString("settings.prefix-format", "");
    }

    /** Fills PlaceholderAPI placeholders when PlaceholderAPI is installed. */
    public static String placeholders(Player player, String text) {
        return papi && text.indexOf('%') >= 0 ? Papi.apply(player, text) : text;
    }

    public static String prefix(Player player) {
        if (format != null && !format.isEmpty()) {
            return placeholders(player, format);
        }
        return enabled ? LuckPermsMeta.get(player, true) : "";
    }

    public static String suffix(Player player) {
        return enabled ? LuckPermsMeta.get(player, false) : "";
    }

    // Separate class so PlaceholderAPI classes are only loaded when PlaceholderAPI is installed
    private static final class Papi {

        static String apply(Player player, String text) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
    }

    // Separate class so LuckPerms classes are only loaded when LuckPerms is installed
    private static final class LuckPermsMeta {

        static String get(Player player, boolean prefix) {
            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return "";
            }
            CachedMetaData meta = user.getCachedData().getMetaData();
            String value = prefix ? meta.getPrefix() : meta.getSuffix();
            return value == null ? "" : value;
        }
    }
}
