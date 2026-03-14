package de.sesosas.simpletablist.interval;

import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.classes.ScoreboardClass;
import de.sesosas.simpletablist.api.classes.AInterval;
import de.sesosas.simpletablist.config.CurrentConfig;
import org.bukkit.Bukkit;

/**
 * Optimized animated text interval using new AnimationManager
 */
public class AnimatedText extends AInterval {

    public AnimatedText() {
        super("AnimatedText");
    }

    @Override
    public void Init() {
        long interval = CurrentConfig.getLong("Tab.Refresh.Interval.Time");
        if (interval <= 0) {
            interval = 1L;
        }
        setIntervalTime(interval);

        // Animations must run on the main thread for safety
        setUseMainThread(true);

        // Only run when players are online
        setRequiresPlayers(true);
    }

    @Override
    public void Run() {
        if (CurrentConfig.getBoolean("Tab.Refresh.Interval.Enable")) {
            try {
                // Check if AnimationManager is initialized
                if (!AnimationManager.isInitialized()) {
                    Bukkit.getLogger().warning("[SimpleTabList] AnimationManager not initialized in AnimatedText interval!");
                    return;
                }

                // Advance all animations
                AnimationManager.tick();

                // Update scoreboard - this must be on the main thread
                ScoreboardClass.Update();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SimpleTabList] Error in animation tick: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        Bukkit.getLogger().info("[SimpleTabList] Animation interval started with period: "
                + getIntervalTime() + " seconds");

        // Validate animations on start
        if (AnimationManager.isInitialized()) {
            int count = AnimationManager.getAnimationCount();
            Bukkit.getLogger().info("[SimpleTabList] Running with " + count + " animations");

            // List all animations
            if (count > 0) {
                Bukkit.getLogger().info("[SimpleTabList] Loaded animations: " +
                        String.join(", ", AnimationManager.getAnimationIds()));
            }

            // Log any validation errors
            java.util.List<String> errors = AnimationManager.validateAnimations();
            if (!errors.isEmpty()) {
                Bukkit.getLogger().warning("[SimpleTabList] Found " + errors.size() + " animation issues:");
                for (String error : errors) {
                    Bukkit.getLogger().warning("  - " + error);
                }
            }
        } else {
            Bukkit.getLogger().warning("[SimpleTabList] AnimationManager not initialized!");
        }
    }

    @Override
    public void onStop() {
        Bukkit.getLogger().info("[SimpleTabList] Animation interval stopped");
    }
}