package de.sesosas.simpletablist.classes.scoreboard;

import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.api.utils.StringUtil;
import de.sesosas.simpletablist.api.utils.WorldUtil;
import de.sesosas.simpletablist.config.CurrentConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * DetailsClass using new AnimationManager
 */
public class DetailsClass {

    public static synchronized void updateTab(Player player) {
        try {
            if (!CurrentConfig.getBoolean("Worlds.Enable")) {
                updateTabForWorld(player, (List<String>) CurrentConfig.getList("Header.Content"), (List<String>) CurrentConfig.getList("Footer.Content"));
            } else {
                List<String> headerContent = (List<String>) WorldUtil.GetWorldConfig(player.getWorld(), "Header.Content");
                List<String> footerContent = (List<String>) WorldUtil.GetWorldConfig(player.getWorld(), "Footer.Content");

                updateTabForWorld(player, headerContent, footerContent);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SimpleTabList] Found an error at Header or Footer config section! Please make sure there are lists with content!");
            Bukkit.getLogger().warning("[SimpleTabList] Error details: " + e.getMessage());
        }
    }

    private static void updateTabForWorld(Player player, List<String> headerContent, List<String> footerContent) {
        updateTabContent(player, "Header", headerContent, true);
        updateTabContent(player, "Footer", footerContent, false);
    }

    private static void updateTabContent(Player player, String section, List<String> contentList, boolean isHeader) {
        if (CurrentConfig.getBoolean(section + ".Enable") && contentList != null) {
            String contentString = buildContentString(contentList, isHeader);

            // Process animations using new AnimationManager
            String processedContent = AnimationManager.processAnimations(contentString);

            // Process placeholders and colors
            String finalContent = StringUtil.Convert(processedContent, player);

            if (isHeader) {
                player.setPlayerListHeader(finalContent);
            } else {
                player.setPlayerListFooter(finalContent);
            }
        } else {
            if (isHeader) {
                player.setPlayerListHeader("");
            } else {
                player.setPlayerListFooter("");
            }
        }
    }

    private static String buildContentString(List<String> contentList, boolean isHeader) {
        StringBuilder contentString = new StringBuilder();
        for (Object str : contentList) {
            if (isHeader) {
                contentString.append(str).append("\n");
            } else {
                contentString.append("\n").append(str);
            }
        }
        return contentString.toString();
    }
}