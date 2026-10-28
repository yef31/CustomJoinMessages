package me.customjoin.listener;

import me.customjoin.CustomJoinMessages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class VoucherListener implements Listener {

    private final CustomJoinMessages plugin;

    public VoucherListener(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != null && handle(event.getPlayer(), event.getHand())) {
            event.setCancelled(true);
        }
    }

    // Right-clicking a mob with a name tag voucher would rename it instead
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (handle(event.getPlayer(), event.getHand())) {
            event.setCancelled(true);
        }
    }

    /** Returns true if the item in that hand was a voucher. */
    private boolean handle(Player player, EquipmentSlot hand) {
        ItemStack item = player.getInventory().getItem(hand);
        String id = plugin.vouchers().idOf(item);
        if (id == null) {
            return false;
        }
        if (plugin.vouchers().redeem(player, id)) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItem(hand, item.getAmount() > 0 ? item : null);
        }
        return true;
    }
}
