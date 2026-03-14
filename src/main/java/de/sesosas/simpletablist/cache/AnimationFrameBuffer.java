package de.sesosas.simpletablist.cache;

import de.sesosas.simpletablist.config.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Frame buffer for animations - precomputes frames instead of calculating for each player
 */
public class AnimationFrameBuffer {

    private static final Map<String, List<String>> animationFrames = new HashMap<>();
    private static int currentFrameIndex = 0;

    /**
     * Loads all animations into the buffer
     */
    public static void loadAnimations() {
        animationFrames.clear();

        String configPath = "animations";
        CustomConfig cf = new CustomConfig().setup(configPath);
        FileConfiguration config = cf.get();

        if (config == null) {
            Bukkit.getLogger().warning("[SimpleTabList] Animations config not found");
            return;
        }

        // Load all animations
        int animationId = 0;
        while (config.contains("animations." + animationId)) {
            List<String> frames = config.getStringList("animations." + animationId);
            if (!frames.isEmpty()) {
                animationFrames.put(String.valueOf(animationId), new ArrayList<>(frames));
            }
            animationId++;
        }

        Bukkit.getLogger().info("[SimpleTabList] Loaded " + animationFrames.size() + " animations into frame buffer");
    }

    /**
     * Gets current frame for an animation
     */
    public static String getCurrentFrame(String animationId) {
        List<String> frames = animationFrames.get(animationId);

        if (frames == null || frames.isEmpty()) {
            return "Unknown animation: " + animationId;
        }

        int frameCount = frames.size();
        int index = currentFrameIndex % frameCount;
        return frames.get(index);
    }

    /**
     * Increments frame index (called on each animation tick)
     */
    public static void nextFrame() {
        currentFrameIndex++;
        // Overflow protection
        if (currentFrameIndex > 10000) {
            currentFrameIndex = 0;
        }
    }

    /**
     * Resets frame index
     */
    public static void reset() {
        currentFrameIndex = 0;
    }

    /**
     * Returns current frame index
     */
    public static int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * Checks if an animation exists
     */
    public static boolean hasAnimation(String animationId) {
        return animationFrames.containsKey(animationId);
    }

    /**
     * Returns the number of frames for an animation
     */
    public static int getFrameCount(String animationId) {
        List<String> frames = animationFrames.get(animationId);
        return frames != null ? frames.size() : 0;
    }

    /**
     * Returns all loaded animation IDs
     */
    public static List<String> getLoadedAnimations() {
        return new ArrayList<>(animationFrames.keySet());
    }

    /**
     * Reloads all animations
     */
    public static void reload() {
        loadAnimations();
    }

    /**
     * Clears all animations
     */
    public static void clear() {
        animationFrames.clear();
        currentFrameIndex = 0;
    }
}