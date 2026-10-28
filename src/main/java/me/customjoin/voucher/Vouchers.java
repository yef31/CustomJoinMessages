package me.customjoin.voucher;

import me.customjoin.CustomJoinMessages;
import me.customjoin.message.JoinMessage;
import me.customjoin.util.Lang;
import me.customjoin.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Right-click items that unlock a message. The message id is stored in the item's persistent data. */
public final class Vouchers {

    private final CustomJoinMessages plugin;
    private final NamespacedKey key;
    // Permission commands (e.g. LuckPerms) apply async, so block a second redeem until they have landed
    private final Set<String> pending = new HashSet<>();

    public Vouchers(CustomJoinMessages plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "voucher");
    }

    public ItemStack create(JoinMessage message, int amount) {
        String materialName = plugin.getConfig().getString("vouchers.material", "NAME_TAG");
        Material material = Material.matchMaterial(materialName);
        if (material == null || !material.isItem()) {
            material = Material.NAME_TAG;
        }
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(Text.color(plugin.getConfig().getString("vouchers.name", "{name}").replace("{name}", message.name())));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("vouchers.lore")) {
            lore.add(Text.color(line.replace("{name}", message.name()).replace("{id}", message.id())));
        }
        meta.setLore(lore);
        if (plugin.getConfig().getBoolean("vouchers.glow", true)) {
            // Looked up by key so it works on both old (DURABILITY) and new (UNBREAKING) API names
            Enchantment glow = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (glow != null) {
                meta.addEnchant(glow, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, message.id());
        item.setItemMeta(meta);
        return item;
    }

    /** The message id stored on the item, or null if it isn't a voucher. */
    public String idOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /** Redeems the voucher in the player's hand. Returns true if one item should be consumed. */
    public boolean redeem(Player player, String id) {
        JoinMessage message = plugin.registry().get(id);
        if (message == null) {
            Lang.send(plugin, player, "voucher-invalid");
            return false;
        }
        String pendingKey = player.getUniqueId() + ":" + message.id();
        if (player.hasPermission(message.permission()) || pending.contains(pendingKey)) {
            Lang.send(plugin, player, "voucher-already", "{name}", message.name());
            return false;
        }
        pending.add(pendingKey);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> pending.remove(pendingKey), 100L);

        for (String command : plugin.getConfig().getStringList("vouchers.redeem-commands")) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command
                    .replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString())
                    .replace("{permission}", message.permission())
                    .replace("{id}", message.id()));
        }
        if (plugin.getConfig().getBoolean("vouchers.auto-equip", true)) {
            plugin.dataStore().setSelected(player.getUniqueId(), message.id());
        }
        Lang.send(plugin, player, "voucher-redeemed", "{name}", message.name());
        Lang.sound(plugin, player, "redeem");
        return true;
    }
}
