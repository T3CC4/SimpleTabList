package de.sesosas.simpletablist.command;

import de.sesosas.simpletablist.SimpleTabList;
import de.sesosas.simpletablist.animation.AnimationManager;
import de.sesosas.simpletablist.classes.ScoreboardClass;
import de.sesosas.simpletablist.cache.PlayerDataCache;
import de.sesosas.simpletablist.config.CurrentConfig;
import de.sesosas.simpletablist.config.SidebarConfig;
import de.sesosas.simpletablist.api.classes.AInterval;
import de.sesosas.simpletablist.api.utils.ThreadUtil;
import de.sesosas.simpletablist.utils.MessageSender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Reload command with cache management
 */
public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Clear player data cache before reload
            int cacheSize = PlayerDataCache.size();
            PlayerDataCache.clearAll();

            // Reload main config
            File file = new File(SimpleTabList.getPlugin().getDataFolder().getAbsolutePath() + "/config.yml");
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            SimpleTabList.getPlugin().config = cfg;

            // Reload sidebar config
            SidebarConfig.reloadConfig();

            // Reload animations
            AnimationManager.reload();

            // Stop and restart intervals with new configuration
            AInterval.stopAllIntervals();
            AInterval.startAllIntervals(SimpleTabList.getPlugin());

            // Update all scoreboards (will rebuild cache)
            ThreadUtil.runOnMainThread(ScoreboardClass::Update);

            // Send confirmation message
            String text = "Successfully reloaded all configurations! Cleared " + cacheSize + " cached entries.";
            MessageSender.Send(player, ChatColor.AQUA + text);
        } else {
            // Console reload
            int cacheSize = PlayerDataCache.size();
            PlayerDataCache.clearAll();

            File file = new File(SimpleTabList.getPlugin().getDataFolder().getAbsolutePath() + "/config.yml");
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            SimpleTabList.getPlugin().config = cfg;

            SidebarConfig.reloadConfig();
            AnimationManager.reload();

            AInterval.stopAllIntervals();
            AInterval.startAllIntervals(SimpleTabList.getPlugin());

            ThreadUtil.runOnMainThread(ScoreboardClass::Update);

            Bukkit.getLogger().info("[SimpleTabList] All configurations reloaded successfully! Cleared " + cacheSize + " cached entries.");
        }
        return true;
    }
}