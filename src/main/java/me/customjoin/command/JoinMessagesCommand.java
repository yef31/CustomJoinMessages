package me.customjoin.command;

import me.customjoin.CustomJoinMessages;
import me.customjoin.gui.MessageMenu;
import me.customjoin.message.JoinMessage;
import me.customjoin.util.Lang;
import me.customjoin.util.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JoinMessagesCommand implements TabExecutor {

    private static final String ADMIN = "customjoin.admin";
    private final CustomJoinMessages plugin;

    public JoinMessagesCommand(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                usage(sender);
                return true;
            }
            if (!player.hasPermission("customjoin.use")) {
                Lang.send(plugin, player, "no-permission");
                return true;
            }
            new MessageMenu(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "clear" -> {
                if (!(sender instanceof Player player)) {
                    Lang.send(plugin, sender, "players-only");
                    return true;
                }
                plugin.dataStore().setSelected(player.getUniqueId(), null);
                Lang.send(plugin, player, "cleared");
            }
            case "reload" -> {
                if (noAdmin(sender)) return true;
                plugin.reload();
                Lang.send(plugin, sender, "reloaded", "{count}", String.valueOf(plugin.registry().all().size()));
            }
            case "list" -> {
                if (noAdmin(sender)) return true;
                Lang.send(plugin, sender, "list-header");
                for (JoinMessage message : plugin.registry().all()) {
                    sender.sendMessage(Text.color(Lang.replace(plugin.getConfig().getString("lang.list-entry", ""),
                            "{id}", message.id(), "{permission}", message.permission(), "{name}", message.name())));
                }
            }
            case "set" -> {
                if (noAdmin(sender)) return true;
                if (args.length < 3) {
                    usage(sender);
                    return true;
                }
                OfflinePlayer target = findPlayer(args[1]);
                if (target == null) {
                    Lang.send(plugin, sender, "player-not-found");
                    return true;
                }
                JoinMessage message = plugin.registry().get(args[2]);
                if (message == null) {
                    Lang.send(plugin, sender, "unknown-message", "{id}", args[2]);
                    return true;
                }
                plugin.dataStore().setSelected(target.getUniqueId(), message.id());
                String name = target.getName() == null ? args[1] : target.getName();
                Lang.send(plugin, sender, "admin-set", "{player}", name, "{name}", message.name());
                Player online = target.getPlayer();
                if (online != null && !online.hasPermission(message.permission())) {
                    Lang.send(plugin, sender, "admin-no-access", "{player}", name);
                }
            }
            case "reset" -> {
                if (noAdmin(sender)) return true;
                if (args.length < 2) {
                    usage(sender);
                    return true;
                }
                OfflinePlayer target = findPlayer(args[1]);
                if (target == null) {
                    Lang.send(plugin, sender, "player-not-found");
                    return true;
                }
                plugin.dataStore().setSelected(target.getUniqueId(), null);
                Lang.send(plugin, sender, "admin-reset", "{player}", target.getName() == null ? args[1] : target.getName());
            }
            case "voucher" -> {
                if (noAdmin(sender)) return true;
                if (args.length < 2) {
                    usage(sender);
                    return true;
                }
                JoinMessage message = plugin.registry().get(args[1]);
                if (message == null) {
                    Lang.send(plugin, sender, "unknown-message", "{id}", args[1]);
                    return true;
                }
                Player target;
                if (args.length >= 3) {
                    target = plugin.getServer().getPlayerExact(args[2]);
                } else if (sender instanceof Player player) {
                    target = player;
                } else {
                    usage(sender);
                    return true;
                }
                if (target == null) {
                    Lang.send(plugin, sender, "player-not-found");
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
                    } catch (NumberFormatException e) {
                        usage(sender);
                        return true;
                    }
                }
                // Drop whatever doesn't fit at the player's feet
                for (ItemStack left : target.getInventory().addItem(plugin.vouchers().create(message, amount)).values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), left);
                }
                Lang.send(plugin, sender, "voucher-given", "{amount}", String.valueOf(amount),
                        "{name}", message.name(), "{player}", target.getName());
            }
            default -> usage(sender);
        }
        return true;
    }

    private boolean noAdmin(CommandSender sender) {
        if (sender.hasPermission(ADMIN)) {
            return false;
        }
        Lang.send(plugin, sender, "no-permission");
        return true;
    }

    private OfflinePlayer findPlayer(String name) {
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) {
            return online;
        }
        for (OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)) {
                return offline;
            }
        }
        return null;
    }

    private void usage(CommandSender sender) {
        for (String line : plugin.getConfig().getStringList("lang.usage")) {
            sender.sendMessage(Text.color(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        boolean admin = sender.hasPermission(ADMIN);
        if (args.length == 1) {
            options.add("clear");
            if (admin) {
                options.addAll(List.of("set", "reset", "list", "reload", "voucher"));
            }
        } else if (admin && ((args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset")))
                || (args.length == 3 && args[0].equalsIgnoreCase("voucher")))) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                options.add(player.getName());
            }
        } else if (admin && ((args.length == 3 && args[0].equalsIgnoreCase("set"))
                || (args.length == 2 && args[0].equalsIgnoreCase("voucher")))) {
            for (JoinMessage message : plugin.registry().all()) {
                options.add(message.id());
            }
        }
        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return options;
    }
}
