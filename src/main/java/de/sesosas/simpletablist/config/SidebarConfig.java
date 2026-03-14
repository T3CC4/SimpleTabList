package de.sesosas.simpletablist.config;

import de.sesosas.simpletablist.SimpleTabList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages dedicated sidebar configuration
 */
public class SidebarConfig {
    private static FileConfiguration sidebarConfig;
    private static File configFile;
    private static final Map<String, FileConfiguration> worldSidebarConfigs = new HashMap<>();

    /**
     * Load or initialize the sidebar configuration
     */
    public static void loadConfig() {
        configFile = new File(SimpleTabList.getPlugin().getDataFolder(), "sidebar.yml");

        if (!configFile.exists()) {
            createDefaultConfig();
        }

        sidebarConfig = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config has all required settings
        ensureDefaults();

        // Load world-specific sidebar configurations
        loadWorldConfigs();

        Bukkit.getLogger().info("[SimpleTabList] Sidebar configuration loaded");
    }

    /**
     * Create default sidebar configuration
     */
    private static void createDefaultConfig() {
        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();

            sidebarConfig = YamlConfiguration.loadConfiguration(configFile);

            // Set default values
            sidebarConfig.set("Sidebar.Enable", true);
            sidebarConfig.set("Sidebar.Title", "&6&l✦ &e&lYOUR SERVER &6&l✦");

            List<String> defaultLines = new ArrayList<>();
            defaultLines.add("&6&l➤ &fServer: &a%server_name%");
            defaultLines.add("&6&l➤ &fPlayers: &a%server_online%/%server_max_players%");
            defaultLines.add("&6&l➤ &fRank: &a%luckperms_prefix%");
            defaultLines.add("&6&l➤ &fHealth: &a%player_health%/%player_max_health%");
            defaultLines.add("&6&l➤ &fXP Level: &a%player_level%");
            defaultLines.add("&1");
            defaultLines.add("&6&l➤ &fTime: &a{animation:1}");  // Animated time
            defaultLines.add("&6&l➤ &fLocation: &a%player_x%, %player_y%, %player_z%");
            defaultLines.add("&2");
            defaultLines.add("&ewww.yourserver.com");

            sidebarConfig.set("Sidebar.Lines", defaultLines);
            sidebarConfig.set("Sidebar.Refresh.Enable", true);
            sidebarConfig.set("Sidebar.Refresh.Interval", 2);
            sidebarConfig.set("Sidebar.PerWorld.Enable", true);

            // Add animation settings
            sidebarConfig.set("Sidebar.Animations.Enable", true);
            sidebarConfig.set("Sidebar.Animations.SyncWithTablist", true);

            // Add section for per-player sidebar settings
            sidebarConfig.set("Sidebar.PerPlayer.Enable", true);
            sidebarConfig.set("Sidebar.PerPlayer.Permission", "stl.sidebar");

            // Add special formatting options
            sidebarConfig.set("Sidebar.Format.BlankLineChar", " ");
            sidebarConfig.set("Sidebar.Format.LineSpacing", true);

            sidebarConfig.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[SimpleTabList] Could not create sidebar configuration file!");
            e.printStackTrace();
        }
    }

    /**
     * Ensure the config has all required default values
     */
    private static void ensureDefaults() {
        boolean changed = false;

        if (!sidebarConfig.contains("Sidebar.Enable")) {
            sidebarConfig.set("Sidebar.Enable", true);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Title")) {
            sidebarConfig.set("Sidebar.Title", "&6&l✦ &e&lYOUR SERVER &6&l✦");
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Lines")) {
            List<String> defaultLines = new ArrayList<>();
            defaultLines.add("&6&l➤ &fServer: &a%server_name%");
            defaultLines.add("&6&l➤ &fPlayers: &a%server_online%/%server_max_players%");
            defaultLines.add("&6&l➤ &fRank: &a%luckperms_prefix%");
            defaultLines.add("&1");
            defaultLines.add("&ewww.yourserver.com");

            sidebarConfig.set("Sidebar.Lines", defaultLines);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Refresh.Enable")) {
            sidebarConfig.set("Sidebar.Refresh.Enable", true);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Refresh.Interval")) {
            sidebarConfig.set("Sidebar.Refresh.Interval", 2);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.PerWorld.Enable")) {
            sidebarConfig.set("Sidebar.PerWorld.Enable", true);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Animations.Enable")) {
            sidebarConfig.set("Sidebar.Animations.Enable", true);
            changed = true;
        }

        if (!sidebarConfig.contains("Sidebar.Animations.SyncWithTablist")) {
            sidebarConfig.set("Sidebar.Animations.SyncWithTablist", true);
            changed = true;
        }

        // Save if changes were made
        if (changed) {
            try {
                sidebarConfig.save(configFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe("[SimpleTabList] Could not save sidebar configuration!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Load world-specific sidebar configurations
     */
    private static void loadWorldConfigs() {
        worldSidebarConfigs.clear();

        File worldsFolder = new File(SimpleTabList.getPlugin().getDataFolder(), "worlds");
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs();
        }

        for (World world : Bukkit.getWorlds()) {
            File worldSidebarFile = new File(worldsFolder, world.getName() + "_sidebar.yml");

            if (!worldSidebarFile.exists()) {
                createWorldSidebarConfig(world, worldSidebarFile);
            }

            FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldSidebarFile);
            worldSidebarConfigs.put(world.getName(), worldConfig);
        }
    }

    /**
     * Create a world-specific sidebar configuration
     * @param world The world to create a config for
     * @param configFile The file to save to
     */
    private static void createWorldSidebarConfig(World world, File configFile) {
        try {
            configFile.createNewFile();

            FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(configFile);

            worldConfig.set("Sidebar.Enable", true);
            worldConfig.set("Sidebar.Title", "&6&l✦ &e&l" + world.getName().toUpperCase() + " &6&l✦");

            List<String> worldLines = new ArrayList<>();
            worldLines.add("&6&l➤ &fWorld: &a" + world.getName());
            worldLines.add("&6&l➤ &fPlayers: &a%world_players_" + world.getName() + "%");
            worldLines.add("&6&l➤ &fTime: &a{animation:1}");  // Animated time
            worldLines.add("&1");
            worldLines.add("&6&l➤ &fYour Location:");
            worldLines.add("&e  X: %player_x%, Y: %player_y%, Z: %player_z%");
            worldLines.add("&2");
            worldLines.add("&ewww.yourserver.com");

            worldConfig.set("Sidebar.Lines", worldLines);

            // Add animation settings for world
            worldConfig.set("Sidebar.Animations.Enable", true);

            worldConfig.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[SimpleTabList] Could not create world sidebar config for " + world.getName());
            e.printStackTrace();
        }
    }

    /**
     * Reload the sidebar configuration
     */
    public static void reloadConfig() {
        if (configFile == null) {
            configFile = new File(SimpleTabList.getPlugin().getDataFolder(), "sidebar.yml");
        }

        if (!configFile.exists()) {
            createDefaultConfig();
        }

        sidebarConfig = YamlConfiguration.loadConfiguration(configFile);
        loadWorldConfigs();
    }

    /**
     * Get the sidebar configuration
     * @return The sidebar FileConfiguration
     */
    public static FileConfiguration getConfig() {
        if (sidebarConfig == null) {
            loadConfig();
        }
        return sidebarConfig;
    }

    /**
     * Get a value from the sidebar configuration
     * @param path The configuration path
     * @return The value at the path
     */
    public static Object get(String path) {
        return getConfig().get(path);
    }

    /**
     * Get a string from the sidebar configuration
     * @param path The configuration path
     * @return The string at the path
     */
    public static String getString(String path) {
        return getConfig().getString(path);
    }

    /**
     * Get a boolean from the sidebar configuration
     * @param path The configuration path
     * @return The boolean at the path
     */
    public static boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    /**
     * Get an integer from the sidebar configuration
     * @param path The configuration path
     * @return The integer at the path
     */
    public static int getInt(String path) {
        return getConfig().getInt(path);
    }

    /**
     * Get a list from the sidebar configuration
     * @param path The configuration path
     * @return The list at the path
     */
    public static List<?> getList(String path) {
        return getConfig().getList(path);
    }

    /**
     * Get a string list from the sidebar configuration
     * @param path The configuration path
     * @return The string list at the path
     */
    public static List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    /**
     * Get a value from a world-specific sidebar configuration
     * @param world The world name
     * @param path The configuration path
     * @return The value at the path for the given world
     */
    public static Object getWorldValue(String world, String path) {
        if (!worldSidebarConfigs.containsKey(world)) {
            return null;
        }

        return worldSidebarConfigs.get(world).get(path);
    }

    /**
     * Get a string from a world-specific sidebar configuration
     * @param world The world name
     * @param path The configuration path
     * @return The string at the path for the given world
     */
    public static String getWorldString(String world, String path) {
        if (!worldSidebarConfigs.containsKey(world)) {
            return null;
        }

        return worldSidebarConfigs.get(world).getString(path);
    }

    /**
     * Get a boolean from a world-specific sidebar configuration
     * @param world The world name
     * @param path The configuration path
     * @return The boolean at the path for the given world
     */
    public static boolean getWorldBoolean(String world, String path) {
        if (!worldSidebarConfigs.containsKey(world)) {
            return false;
        }

        return worldSidebarConfigs.get(world).getBoolean(path);
    }

    /**
     * Get a list from a world-specific sidebar configuration
     * @param world The world name
     * @param path The configuration path
     * @return The list at the path for the given world
     */
    public static List<?> getWorldList(String world, String path) {
        if (!worldSidebarConfigs.containsKey(world)) {
            return null;
        }

        return worldSidebarConfigs.get(world).getList(path);
    }

    /**
     * Get a string list from a world-specific sidebar configuration
     * @param world The world name
     * @param path The configuration path
     * @return The string list at the path for the given world
     */
    public static List<String> getWorldStringList(String world, String path) {
        if (!worldSidebarConfigs.containsKey(world)) {
            return null;
        }

        return worldSidebarConfigs.get(world).getStringList(path);
    }

    /**
     * Save the sidebar configuration
     */
    public static void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[SimpleTabList] Could not save sidebar configuration!");
            e.printStackTrace();
        }
    }
}