package de.sesosas.simpletablist.animation;

import de.sesosas.simpletablist.SimpleTabList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Animation system with validation, error handling and performance optimization
 */
public class AnimationManager {

    private static final Pattern ANIMATION_PATTERN = Pattern.compile("\\{animation:([\\w-]+)(?::([\\w-]+))?}");
    private static final Map<String, Animation> animations = new ConcurrentHashMap<>();
    private static File animationsFile;
    private static FileConfiguration animationsConfig;
    private static boolean initialized = false;

    /**
     * Animation data class
     */
    public static class Animation {
        private final String id;
        private final List<String> frames;
        private final AnimationType type;
        private final int speed; // Multiplier for frame updates (1 = normal, 2 = half speed, etc.)
        private int currentFrame;
        private int tickCounter;

        public Animation(String id, List<String> frames, AnimationType type, int speed) {
            this.id = id;
            this.frames = new ArrayList<>(frames);
            this.type = type;
            this.speed = Math.max(1, speed);
            this.currentFrame = 0;
            this.tickCounter = 0;
        }

        public String getId() { return id; }
        public List<String> getFrames() { return frames; }
        public AnimationType getType() { return type; }
        public int getSpeed() { return speed; }
        public int getFrameCount() { return frames.size(); }

        /**
         * Get current frame
         */
        public String getCurrentFrame() {
            if (frames.isEmpty()) return "";
            return frames.get(currentFrame);
        }

        /**
         * Advance to next frame
         */
        public void nextFrame() {
            if (frames.isEmpty()) return;

            tickCounter++;
            if (tickCounter >= speed) {
                tickCounter = 0;

                switch (type) {
                    case LOOP:
                        currentFrame = (currentFrame + 1) % frames.size();
                        break;
                    case REVERSE_LOOP:
                        currentFrame--;
                        if (currentFrame < 0) {
                            currentFrame = frames.size() - 1;
                        }
                        break;
                    case BOUNCE:
                        // Will be handled by global bounce logic
                        break;
                    case RANDOM:
                        currentFrame = new Random().nextInt(frames.size());
                        break;
                }
            }
        }

        /**
         * Set specific frame
         */
        public void setFrame(int frame) {
            if (frame >= 0 && frame < frames.size()) {
                this.currentFrame = frame;
            }
        }

        /**
         * Reset to first frame
         */
        public void reset() {
            this.currentFrame = 0;
            this.tickCounter = 0;
        }
    }

    /**
     * Animation types
     */
    public enum AnimationType {
        LOOP,           // Normal loop: 1 -> 2 -> 3 -> 1
        REVERSE_LOOP,   // Reverse loop: 3 -> 2 -> 1 -> 3
        BOUNCE,         // Bounce: 1 -> 2 -> 3 -> 2 -> 1
        RANDOM          // Random frame each time
    }

    /**
     * Initialize animation system
     */
    public static void initialize() {
        if (initialized) {
            Bukkit.getLogger().warning("[SimpleTabList] AnimationManager already initialized");
            return;
        }

        animationsFile = new File(SimpleTabList.getPlugin().getDataFolder(), "animations.yml");

        if (!animationsFile.exists()) {
            createDefaultAnimations();
        }

        loadAnimations();
        initialized = true;

        Bukkit.getLogger().info("[SimpleTabList] AnimationManager initialized with " + animations.size() + " animations");
    }

