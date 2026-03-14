package de.sesosas.simpletablist;

import de.sesosas.simpletablist.api.classes.AInterval;
import de.sesosas.simpletablist.api.utils.ThreadUtil;
import de.sesosas.simpletablist.api.utils.WorldUtil;
import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.cache.PlayerDataCache;
import de.sesosas.simpletablist.classes.UpdateClass;
import de.sesosas.simpletablist.classes.scoreboard.NamesClass;
import de.sesosas.simpletablist.classes.scoreboard.SidebarClass;
import de.sesosas.simpletablist.command.ReloadCommand;
import de.sesosas.simpletablist.command.SidebarCommand;
import de.sesosas.simpletablist.command.AnimationCommand;
import de.sesosas.simpletablist.config.SidebarConfig;
import de.sesosas.simpletablist.event.IEventHandler;
import de.sesosas.simpletablist.classes.ScoreboardClass;
import de.sesosas.simpletablist.interval.AnimatedText;
import de.sesosas.simpletablist.interval.SidebarInterval;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SimpleTabList extends JavaPlugin implements Listener {

    public FileConfiguration config = getConfig();

    private static SimpleTabList plugin;

    public static SimpleTabList getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize thread utility
        ThreadUtil.initialize(this);

        // Initialize scoreboard
        NamesClass.initScoreboard();

        // Set up configuration defaults FIRST
        setupDefaultConfig();

        // Load sidebar configuration BEFORE AnimationManager
        SidebarConfig.loadConfig();

        // NOW initialize animation system (after configs exist)
        AnimationManager.initialize();

        // Initialize sidebar display
        SidebarClass.initialize();

        // Generate world configs
        WorldUtil.GenerateWorldConfig();

        // Set up LuckPerms integration with cache invalidation
        setupLuckPerms();

        // Set up metrics if enabled
        if(config.getBoolean("bstats.Enable")){
            setupMetrics();
        }

        // Check for updates
        checkForUpdates();

        // Initialize animated text and sidebar intervals
        new AnimatedText();
        new SidebarInterval();

        // Start all intervals
        AInterval.startAllIntervals(this);

        // Register event handlers and commands
        getServer().getPluginManager().registerEvents(new IEventHandler(), this);
        getCommand("stl-reload").setExecutor(new ReloadCommand());
        getCommand("sidebar").setExecutor(new SidebarCommand());
        getCommand("animation").setExecutor(new AnimationCommand());

        // Do initial scoreboard update after everything is initialized
        Bukkit.getScheduler().runTaskLater(this, () -> {
            ScoreboardClass.Update();
            Bukkit.getLogger().info("[SimpleTabList] Initial scoreboard update completed");
        }, 20L); // 1 second delay

        // Start cache cleanup task
        startCacheCleanupTask();

        Bukkit.getLogger().info("Simple TabList has started with PlayerDataCache!");
    }

    private void setupDefaultConfig() {
        java.lang.String[] headerString = new java.lang.String[]{"This is a header and animation {animation:0}!", "You: %player_name%!"};
        java.lang.String[] footerString = new java.lang.String[] {"This is a footer!", "This is footer line 2!"};

        config.addDefault("Names.Enable", true);
        config.addDefault("Names.Format.Default", "%luckperms_prefix% &f[player_name] %luckperms_suffix%");
        config.addDefault("Names.Global.Enable", false);
        config.addDefault("Names.Global.Prefix", "");
        config.addDefault("Names.Global.Suffix", "");
        config.addDefault("Names.Sorting.Enable", true);
        config.addDefault("Names.Sorting.Type", "weight");
        config.addDefault("Names.Sorting.Ascending", true);
        config.addDefault("Worlds.Enable", false);
        config.addDefault("Header.Enable", true);
        config.addDefault("Header.Content", headerString);
        config.addDefault("Footer.Enable", true);
        config.addDefault("Footer.Content", footerString);
        config.addDefault("Chat.Prefix", "§f[§cSTL§f]");
        config.addDefault("Chat.ActionbarMessage", false);
        config.addDefault("Tab.Refresh.Interval.Enable", false);
        config.addDefault("Tab.Refresh.Interval.Time", 1L);
        config.addDefault("bstats.Enable", true);
        config.addDefault("Performance.AsyncThreads", true);
        config.addDefault("Performance.Cache.TTL", 30);
        config.addDefault("Performance.Cache.CleanupInterval", 60);
        config.options().copyDefaults(true);

        List<String> headerComment = new ArrayList<>();
        headerComment.add("Worlds\n");
        headerComment.add("    Enable\n");
        headerComment.add("Does enable/disable the worlds function which overrides the current Header and Footer content.\n");
        headerComment.add("You need LuckPerms and PlaceholderAPI to make this plugin work!\n");
        headerComment.add("Tab Refresh Interval Time is calculated in seconds.\n");
        headerComment.add("Performance.AsyncThreads: Set to true to run operations asynchronously for better performance.\n");
        headerComment.add("Performance.Cache.TTL: Cache time-to-live in seconds (default: 30)\n");
        headerComment.add("Performance.Cache.CleanupInterval: How often to clean expired cache entries in seconds (default: 60)\n");
        config.options().header(headerComment.toString().replace("[", "").replace("]", "").replace(", ", ""));
        saveConfig();
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();
            EventBus eventBus = luckPerms.getEventBus();

            // Listen to node add events
            eventBus.subscribe(plugin, NodeAddEvent.class, this::onNodeChange);

            // Listen to node remove events
            eventBus.subscribe(plugin, NodeRemoveEvent.class, this::onNodeChange);

            // Listen to user data recalculate events
            eventBus.subscribe(plugin, UserDataRecalculateEvent.class, this::onUserDataRecalculate);

            Bukkit.getLogger().info("[SimpleTabList] LuckPerms integration with cache invalidation enabled");
        } else {
            Bukkit.getLogger().warning("LuckPerms not found! Some features will not work properly.");
        }
    }

    private void setupMetrics() {
        int id = 15221;
        Metrics metrics = new Metrics(this, id);
        metrics.addCustomChart(new SingleLineChart("banned", () -> Bukkit.getBannedPlayers().size()));
        metrics.addCustomChart(new SingleLineChart("cached_players", PlayerDataCache::size));
        Bukkit.getLogger().info("bStats metrics enabled");
    }

    private void checkForUpdates() {
        new UpdateClass(this, 101989).getVersion(version -> {
            if (Float.parseFloat(this.getDescription().getVersion()) < Float.parseFloat(version)) {
                getLogger().info("There is a new update available: v" + version);
            } else {
                getLogger().info("You are running the latest version");
            }
        });
    }

    /**
     * Start cache cleanup task
     */
    private void startCacheCleanupTask() {
        long cleanupInterval = config.getLong("Performance.Cache.CleanupInterval", 60);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            int sizeBefore = PlayerDataCache.size();
            PlayerDataCache.cleanupExpired();
            int sizeAfter = PlayerDataCache.size();

            if (sizeBefore > sizeAfter) {
                Bukkit.getLogger().info("[SimpleTabList] Cache cleanup: removed " + (sizeBefore - sizeAfter) + " expired entries");
            }
        }, cleanupInterval * 20L, cleanupInterval * 20L);
    }

    @Override
    public void onDisable(){
        // Clean up player scoreboards
        NamesClass.resetPlayerNames();

        // Remove all sidebars
        SidebarClass.removeAllSidebars();

        // Stop all intervals
        AInterval.stopAllIntervals();

        // Clear all caches
        PlayerDataCache.clearAll();

        // Graceful shutdown of thread pools
        ThreadUtil.shutdown();

        // Give threads a moment to terminate gracefully
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // Ignore interruption during shutdown
        }

        // Force shutdown any remaining threads
        ThreadUtil.forceShutdown();

        Bukkit.getLogger().info("Simple TabList has been disabled");
    }

    /**
     * Handle LuckPerms node changes (add/remove)
     */
    private <T extends LuckPermsEvent> void onNodeChange(T event) {
        // Extract UUID from event
        UUID affectedUser = null;

        if (event instanceof NodeAddEvent) {
            NodeAddEvent nodeEvent = (NodeAddEvent) event;
            if (nodeEvent.isUser()) {
                // Get UUID from Identifier
                net.luckperms.api.model.user.User user = (net.luckperms.api.model.user.User) nodeEvent.getTarget();
                affectedUser = user.getUniqueId();
            }
        } else if (event instanceof NodeRemoveEvent) {
            NodeRemoveEvent nodeEvent = (NodeRemoveEvent) event;
            if (nodeEvent.isUser()) {
                // Get UUID from Identifier
                net.luckperms.api.model.user.User user = (net.luckperms.api.model.user.User) nodeEvent.getTarget();
                affectedUser = user.getUniqueId();
            }
        }

        // Invalidate cache for affected user
        if (affectedUser != null) {
            final UUID userId = affectedUser;
            PlayerDataCache.invalidate(userId);

            // Update scoreboard for online player
            Player player = Bukkit.getPlayer(userId);
            if (player != null && player.isOnline()) {
                ThreadUtil.submitTask(() -> {
                    ScoreboardClass.Update();
                });
            }
        }
    }

    /**
     * Handle LuckPerms user data recalculate
     */
    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        UUID userId = event.getUser().getUniqueId();

        // Invalidate cache
        PlayerDataCache.invalidate(userId);

        // Update scoreboard for online player
        Player player = Bukkit.getPlayer(userId);
        if (player != null && player.isOnline()) {
            ThreadUtil.submitTask(() -> {
                ScoreboardClass.Update();
            });
        }
    }
}