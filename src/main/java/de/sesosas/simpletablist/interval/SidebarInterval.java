package de.sesosas.simpletablist.interval;

import de.sesosas.simpletablist.api.classes.AInterval;
import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.classes.scoreboard.SidebarClass;
import de.sesosas.simpletablist.config.SidebarConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval task for updating sidebars with AnimationManager support
 */
public class SidebarInterval extends AInterval {
    private boolean syncWithTablistAnimation;

    public SidebarInterval() {
        super("SidebarUpdate");
    }

    @Override
    public void Init() {
        // Get the update interval from config, default to 2 seconds
        int interval = SidebarConfig.getInt("Sidebar.Refresh.Interval");
        if (interval <= 0) {
            interval = 2;
        }
        setIntervalTime(interval);

        // Check if sidebar animations should sync with tablist animations
        syncWithTablistAnimation = SidebarConfig.getBoolean("Sidebar.Animations.SyncWithTablist");

        // Sidebar updates need to be on the main thread as they modify Bukkit entities
        setUseMainThread(true);

        // Sidebar should only update when players are online
        setRequiresPlayers(true);
    }

    @Override
    public void Run() {
        // Check if sidebars are enabled globally
        if (!SidebarConfig.getBoolean("Sidebar.Enable")) {
            return;
        }

        try {
            // If animations are enabled, we advance the frame index
            if (SidebarConfig.getBoolean("Sidebar.Animations.Enable") && !syncWithTablistAnimation) {
                // Only tick animations if we're not syncing with tablist
                // (otherwise the tablist interval will handle it)
                AnimationManager.tick();
            }

            // Only update sidebars if refresh is enabled or if animations are enabled
            if (SidebarConfig.getBoolean("Sidebar.Refresh.Enable") ||
                    SidebarConfig.getBoolean("Sidebar.Animations.Enable")) {

                // Update sidebar for all online players
                List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
                for (Player player : playerList) {
                    try {
                        SidebarClass.updateSidebar(player);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[SimpleTabList] Error updating sidebar for " + player.getName() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[SimpleTabList] Critical error in sidebar interval: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        Bukkit.getLogger().info("[SimpleTabList] Sidebar update interval started with period: "
                + getIntervalTime() + " seconds");

        if (syncWithTablistAnimation) {
            Bukkit.getLogger().info("[SimpleTabList] Sidebar animations will sync with tablist animations");
        } else if (SidebarConfig.getBoolean("Sidebar.Animations.Enable")) {
            Bukkit.getLogger().info("[SimpleTabList] Sidebar animations enabled with independent timing");
        }
    }

    @Override
    public void onStop() {
        Bukkit.getLogger().info("[SimpleTabList] Sidebar update interval stopped");
    }
}