package de.sesosas.simpletablist.classes;

import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.api.utils.StringUtil;
import de.sesosas.simpletablist.cache.PlayerDataCache;
import de.sesosas.simpletablist.cache.ScoreboardStateCache;
import de.sesosas.simpletablist.config.CurrentConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import de.sesosas.simpletablist.classes.scoreboard.DetailsClass;
import de.sesosas.simpletablist.classes.scoreboard.NamesClass;

/**
 * Scoreboard with batch processing, caching and new AnimationManager
 */
public class ScoreboardClass {

    private static final int BATCH_SIZE = 50;

    /**
     * Update all scoreboards with batch processing
     */
    public static synchronized void Update() {
        try {
            List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());

            if (playerList.isEmpty()) {
                return;
            }

            // Process in batches to avoid overwhelming the server
            if (playerList.size() > BATCH_SIZE) {
                processBatches(playerList);
            } else {
                processPlayers(playerList);
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Error updating scoreboards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process players in batches
     */
    private static void processBatches(List<Player> allPlayers) {
        List<List<Player>> batches = new ArrayList<>();

        for (int i = 0; i < allPlayers.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, allPlayers.size());
            batches.add(allPlayers.subList(i, end));
        }

        // Process batches sequentially to avoid overload
        for (List<Player> batch : batches) {
            processPlayers(batch);
        }
    }

    /**
     * Process a list of players
     */
    private static void processPlayers(List<Player> players) {
        boolean namesEnabled = CurrentConfig.getBoolean("Names.Enable");

        for (Player player : players) {
            try {
                if (namesEnabled) {
                    updatePlayerNameOptimized(player);
                }
                updatePlayerTabOptimized(player);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SimpleTabList] Error updating player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Update player name with caching and differential updates
     */
    private static void updatePlayerNameOptimized(Player player) {
        // Get cached player data
        PlayerDataCache.CachedPlayerData data = PlayerDataCache.getPlayerData(player);

        // Build display name
        String format = CurrentConfig.getString("Names.Format.Default");
        String displayName = StringUtil.Convert(format, player);

        // Only update if changed
        if (ScoreboardStateCache.shouldUpdateDisplayName(player.getUniqueId(), displayName)) {
            player.setPlayerListName(displayName);
            ScoreboardStateCache.updateDisplayName(player.getUniqueId(), displayName);

            // Sort player if enabled
            if (CurrentConfig.getBoolean("Names.Sorting.Enable")) {
                NamesClass.sortPlayer(player);
            }
        }
    }

    /**
     * Update player tab (header/footer) with differential updates
     */
    private static void updatePlayerTabOptimized(Player player) {
        try {
            DetailsClass.updateTab(player);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Error updating tab for " + player.getName());
        }
    }

    /**
     * Update only a specific player (for events like join/permission change)
     */
    public static void UpdateSinglePlayer(Player player) {
        try {
            if (CurrentConfig.getBoolean("Names.Enable")) {
                // Invalidate cache for this player
                PlayerDataCache.invalidate(player);
                updatePlayerNameOptimized(player);
            }
            updatePlayerTabOptimized(player);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Error updating single player " + player.getName());
        }
    }

    /**
     * Fast update - only updates animated content
     */
    public static void FastUpdate() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : players) {
            try {
                // Only update header/footer (animations), skip names
                DetailsClass.updateTab(player);
            } catch (Exception e) {
                // Silently fail for fast updates
            }
        }
    }
}