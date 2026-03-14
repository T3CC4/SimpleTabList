package de.sesosas.simpletablist.api.classes;

import de.sesosas.simpletablist.api.utils.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract class for creating interval-based tasks
 */
public abstract class AInterval {

    private final String name;
    private static final List<AInterval> instances = new ArrayList<>();
    private BukkitTask bukkitTask;
    private String scheduledTaskId;
    private long intervalTime;
    private boolean isRunning = false;
    private final Object lock = new Object();
    private boolean useMainThread = false;
    private boolean requiresPlayers = true; // By default, intervals require players to run

    /**
     * Creates a new interval task
     * @param name Unique name for this interval
     */
    public AInterval(String name) {
        this.name = name;
        instances.add(this);
        Init();
    }

    /**
     * Get the name of this interval
     * @return Name of the interval
     */
    public String getName() {
        return name;
    }

    /**
     * Set the interval time in seconds
     * @param intervalTime Interval time in seconds
     */
    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    /**
     * Get the interval time in seconds
     * @return Interval time in seconds
     */
    public long getIntervalTime() {
        return this.intervalTime;
    }

    /**
     * Set whether this interval should run on the main server thread
     * @param useMainThread True to run on main thread, false for async execution
     */
    public void setUseMainThread(boolean useMainThread) {
        this.useMainThread = useMainThread;
    }

    /**
     * Set whether this interval requires players to be online to run
     * @param requiresPlayers True if this task should only run when players are online
     */
    public void setRequiresPlayers(boolean requiresPlayers) {
        this.requiresPlayers = requiresPlayers;
    }

    /**
     * Check if this interval requires players to be online
     * @return True if this task requires players to run
     */
    public boolean requiresPlayers() {
        return this.requiresPlayers;
    }

    /**
     * Check if this interval is running on the main server thread
     * @return True if running on main thread
     */
    public boolean isUsingMainThread() {
        return this.useMainThread;
    }

    /**
     * Initialize this interval
     */
    public abstract void Init();

    /**
     * Run method to be called at each interval
     */
    public abstract void Run();

    /**
     * Method called when an interval is starting
     */
    public void onStart() {
        // Override in subclasses if needed
    }

    /**
     * Method called when an interval is stopping
     */
    public void onStop() {
        // Override in subclasses if needed
    }

    /**
     * Start this interval
     * @param plugin Plugin instance
     * @param interval Interval time in seconds
     */
    public void startInterval(JavaPlugin plugin, long interval) {
        synchronized (lock) {
            if (isRunning) {
                return;
            }

            if (useMainThread) {
                // Use Bukkit scheduler for main thread execution
                bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    try {
                        // Check if this task should run based on player count
                        if (requiresPlayers && Bukkit.getOnlinePlayers().isEmpty()) {
                            return; // Skip execution if no players are online
                        }
                        Run();
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE,
                                "[SimpleTabList] Error in interval task: " + name, e);
                    }
                }, 0L, interval * 20L);
            } else {
                // Use ThreadUtil for async execution
                scheduledTaskId = "interval_" + name;
                ThreadUtil.scheduleRepeatingTask(scheduledTaskId, () -> {
                    try {
                        // Check if this task should run based on player count
                        if (requiresPlayers && Bukkit.getOnlinePlayers().isEmpty()) {
                            return; // Skip execution if no players are online
                        }
                        Run();
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE,
                                "[SimpleTabList] Error in async interval task: " + name, e);
                    }
                }, 0, interval * 1000);
            }

            isRunning = true;
            onStart();
        }
    }

    /**
     * Stop this interval
     */
    public void stopInterval() {
        synchronized (lock) {
            if (!isRunning) {
                return;
            }

            if (useMainThread && bukkitTask != null && !bukkitTask.isCancelled()) {
                bukkitTask.cancel();
                bukkitTask = null;
            } else if (!useMainThread && scheduledTaskId != null) {
                ThreadUtil.cancelTask(scheduledTaskId);
                scheduledTaskId = null;
            }

            isRunning = false;
            onStop();
        }
    }

    /**
     * Check if this interval is currently running
     * @return True if the interval is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start all registered intervals
     * @param plugin Plugin instance
     */
    public static void startAllIntervals(JavaPlugin plugin) {
        for (AInterval intervalTask : instances) {
            try {
                intervalTask.startInterval(plugin, intervalTask.getIntervalTime());
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "[SimpleTabList] Failed to start interval: " + intervalTask.getName(), e);
            }
        }
    }

    /**
     * Stop all registered intervals
     */
    public static void stopAllIntervals() {
        for (AInterval intervalTask : instances) {
            try {
                intervalTask.stopInterval();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "[SimpleTabList] Failed to stop interval: " + intervalTask.getName(), e);
            }
        }
    }

    /**
     * Get all registered interval instances
     * @return List of all interval instances
     */
    public static List<AInterval> getAllIntervals() {
        return new ArrayList<>(instances);
    }

    /**
     * Find an interval by name
     * @param name The name to search for
     * @return The interval instance, or null if not found
     */
    public static AInterval getIntervalByName(String name) {
        for (AInterval interval : instances) {
            if (interval.getName().equals(name)) {
                return interval;
            }
        }
        return null;
    }
}