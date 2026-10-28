package me.customjoin.gui;

import me.customjoin.CustomJoinMessages;
import me.customjoin.message.JoinMessage;
import me.customjoin.util.Lang;
import me.customjoin.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The paged selection menu. Re-rendered in place after every click. */
public final class MessageMenu implements InventoryHolder {

    public enum Button { CLEAR, PREVIOUS, NEXT, CLOSE }

    private final CustomJoinMessages plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Integer> contentSlots;
    private final Map<Integer, String> messageSlots = new HashMap<>();
    private final Map<Integer, Button> buttonSlots = new HashMap<>();
    private int page;

    public MessageMenu(CustomJoinMessages plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        int size = Math.max(2, Math.min(6, plugin.getConfig().getInt("gui.rows", 6))) * 9;
        this.inventory = plugin.getServer().createInventory(this, size,
                Text.color(plugin.getConfig().getString("gui.title", "Join & Leave Messages")));
        List<Integer> slots = parseSlots(plugin.getConfig().getStringList("gui.content-slots"), size);
        if (slots.isEmpty()) {
            // Default: every slot except the last row
            for (int i = 0; i < size - 9; i++) {
                slots.add(i);
            }
        }
        this.contentSlots = slots;
    }

    public void open() {
        render();
        player.openInventory(inventory);
        Lang.sound(plugin, player, "open");
    }

    public void render() {
        FileConfiguration config = plugin.getConfig();
        inventory.clear();
        messageSlots.clear();
        buttonSlots.clear();

        // Decoration (borders, corners...) goes first so anything else can overwrite it
        ConfigurationSection decoration = config.getConfigurationSection("gui.decoration");
        if (decoration != null) {
            for (String key : decoration.getKeys(false)) {
                ConfigurationSection deco = decoration.getConfigurationSection(key);
                if (deco == null) {
                    continue;
                }
                ItemStack item = item(material(deco.getString("material"), Material.GRAY_STAINED_GLASS_PANE),
                        deco.getString("name", " "), deco.getStringList("lore"));
                for (int slot : parseSlots(deco.getStringList("slots"), inventory.getSize())) {
                    inventory.setItem(slot, item);
                }
            }
        }

        List<JoinMessage> available = plugin.registry().available(player);
        JoinMessage active = plugin.registry().active(player);
        int perPage = contentSlots.size();
        int pageCount = Math.max(1, (available.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pageCount - 1));

        int start = page * perPage;
        for (int i = 0; i < perPage && start + i < available.size(); i++) {
            JoinMessage message = available.get(start + i);
            int slot = contentSlots.get(i);
            inventory.setItem(slot, messageItem(message, active != null && active.id().equals(message.id())));
            messageSlots.put(slot, message.id());
        }
        if (available.isEmpty()) {
            placeButton("empty", null, contentSlots.get(perPage / 2), new String[0]);
        }

        String currentName = active == null ? config.getString("lang.none", "None") : active.name();
        String[] info = {
                "{current}", currentName,
                "{unlocked}", String.valueOf(available.size()),
                "{total}", String.valueOf(plugin.registry().all().size()),
                "{page}", String.valueOf(page + 1),
                "{pages}", String.valueOf(pageCount)
        };
        placeButton("current", null, 4, info);
        if (page > 0) {
            placeButton("previous", Button.PREVIOUS, inventory.getSize() - 6, info);
        }
        if (page < pageCount - 1) {
            placeButton("next", Button.NEXT, inventory.getSize() - 4, info);
        }
        placeButton("clear", Button.CLEAR, inventory.getSize() - 7, info);
        placeButton("close", Button.CLOSE, inventory.getSize() - 5, info);
    }

    private ItemStack messageItem(JoinMessage message, boolean equipped) {
        FileConfiguration config = plugin.getConfig();
        String status = config.getString(equipped ? "gui.status-equipped" : "gui.status-not-equipped", "");
        String action = config.getString(equipped ? "gui.action-equipped" : "gui.action-not-equipped", "");
        String join = Text.withPlayer(message.join(), player);
        String leave = Text.withPlayer(message.leave(), player);

        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("gui.item-lore")) {
            if (line.contains("{description}")) {
                for (String desc : message.description()) {
                    lore.add(line.replace("{description}", desc));
                }
                continue;
            }
            lore.add(Lang.replace(line,
                    "{name}", message.name(),
                    "{join}", join,
                    "{leave}", leave,
                    "{status}", status,
                    "{action}", action));
        }
        String name = config.getString("gui.item-name", "{name}").replace("{name}", message.name());
        ItemStack item = item(message.material(), name, lore);

        if (equipped && config.getBoolean("gui.equipped-glow", true)) {
            ItemMeta meta = item.getItemMeta();
            // Looked up by key so it works on both old (DURABILITY) and new (UNBREAKING) API names
            Enchantment glow = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (meta != null && glow != null) {
                meta.addEnchant(glow, 1, true);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    /** Places gui.buttons.<key> at its configured slot (or the fallback) and registers it as clickable. */
    private void placeButton(String key, Button type, int fallbackSlot, String[] replacements) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("gui.buttons." + key);
        int slot = section == null ? fallbackSlot : section.getInt("slot", fallbackSlot);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        String name = section == null ? key : section.getString("name", key);
        List<String> lore = new ArrayList<>();
        if (section != null) {
            for (String line : section.getStringList("lore")) {
                lore.add(Lang.replace(line, replacements));
            }
        }
        Material material = material(section == null ? null : section.getString("material"), Material.STONE);
        inventory.setItem(slot, item(material, Lang.replace(name, replacements), lore));
        if (type != null) {
            buttonSlots.put(slot, type);
        }
    }

    private static Material material(String name, Material fallback) {
        Material material = name == null ? null : Material.matchMaterial(name);
        return material == null || !material.isItem() ? fallback : material;
    }

    /** Accepts entries like "10", "10-16". */
    private static List<Integer> parseSlots(List<String> entries, int size) {
        List<Integer> out = new ArrayList<>();
        for (String entry : entries) {
            try {
                String[] parts = entry.trim().split("-");
                int from = Integer.parseInt(parts[0].trim());
                int to = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : from;
                for (int i = from; i <= to; i++) {
                    if (i >= 0 && i < size && !out.contains(i)) {
                        out.add(i);
                    }
                }
            } catch (NumberFormatException ignored) {
                // skip bad entries
            }
        }
        return out;
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            meta.setLore(Text.color(lore));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    public String messageAt(int slot) {
        return messageSlots.get(slot);
    }

    public Button buttonAt(int slot) {
        return buttonSlots.get(slot);
    }

    public void changePage(int delta) {
        page += delta;
        render();
    }

    public Player player() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
