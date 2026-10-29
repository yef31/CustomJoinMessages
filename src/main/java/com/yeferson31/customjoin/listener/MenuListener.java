package com.yeferson31.customjoin.listener;

import com.yeferson31.customjoin.CustomJoinMessages;
import com.yeferson31.customjoin.gui.MessageMenu;
import com.yeferson31.customjoin.message.JoinMessage;
import com.yeferson31.customjoin.util.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {

    private final CustomJoinMessages plugin;

    public MenuListener(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MessageMenu menu)) {
            return;
        }
        // Block every click while the menu is open (incl. shift-clicks from the player inventory)
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        Player player = menu.player();
        int slot = event.getRawSlot();

        String id = menu.messageAt(slot);
        if (id != null) {
            toggle(player, id);
            menu.render();
            return;
        }

        MessageMenu.Button button = menu.buttonAt(slot);
        if (button == null) {
            return;
        }
        switch (button) {
            case CLEAR -> {
                plugin.dataStore().setSelected(player.getUniqueId(), null);
                Lang.send(plugin, player, "cleared");
                Lang.sound(plugin, player, "clear");
                menu.render();
            }
            case PREVIOUS -> {
                Lang.sound(plugin, player, "page");
                menu.changePage(-1);
            }
            case NEXT -> {
                Lang.sound(plugin, player, "page");
                menu.changePage(1);
            }
            case CLOSE -> plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
        }
    }

    private void toggle(Player player, String id) {
        JoinMessage message = plugin.registry().get(id);
        if (message == null || !player.hasPermission(message.permission())) {
            Lang.send(plugin, player, "no-permission");
            return;
        }
        boolean equipped = id.equals(plugin.dataStore().getSelected(player.getUniqueId()));
        if (equipped) {
            plugin.dataStore().setSelected(player.getUniqueId(), null);
            Lang.send(plugin, player, "unequipped", "{name}", message.name());
            Lang.sound(plugin, player, "unequip");
        } else {
            plugin.dataStore().setSelected(player.getUniqueId(), id);
            Lang.send(plugin, player, "equipped", "{name}", message.name());
            Lang.sound(plugin, player, "equip");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MessageMenu) {
            event.setCancelled(true);
        }
    }
}
