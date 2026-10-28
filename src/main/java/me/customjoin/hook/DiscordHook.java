package me.customjoin.hook;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.customjoin.CustomJoinMessages;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/** Sends join/leave messages to Discord through DiscordSRV, replacing DiscordSRV's own ones. Only loaded when DiscordSRV is installed. */
public final class DiscordHook {

    private static final String[] OWN_MESSAGES = {
            "MinecraftPlayerJoinMessage", "MinecraftPlayerFirstJoinMessage", "MinecraftPlayerLeaveMessage"
    };

    private final CustomJoinMessages plugin;
    private final Map<String, Boolean> original = new HashMap<>();
    private boolean enabled;

    public DiscordHook(CustomJoinMessages plugin) {
        this.plugin = plugin;
        for (String key : OWN_MESSAGES) {
            original.put(key, DiscordSRV.config().getBooleanElse(key + ".Enabled", true));
        }
    }

    /** Re-reads discordsrv.enabled; turns DiscordSRV's own join/leave messages off while ours are on. */
    public void load() {
        enabled = plugin.getConfig().getBoolean("discordsrv.enabled", true);
        for (String key : OWN_MESSAGES) {
            DiscordSRV.config().setRuntimeValue(key + ".Enabled", !enabled && original.get(key));
        }
    }

    public void restore() {
        for (String key : OWN_MESSAGES) {
            DiscordSRV.config().setRuntimeValue(key + ".Enabled", original.get(key));
        }
    }

    public void send(Player player, String message, boolean join) {
        if (!enabled || !DiscordSRV.isReady || isVanished(player)) {
            return;
        }
        String text = plain(message);
        if (text.isEmpty()) {
            return;
        }
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("discordsrv");
        if (config == null) {
            return;
        }
        TextChannel channel = DiscordSRV.getPlugin().getOptionalTextChannel(config.getString("channel", "join"));
        if (channel == null) {
            return;
        }
        String type = join ? "join" : "leave";
        if (config.getBoolean("embed", true)) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(text.length() > 256 ? text.substring(0, 256) : text, null, DiscordSRV.getAvatarUrl(player))
                    .setColor(color(config.getString(type + "-color", "")));
            DiscordUtil.queueMessage(channel, new MessageBuilder().setEmbeds(embed.build()).build());
        } else {
            String format = config.getString(type + "-format", "{message}");
            DiscordUtil.queueMessage(channel, format
                    .replace("{message}", DiscordUtil.escapeMarkdown(text))
                    .replace("{player}", DiscordUtil.escapeMarkdown(player.getName())));
        }
    }

    /** Strips colors and resource-pack glyphs (private use area), which Discord can't show. */
    private static String plain(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(message).replaceAll("[\\uE000-\\uF8FF]", "").replaceAll("\\s{2,}", " ").trim();
    }

    private static Color color(String hex) {
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isVanished(Player player) {
        for (MetadataValue value : player.getMetadata("vanished")) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return false;
    }
}
