package de.sesosas.simpletablist.api.luckperms;

import de.sesosas.simpletablist.cache.PlayerDataCache;
import org.bukkit.entity.Player;

/**
 * Group class using PlayerDataCache
 */
public class Group {

    /**
     * Get player group weight from cache
     */
    public static int getPlayerGroupWeight(Player player) {
        PlayerDataCache.CachedPlayerData data = PlayerDataCache.getPlayerData(player);
        return data.getGroupWeight();
    }

    /**
     * Get player group name from cache
     */
    public static String getPlayerGroupName(Player player) {
        PlayerDataCache.CachedPlayerData data = PlayerDataCache.getPlayerData(player);
        return data.getGroupName();
    }

    /**
     * Get player prefix from cache
     */
    public static String getPlayerPrefix(Player player) {
        PlayerDataCache.CachedPlayerData data = PlayerDataCache.getPlayerData(player);
        return data.getPrefix();
    }

    /**
     * Get player suffix from cache
     */
    public static String getPlayerSuffix(Player player) {
        PlayerDataCache.CachedPlayerData data = PlayerDataCache.getPlayerData(player);
        return data.getSuffix();
    }
}