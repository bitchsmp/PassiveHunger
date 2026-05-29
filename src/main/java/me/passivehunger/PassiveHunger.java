package me.passivehunger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class PassiveHunger extends JavaPlugin {

    private boolean depletionEnabled;
    private double totalDepletionTimeSeconds;
    private boolean excludeCreativeSpectator;
    private boolean excludePeaceful;
    private boolean excludeBypassPermission;

    private HungerTicker hungerTicker;
    private BukkitTask tickerTask;

    @Override
    public void onEnable() {
        // Save default config if not present
        saveDefaultConfig();

        // Load configuration values
        loadPluginConfig();

        // Register Command Executor
        if (getCommand("passivehunger") != null) {
            HungerCommand commandExecutor = new HungerCommand(this);
            getCommand("passivehunger").setExecutor(commandExecutor);
            getCommand("passivehunger").setTabCompleter(commandExecutor);
        }

        // Register Quit Listener to clean up memory
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Start Ticker
        startTicker();

        getLogger().info("PassiveHunger has been enabled!");
    }

    @Override
    public void onDisable() {
        stopTicker();
        getLogger().info("PassiveHunger has been disabled!");
    }

    /**
     * Loads/reloads the configuration values from config.yml.
     */
    public void loadPluginConfig() {
        reloadConfig();
        this.depletionEnabled = getConfig().getBoolean("enabled", true);
        this.totalDepletionTimeSeconds = getConfig().getDouble("total-depletion-time-seconds", 1200.0);
        this.excludeCreativeSpectator = getConfig().getBoolean("exclude-creative-spectator", true);
        this.excludePeaceful = getConfig().getBoolean("exclude-peaceful", true);
        this.excludeBypassPermission = getConfig().getBoolean("exclude-bypass-permission", true);

        // Adjust running state based on loaded config
        if (depletionEnabled) {
            startTicker();
        } else {
            stopTicker();
        }
    }

    /**
     * Starts the repeating task if it is not already running.
     */
    public void startTicker() {
        if (!depletionEnabled) {
            return;
        }

        if (tickerTask == null) {
            if (hungerTicker == null) {
                hungerTicker = new HungerTicker(this);
            }
            // Run task every 20 ticks (1 second)
            tickerTask = hungerTicker.runTaskTimer(this, 20L, 20L);
            getLogger().info("PassiveHunger ticker started.");
        }
    }

    /**
     * Stops the repeating task.
     */
    public void stopTicker() {
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
            getLogger().info("PassiveHunger ticker stopped.");
        }
        if (hungerTicker != null) {
            hungerTicker.clearCache();
        }
    }

    public boolean isDepletionEnabled() {
        return depletionEnabled;
    }

    public void setDepletionEnabled(boolean enabled) {
        this.depletionEnabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();
        if (enabled) {
            startTicker();
        } else {
            stopTicker();
        }
    }

    public double getTotalDepletionTimeSeconds() {
        return totalDepletionTimeSeconds;
    }

    public boolean isExcludeCreativeSpectator() {
        return excludeCreativeSpectator;
    }

    public boolean isExcludePeaceful() {
        return excludePeaceful;
    }

    public boolean isExcludeBypassPermission() {
        return excludeBypassPermission;
    }

    public HungerTicker getHungerTicker() {
        return hungerTicker;
    }
}
