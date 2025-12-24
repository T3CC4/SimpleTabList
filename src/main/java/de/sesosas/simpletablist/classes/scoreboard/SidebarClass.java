package de.sesosas.simpletablist.classes.scoreboard;

import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.api.luckperms.Permission;
import de.sesosas.simpletablist.api.utils.StringUtil;
import de.sesosas.simpletablist.config.SidebarConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Updated sidebar class with new AnimationManager
 */
public class SidebarClass {
    private static final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private static final Set<UUID> disabledSidebars = new HashSet<>();
    private static boolean isPerPlayerEnabled = false;
    private static String perPlayerPermission = "stl.sidebar";

    public static void initialize() {
        isPerPlayerEnabled = SidebarConfig.getBoolean("Sidebar.PerPlayer.Enable");
        perPlayerPermission = SidebarConfig.getString("Sidebar.PerPlayer.Permission");

        Bukkit.getLogger().info("[SimpleTabList] Sidebar manager initialized");

        // Initialize sidebars for all online players IMMEDIATELY
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateSidebar(player);
        }
        Bukkit.getLogger().info("[SimpleTabList] Initialized sidebars for " + Bukkit.getOnlinePlayers().size() + " players");
    }

    private static boolean shouldHaveSidebar(Player player) {
        if (disabledSidebars.contains(player.getUniqueId())) {
            return false;
        }

        if (!SidebarConfig.getBoolean("Sidebar.Enable")) {
            return false;
        }

        if (isPerPlayerEnabled && !Permission.hasPermission(player, perPlayerPermission)) {
            return false;
        }

        String worldName = player.getWorld().getName();
        if (SidebarConfig.getBoolean("Sidebar.PerWorld.Enable")) {
            return SidebarConfig.getWorldBoolean(worldName, "Sidebar.Enable");
        }

        return true;
    }

    public static void updateSidebar(Player player) {
        if (!shouldHaveSidebar(player)) {
            removeSidebar(player);
            return;
        }

        try {
            Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());

            if (scoreboard == null) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                if (manager == null) {
                    Bukkit.getLogger().warning("[SimpleTabList] ScoreboardManager is null!");
                    return;
                }

                scoreboard = manager.getNewScoreboard();
                playerScoreboards.put(player.getUniqueId(), scoreboard);
                player.setScoreboard(scoreboard);
            }

            Objective oldObjective = scoreboard.getObjective("stlsidebar");
            if (oldObjective != null) {
                oldObjective.unregister();
            }

            String worldName = player.getWorld().getName();
            String title;

            if (SidebarConfig.getBoolean("Sidebar.PerWorld.Enable") &&
                    SidebarConfig.getWorldValue(worldName, "Sidebar.Title") != null) {
                title = SidebarConfig.getWorldString(worldName, "Sidebar.Title");
            } else {
                title = SidebarConfig.getString("Sidebar.Title");
            }

            // Process title: placeholders first, then animations
            title = StringUtil.Convert(title, player);
            title = AnimationManager.processAnimations(title);

            Objective sidebar = scoreboard.registerNewObjective("stlsidebar", "dummy", title);
            sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<String> lines;
            if (SidebarConfig.getBoolean("Sidebar.PerWorld.Enable") &&
                    SidebarConfig.getWorldValue(worldName, "Sidebar.Lines") != null) {
                lines = SidebarConfig.getWorldStringList(worldName, "Sidebar.Lines");
            } else {
                lines = SidebarConfig.getStringList("Sidebar.Lines");
            }

            if (lines != null && !lines.isEmpty()) {
                String blankLineChar = SidebarConfig.getString("Sidebar.Format.BlankLineChar");
                if (blankLineChar == null || blankLineChar.isEmpty()) {
                    blankLineChar = " ";
                }

                boolean lineSpacing = SidebarConfig.getBoolean("Sidebar.Format.LineSpacing");

                int score = lines.size();
                for (String line : lines) {
                    // Process placeholders and colors first, then animations
                    String processedLine = StringUtil.Convert(line, player);
                    processedLine = AnimationManager.processAnimations(processedLine);

                    if (processedLine.length() <= 2 && processedLine.startsWith("&")) {
                        processedLine = blankLineChar;
                    }

                    if (lineSpacing && !processedLine.equals(blankLineChar)) {
                        processedLine = " " + processedLine + " ";
                    }

                    if (processedLine.length() > 0) {
                        String uniqueLine = makeLineUnique(processedLine, score);
                        Score lineScore = sidebar.getScore(uniqueLine);
                        lineScore.setScore(score);
                    }
                    score--;
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Error updating sidebar for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String makeLineUnique(String line, int index) {
        ChatColor lastColor = ChatColor.WHITE;
        for (int i = line.length() - 2; i >= 0; i--) {
            if (line.charAt(i) == '§' && i + 1 < line.length()) {
                char colorChar = line.charAt(i + 1);
                ChatColor color = ChatColor.getByChar(colorChar);
                if (color != null) {
                    lastColor = color;
                    break;
                }
            }
        }

        String uniqueStr = "";
        for (int i = 0; i < index % 3; i++) {
            uniqueStr += ChatColor.RESET.toString();
        }

        int maxLength = 40;
        if (line.length() > maxLength) {
            line = line.substring(0, maxLength - uniqueStr.length());
        }

        return line + uniqueStr + lastColor.toString();
    }

    public static void removeSidebar(Player player) {
        try {
            Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
            if (scoreboard != null) {
                Objective objective = scoreboard.getObjective("stlsidebar");
                if (objective != null) {
                    objective.unregister();
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Error removing sidebar for " + player.getName() + ": " + e.getMessage());
        }
    }

    public static void removeAllSidebars() {
        for (UUID uuid : playerScoreboards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                removeSidebar(player);
            }
        }
        playerScoreboards.clear();
        disabledSidebars.clear();
    }

    public static void resetPlayerScoreboard(Player player) {
        removeSidebar(player);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }
        playerScoreboards.remove(player.getUniqueId());
    }

    public static boolean toggleSidebar(Player player) {
        UUID playerUuid = player.getUniqueId();

        if (disabledSidebars.contains(playerUuid)) {
            disabledSidebars.remove(playerUuid);
            updateSidebar(player);
            return true;
        } else {
            disabledSidebars.add(playerUuid);
            removeSidebar(player);
            return false;
        }
    }

    public static boolean isSidebarDisabled(Player player) {
        return disabledSidebars.contains(player.getUniqueId());
    }

    public static void handlePlayerQuit(Player player) {
        playerScoreboards.remove(player.getUniqueId());
    }
}