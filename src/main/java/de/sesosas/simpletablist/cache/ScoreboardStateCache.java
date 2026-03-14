package de.sesosas.simpletablist.cache;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for scoreboard states to avoid unnecessary updates
 */
public class ScoreboardStateCache {

    private static final Map<UUID, PlayerScoreboardState> cache = new ConcurrentHashMap<>();

    /**
     * Current scoreboard state of a player
     */
    public static class PlayerScoreboardState {
        private final String displayName;
        private final String header;
        private final String footer;
        private final String teamName;
        private final int hashCode;

        public PlayerScoreboardState(String displayName, String header, String footer, String teamName) {
            this.displayName = displayName;
            this.header = header;
            this.footer = footer;
            this.teamName = teamName;
            this.hashCode = Objects.hash(displayName, header, footer, teamName);
        }

        public String getDisplayName() { return displayName; }
        public String getHeader() { return header; }
        public String getFooter() { return footer; }
        public String getTeamName() { return teamName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlayerScoreboardState that = (PlayerScoreboardState) o;
            return Objects.equals(displayName, that.displayName) &&
                    Objects.equals(header, that.header) &&
                    Objects.equals(footer, that.footer) &&
                    Objects.equals(teamName, that.teamName);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Checks if an update is needed and stores new state
     * @return true if update needed, false if identical to cache
     */
    public static boolean shouldUpdate(UUID playerId, String displayName, String header, String footer, String teamName) {
        PlayerScoreboardState newState = new PlayerScoreboardState(displayName, header, footer, teamName);
        PlayerScoreboardState cached = cache.get(playerId);

        // No cache entry or different = update needed
        if (cached == null || !cached.equals(newState)) {
            cache.put(playerId, newState);
            return true;
        }

        return false;
    }

    /**
     * Checks if DisplayName update is needed
     */
    public static boolean shouldUpdateDisplayName(UUID playerId, String displayName) {
        PlayerScoreboardState cached = cache.get(playerId);
        return cached == null || !Objects.equals(cached.displayName, displayName);
    }

    /**
     * Checks if Header update is needed
     */
    public static boolean shouldUpdateHeader(UUID playerId, String header) {
        PlayerScoreboardState cached = cache.get(playerId);
        return cached == null || !Objects.equals(cached.header, header);
    }

    /**
     * Checks if Footer update is needed
     */
    public static boolean shouldUpdateFooter(UUID playerId, String footer) {
        PlayerScoreboardState cached = cache.get(playerId);
        return cached == null || !Objects.equals(cached.footer, footer);
    }

    /**
     * Updates only DisplayName in cache
     */
    public static void updateDisplayName(UUID playerId, String displayName) {
        PlayerScoreboardState cached = cache.get(playerId);
        if (cached != null) {
            cache.put(playerId, new PlayerScoreboardState(
                    displayName, cached.header, cached.footer, cached.teamName
            ));
        } else {
            cache.put(playerId, new PlayerScoreboardState(displayName, "", "", ""));
        }
    }

    /**
     * Updates only Header in cache
     */
    public static void updateHeader(UUID playerId, String header) {
        PlayerScoreboardState cached = cache.get(playerId);
        if (cached != null) {
            cache.put(playerId, new PlayerScoreboardState(
                    cached.displayName, header, cached.footer, cached.teamName
            ));
        } else {
            cache.put(playerId, new PlayerScoreboardState("", header, "", ""));
        }
    }

    /**
     * Updates only Footer in cache
     */
    public static void updateFooter(UUID playerId, String footer) {
        PlayerScoreboardState cached = cache.get(playerId);
        if (cached != null) {
            cache.put(playerId, new PlayerScoreboardState(
                    cached.displayName, cached.header, footer, cached.teamName
            ));
        } else {
            cache.put(playerId, new PlayerScoreboardState("", "", footer, ""));
        }
    }

    /**
     * Removes player from cache
     */
    public static void remove(UUID playerId) {
        cache.remove(playerId);
    }

    /**
     * Clears the entire cache
     */
    public static void clearAll() {
        cache.clear();
    }

    /**
     * Returns the number of cached entries
     */
    public static int size() {
        return cache.size();
    }
}