    /**
     * Create default animations file
     */
    private static void createDefaultAnimations() {
        try {
            animationsFile.getParentFile().mkdirs();
            animationsFile.createNewFile();

            animationsConfig = YamlConfiguration.loadConfiguration(animationsFile);

            // Example 1: Simple color animation
            List<String> colorFrames = Arrays.asList(
                    "&c❤", "&6❤", "&e❤", "&a❤", "&b❤", "&9❤", "&d❤"
            );
            saveAnimation("hearts", colorFrames, AnimationType.LOOP, 1);

            // Example 2: Loading animation
            List<String> loadingFrames = Arrays.asList(
                    "&7[&e■&8□□□&7]",
                    "&7[&8□&e■&8□□&7]",
                    "&7[&8□□&e■&8□&7]",
                    "&7[&8□□□&e■&7]",
                    "&7[&8□□&e■&8□&7]",
                    "&7[&8□&e■&8□□&7]"
            );
            saveAnimation("loading", loadingFrames, AnimationType.LOOP, 1);

            // Example 3: Time of day
            List<String> timeFrames = Arrays.asList(
                    "&e☀ &fDay", "&6⛅ &fDusk", "&8🌙 &fNight", "&b🌅 &fDawn"
            );
            saveAnimation("time", timeFrames, AnimationType.LOOP, 5);

            // Example 4: Wave animation
            List<String> waveFrames = Arrays.asList(
                    "&b~&3~&9~&3~&b~",
                    "&3~&9~&3~&b~&3~",
                    "&9~&3~&b~&3~&9~",
                    "&3~&b~&3~&9~&3~",
                    "&b~&3~&9~&3~&b~"
            );
            saveAnimation("wave", waveFrames, AnimationType.LOOP, 2);

            // Example 5: Server info rotating
            List<String> infoFrames = Arrays.asList(
                    "&eWelcome to the server!",
                    "&aDon't forget to vote!",
                    "&bJoin our Discord!",
                    "&6Visit our website!"
            );
            saveAnimation("info", infoFrames, AnimationType.LOOP, 10);

            // Example 6: Bounce animation
            List<String> bounceFrames = Arrays.asList(
                    "&f█&7▓▒░",
                    "&7▓&f█&7▓▒",
                    "&7▒▓&f█&7▓",
                    "&7░▒▓&f█"
            );
            saveAnimation("bounce", bounceFrames, AnimationType.BOUNCE, 1);

            // Example 7: Random stars
            List<String> starFrames = Arrays.asList(
                    "&e✦", "&6✦", "&f✦", "&b✦", "&9✦"
            );
            saveAnimation("stars", starFrames, AnimationType.RANDOM, 1);

            animationsConfig.save(animationsFile);
            Bukkit.getLogger().info("[SimpleTabList] Created default animations.yml with examples");

        } catch (IOException e) {
            Bukkit.getLogger().severe("[SimpleTabList] Failed to create animations.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save animation to config
     */
    private static void saveAnimation(String id, List<String> frames, AnimationType type, int speed) {
        String path = "animations." + id;
        animationsConfig.set(path + ".frames", frames);
        animationsConfig.set(path + ".type", type.name());
        animationsConfig.set(path + ".speed", speed);
    }

    /**
     * Load all animations from config
     */
    public static void loadAnimations() {
        animations.clear();

        if (!animationsFile.exists()) {
            Bukkit.getLogger().warning("[SimpleTabList] animations.yml not found");
            return;
        }

        animationsConfig = YamlConfiguration.loadConfiguration(animationsFile);
        ConfigurationSection animSection = animationsConfig.getConfigurationSection("animations");

        if (animSection == null) {
            Bukkit.getLogger().warning("[SimpleTabList] No animations section found in animations.yml");
            return;
        }

        for (String id : animSection.getKeys(false)) {
            try {
                String path = "animations." + id;

                List<String> frames = animationsConfig.getStringList(path + ".frames");
                if (frames.isEmpty()) {
                    Bukkit.getLogger().warning("[SimpleTabList] Animation '" + id + "' has no frames, skipping");
                    continue;
                }

                String typeStr = animationsConfig.getString(path + ".type", "LOOP");
                AnimationType type;
                try {
                    type = AnimationType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[SimpleTabList] Invalid animation type '" + typeStr + "' for animation '" + id + "', using LOOP");
                    type = AnimationType.LOOP;
                }

                int speed = animationsConfig.getInt(path + ".speed", 1);
                if (speed < 1) {
                    Bukkit.getLogger().warning("[SimpleTabList] Invalid speed " + speed + " for animation '" + id + "', using 1");
                    speed = 1;
                }

                Animation animation = new Animation(id, frames, type, speed);
                animations.put(id, animation);

            } catch (Exception e) {
                Bukkit.getLogger().warning("[SimpleTabList] Failed to load animation '" + id + "': " + e.getMessage());
            }
        }

        Bukkit.getLogger().info("[SimpleTabList] Loaded " + animations.size() + " animations");
    }

    /**
     * Reload animations from file
     */
    public static void reload() {
        loadAnimations();
    }

    /**
     * Process text and replace animation placeholders
     */
    public static String processAnimations(String text) {
        if (text == null || text.isEmpty() || !text.contains("{animation:")) {
            return text;
        }

        Matcher matcher = ANIMATION_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String animationId = matcher.group(1);
            String frameStr = matcher.group(2);

            String replacement;

            if (frameStr != null) {
                // Static frame request: {animation:id:5}
                try {
                    int frameIndex = Integer.parseInt(frameStr);
                    replacement = getStaticFrame(animationId, frameIndex);
                } catch (NumberFormatException e) {
                    replacement = "{animation:" + animationId + ":" + frameStr + "}";
                }
            } else {
                // Dynamic animation: {animation:id}
                replacement = getCurrentFrame(animationId);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Get current frame of an animation
     */
    public static String getCurrentFrame(String id) {
        Animation animation = animations.get(id);
        if (animation == null) {
            return "&c[Unknown animation: " + id + "]";
        }
        return animation.getCurrentFrame();
    }

    /**
     * Get specific frame of an animation
     */
    public static String getStaticFrame(String id, int frame) {
        Animation animation = animations.get(id);
        if (animation == null) {
            return "&c[Unknown animation: " + id + "]";
        }

        List<String> frames = animation.getFrames();
        if (frame < 0 || frame >= frames.size()) {
            return "&c[Invalid frame: " + frame + "]";
        }

        return frames.get(frame);
    }

    /**
     * Advance all animations to next frame
     */
    public static void tick() {
        for (Animation animation : animations.values()) {
            animation.nextFrame();
        }
    }

    /**
     * Reset all animations
     */
    public static void resetAll() {
        for (Animation animation : animations.values()) {
            animation.reset();
        }
    }

    /**
     * Get animation by ID
     */
    public static Animation getAnimation(String id) {
        return animations.get(id);
    }

    /**
     * Get all animation IDs
     */
    public static Set<String> getAnimationIds() {
        return new HashSet<>(animations.keySet());
    }

    /**
     * Check if animation exists
     */
    public static boolean hasAnimation(String id) {
        return animations.containsKey(id);
    }

    /**
     * Get number of loaded animations
     */
    public static int getAnimationCount() {
        return animations.size();
    }

    /**
     * Validate animation configuration
     */
    public static List<String> validateAnimations() {
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, Animation> entry : animations.entrySet()) {
            String id = entry.getKey();
            Animation anim = entry.getValue();

            if (anim.getFrames().isEmpty()) {
                errors.add("Animation '" + id + "' has no frames");
            }

            if (anim.getSpeed() < 1) {
                errors.add("Animation '" + id + "' has invalid speed: " + anim.getSpeed());
            }

            // Check for empty frames
            for (int i = 0; i < anim.getFrames().size(); i++) {
                String frame = anim.getFrames().get(i);
                if (frame == null || frame.isEmpty()) {
                    errors.add("Animation '" + id + "' has empty frame at index " + i);
                }
            }
        }

        return errors;
    }

    /**
     * Get animation info for debugging
     */
    public static String getAnimationInfo(String id) {
        Animation animation = animations.get(id);
        if (animation == null) {
            return "Animation '" + id + "' not found";
        }

        return String.format(
                "Animation '%s': %d frames, type=%s, speed=%d, current=%d",
                id,
                animation.getFrameCount(),
                animation.getType(),
                animation.getSpeed(),
                animation.currentFrame
        );
    }

    /**
     * Check if system is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}