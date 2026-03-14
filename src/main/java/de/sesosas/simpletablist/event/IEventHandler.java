package de.sesosas.simpletablist.event;

import de.sesosas.simpletablist.api.utils.ThreadUtil;
import de.sesosas.simpletablist.cache.PlayerDataCache;
import de.sesosas.simpletablist.classes.ScoreboardClass;
import de.sesosas.simpletablist.classes.scoreboard.SidebarClass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.TimeUnit;

/**
 * Event handler with cache management
 */
public class IEventHandler implements Listener {

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Pre-load player data into cache
        ThreadUtil.submitTask(() -> {
            PlayerDataCache.refreshPlayerData(player);
        });

        // Initialize sidebar for player with small delay
        ThreadUtil.runLaterOnMainThread(() -> {
            SidebarClass.updateSidebar(player);
        }, 10L); // 0.5 second delay

        // Update scoreboard immediately for this player
        ThreadUtil.runLaterOnMainThread(() -> {
            ScoreboardClass.UpdateSinglePlayer(player);
        }, 5L); // 0.25 second delay
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent event) {
        // Remove player from cache to free memory
        PlayerDataCache.invalidate(event.getPlayer());

        // Properly handle player quit for sidebar management
        SidebarClass.handlePlayerQuit(event.getPlayer());

        // Update for remaining players
        ThreadUtil.submitTask(ScoreboardClass::Update);
    }

    @EventHandler
    public void OnEntityPortalExitEvent(PlayerTeleportEvent event) {
        ThreadUtil.submitTask(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ScoreboardClass.Update();
        });
    }
}