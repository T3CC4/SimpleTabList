package de.sesosas.simpletablist.command;

import de.sesosas.simpletablist.classes.scoreboard.SidebarClass;
import de.sesosas.simpletablist.config.SidebarConfig;
import de.sesosas.simpletablist.utils.MessageSender;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to toggle the sidebar visibility
 */
public class SidebarCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!SidebarConfig.getBoolean("Sidebar.Enable")) {
            MessageSender.Send(player, ChatColor.RED + "Sidebars are currently disabled on this server.");
            return true;
        }

        // Toggle the sidebar
        boolean isVisible = SidebarClass.toggleSidebar(player);

        if (isVisible) {
            MessageSender.Send(player, ChatColor.GREEN + "Sidebar is now visible.");
        } else {
            MessageSender.Send(player, ChatColor.YELLOW + "Sidebar is now hidden.");
        }

        return true;
    }
}