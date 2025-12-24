package de.sesosas.simpletablist.cache;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for player data to avoid repeated LuckPerms queries
 */
public class PlayerDataCache {

    private static final Map<UUID, CachedPlayerData> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 30000; // 30 seconds

    /**
     * Cached player data
     */
    public static class CachedPlayerData {
        private final String groupName;
        private final int groupWeight;
        private final String prefix;
        private final String suffix;
        private final long timestamp;

        public CachedPlayerData(String groupName, int groupWeight, String prefix, String suffix) {
            this.groupName = groupName;
            this.groupWeight = groupWeight;
            this.prefix = prefix != null ? prefix : "";
            this.suffix = suffix != null ? suffix : "";
            this.timestamp = System.currentTimeMillis();
        }

        public String getGroupName() { return groupName; }
        public int getGroupWeight() { return groupWeight; }
        public String getPrefix() { return prefix; }
        public String getSuffix() { return suffix; }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }

    /**
     * Gets player data from cache or loads it fresh
     */
    public static CachedPlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        CachedPlayerData cached = cache.get(uuid);

        // Cache hit and not expired
        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        // Cache miss or expired - reload
        return refreshPlayerData(player);
    }

    /**
     * Reloads player data and caches it
     */
    public static CachedPlayerData refreshPlayerData(Player player) {
        UUID uuid = player.getUniqueId();

        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
            if (user == null) {
                // Fallback if user not found
                CachedPlayerData fallback = new CachedPlayerData("default", 0, "", "");
                cache.put(uuid, fallback);
                return fallback;
            }

            String primaryGroup = user.getPrimaryGroup();
            net.luckperms.api.model.group.Group group = LuckPermsProvider.get()
                    .getGroupManager().getGroup(primaryGroup);

            int weight = 0;
            if (group != null && group.getWeight().isPresent()) {
                weight = group.getWeight().getAsInt();
            }

            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            String suffix = metaData.getSuffix();

            CachedPlayerData data = new CachedPlayerData(primaryGroup, weight, prefix, suffix);
            cache.put(uuid, data);
            return data;

        } catch (Exception e) {
            // Fallback on error
            CachedPlayerData fallback = new CachedPlayerData("default", 0, "", "");
            cache.put(uuid, fallback);
            return fallback;
        }
    }

    /**
     * Invalidates cache entry for a player
     */
    public static void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Invalidates cache entry for a player
     */
    public static void invalidate(Player player) {
        invalidate(player.getUniqueId());
    }

    /**
     * Clears the entire cache
     */
    public static void clearAll() {
        cache.clear();
    }

    /**
     * Removes expired entries
     */
    public static void cleanupExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Returns the number of cached entries
     */
    public static int size() {
        return cache.size();
    }
}