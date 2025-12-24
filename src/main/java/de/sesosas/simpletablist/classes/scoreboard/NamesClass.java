package de.sesosas.simpletablist.classes.scoreboard;

import de.sesosas.simpletablist.SimpleTabList;
import de.sesosas.simpletablist.api.luckperms.Group;
import de.sesosas.simpletablist.api.luckperms.Permission;
import de.sesosas.simpletablist.api.utils.StringUtil;
import de.sesosas.simpletablist.config.CurrentConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamesClass {
    private static org.bukkit.scoreboard.Scoreboard mainScoreboard;
    // Map to store original player scoreboards
    private static final Map<Player, Scoreboard> originalScoreboards = new HashMap<>();

    public static void initScoreboard() {
        mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public static void updatePlayerName(Player player) {
        if(Permission.getPermissionString(player, "stl.format.") != null) {
            player.setPlayerListName(StringUtil.Convert(Permission.getPermissionString(player, "stl.format."), player));
        }
        else{
            player.setPlayerListName(StringUtil.Convert(CurrentConfig.getString("Names.Format.Default"), player));
        }

        sortPlayer(player);
    }

    public static void sortPlayer(Player player) {
        if (CurrentConfig.getBoolean("Names.Sorting.Enable")) {
            boolean isAscending = CurrentConfig.getBoolean("Names.Sorting.Ascending");
            String sortType = CurrentConfig.getString("Names.Sorting.Type");

            assignPlayerToTeam(player, sortType, isAscending);
        } else {
            // If sorting is disabled, make sure we're not affecting the player's scoreboard
            restoreOriginalScoreboard(player);
        }
    }

    private static void assignPlayerToTeam(Player player, String sortType, boolean isAscending) {
        // Store original scoreboard if we haven't already
        if (!originalScoreboards.containsKey(player)) {
            originalScoreboards.put(player, player.getScoreboard());
        }

        // Remove player from any existing teams on the main scoreboard
        for (Team team : mainScoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                Bukkit.getScheduler().runTask(SimpleTabList.getPlugin(), () -> team.removeEntry(player.getName()));
            }
        }

        String teamName = "STL";

        if (sortType.equalsIgnoreCase("weight")) {
            int playerWeight = Group.getPlayerGroupWeight(player);
            int sortingPrefix = isAscending ? playerWeight : Integer.MAX_VALUE - playerWeight;
            teamName = "STL_" + formatNumber(sortingPrefix);
        }

        // Ensure team name is unique and valid
        teamName = ensureValidTeamName(teamName, player);

        Team team = mainScoreboard.getTeam(teamName);

        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
        }
        Team finalTeam = team;
        Bukkit.getScheduler().runTask(SimpleTabList.getPlugin(), () -> finalTeam.addEntry(player.getName()));

        // Important: we're NOT setting player.setScoreboard(scoreboard) here
        // That's what was causing the conflict with other plugins
    }

    private static String formatNumber(int number) {
        // Format number to ensure sorting works correctly
        return String.format("%05d", number);
    }

    private static String ensureValidTeamName(String baseName, Player player) {
        // Team names must be at most 16 characters
        String teamName = baseName;
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        // Ensure uniqueness if needed
        int counter = 0;
        String tempName = teamName;
        while (mainScoreboard.getTeam(tempName) != null) {
            counter++;
            String suffix = "_" + counter;
            tempName = teamName.substring(0, Math.min(16 - suffix.length(), teamName.length())) + suffix;
        }
        return tempName;
    }

    public static void resetPlayerNames() {
        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : playerList) {
            player.setPlayerListName(player.getName());
            restoreOriginalScoreboard(player);
        }

        // Clean up any STL teams
        cleanupSTLTeams();
    }

    private static void restoreOriginalScoreboard(Player player) {
        // Remove player from STL teams
        for (Team team : mainScoreboard.getTeams()) {
            if (team.getName().startsWith("STL") && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }

        // If we stored their original scoreboard, restore it
        if (originalScoreboards.containsKey(player)) {
            // We don't actually reset it to avoid interfering with other plugins
            originalScoreboards.remove(player);
        }
    }

    private static void cleanupSTLTeams() {
        // Clean up empty STL teams
        for (Team team : new ArrayList<>(mainScoreboard.getTeams())) {
            if (team.getName().startsWith("STL") && team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
    }

    // Called when a player leaves the server
    public static void handlePlayerQuit(Player player) {
        restoreOriginalScoreboard(player);
        originalScoreboards.remove(player);
    }
}