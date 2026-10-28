package me.customjoin.listener;

import me.customjoin.CustomJoinMessages;
import me.customjoin.message.JoinMessage;
import me.customjoin.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Consumer;

public final class ConnectionListener implements Listener {

    private final CustomJoinMessages plugin;

    public ConnectionListener(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    // MONITOR so we run after EssentialsX (HIGHEST), which clears the join/quit message when it has its own
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        JoinMessage active = plugin.registry().active(event.getPlayer());
        apply(event.getPlayer(), active == null ? null : active.join(), "settings.default-join", event::setJoinMessage);
        if (plugin.discord() != null) {
            plugin.discord().send(event.getPlayer(), event.getJoinMessage(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        JoinMessage active = plugin.registry().active(event.getPlayer());
        apply(event.getPlayer(), active == null ? null : active.leave(), "settings.default-leave", event::setQuitMessage);
        if (plugin.discord() != null) {
            plugin.discord().send(event.getPlayer(), event.getQuitMessage(), false);
        }
    }

    private void apply(Player player, String custom, String defaultPath, Consumer<String> setter) {
        if (custom != null && !custom.isEmpty()) {
            setter.accept(Text.color(Text.withPlayer(custom, player)));
            return;
        }
        String fallback = plugin.getConfig().getString(defaultPath, "");
        if (fallback == null || fallback.isEmpty()) {
            return; // keep vanilla
        }
        setter.accept(fallback.equalsIgnoreCase("none") ? null : Text.color(Text.withPlayer(fallback, player)));
    }
}
