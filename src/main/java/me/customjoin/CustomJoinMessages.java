package me.customjoin;

import me.customjoin.command.JoinMessagesCommand;
import me.customjoin.data.PlayerDataStore;
import me.customjoin.hook.DiscordHook;
import me.customjoin.hook.Prefixes;
import me.customjoin.listener.ConnectionListener;
import me.customjoin.listener.MenuListener;
import me.customjoin.listener.VoucherListener;
import me.customjoin.message.MessageRegistry;
import me.customjoin.voucher.Vouchers;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CustomJoinMessages extends JavaPlugin {

    private MessageRegistry registry;
    private PlayerDataStore dataStore;
    private Vouchers vouchers;
    private DiscordHook discord;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registry = new MessageRegistry(this);
        registry.load();
        dataStore = new PlayerDataStore(this);
        dataStore.load();
        vouchers = new Vouchers(this);
        Prefixes.load(this);
        if (getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            discord = new DiscordHook(this);
            discord.load();
        }

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new VoucherListener(this), this);

        PluginCommand command = getCommand("joinmessages");
        if (command != null) {
            JoinMessagesCommand executor = new JoinMessagesCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        getLogger().info("Loaded " + registry.all().size() + " join/leave messages.");
        warnAboutEssentials();
    }

    /** EssentialsX broadcasts its own join/quit message on top of ours unless it is set to "none". */
    private void warnAboutEssentials() {
        Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(essentials.getDataFolder(), "config.yml"));
        for (String key : new String[]{"custom-join-message", "custom-quit-message"}) {
            String value = config.getString(key, "none");
            if (!"none".equalsIgnoreCase(value)) {
                getLogger().warning("EssentialsX " + key + " is set, so players will see two messages. Set it to \"none\" in"
                        + " plugins/Essentials/config.yml and use settings.default-join / default-leave here instead.");
            }
        }
    }

    @Override
    public void onDisable() {
        // Close open menus so nobody keeps an item-stealable inventory after a reload
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof me.customjoin.gui.MessageMenu) {
                player.closeInventory();
            }
        }
        if (dataStore != null) {
            dataStore.saveSync();
        }
        if (discord != null) {
            discord.restore();
        }
    }

    public void reload() {
        reloadConfig();
        registry.load();
        Prefixes.load(this);
        if (discord != null) {
            discord.load();
        }
    }

    public MessageRegistry registry() {
        return registry;
    }

    public PlayerDataStore dataStore() {
        return dataStore;
    }

    public Vouchers vouchers() {
        return vouchers;
    }

    /** Null when DiscordSRV isn't installed. */
    public DiscordHook discord() {
        return discord;
    }
}
