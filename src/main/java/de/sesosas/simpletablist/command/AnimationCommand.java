package de.sesosas.simpletablist.command;

import de.sesosas.simpletablist.animation.AnimationManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Command to debug and manage animations
 */
public class AnimationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listAnimations(sender);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /animation info <id>");
                    return true;
                }
                showAnimationInfo(sender, args[1]);
                break;
            case "validate":
                validateAnimations(sender);
                break;
            case "reload":
                reloadAnimations(sender);
                break;
            case "test":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /animation test <id>");
                    return true;
                }
                testAnimation(sender, args[1]);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== Animation Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/animation list" + ChatColor.GRAY + " - List all animations");
        sender.sendMessage(ChatColor.YELLOW + "/animation info <id>" + ChatColor.GRAY + " - Show animation details");
        sender.sendMessage(ChatColor.YELLOW + "/animation validate" + ChatColor.GRAY + " - Validate all animations");
        sender.sendMessage(ChatColor.YELLOW + "/animation reload" + ChatColor.GRAY + " - Reload animations.yml");
        sender.sendMessage(ChatColor.YELLOW + "/animation test <id>" + ChatColor.GRAY + " - Test an animation");
    }

    private void listAnimations(CommandSender sender) {
        Set<String> ids = AnimationManager.getAnimationIds();

        if (ids.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No animations loaded!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== Loaded Animations ===");
        sender.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + ids.size());
        sender.sendMessage("");

        for (String id : ids) {
            AnimationManager.Animation anim = AnimationManager.getAnimation(id);
            if (anim != null) {
                sender.sendMessage(ChatColor.YELLOW + id + ChatColor.GRAY +
                        " - " + anim.getFrameCount() + " frames, " +
                        "type: " + anim.getType() + ", " +
                        "speed: " + anim.getSpeed());
            }
        }
    }

    private void showAnimationInfo(CommandSender sender, String id) {
        AnimationManager.Animation anim = AnimationManager.getAnimation(id);

        if (anim == null) {
            sender.sendMessage(ChatColor.RED + "Animation '" + id + "' not found!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== Animation: " + id + " ===");
        sender.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.WHITE + anim.getType());
        sender.sendMessage(ChatColor.GRAY + "Speed: " + ChatColor.WHITE + anim.getSpeed());
        sender.sendMessage(ChatColor.GRAY + "Frames: " + ChatColor.WHITE + anim.getFrameCount());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Frame Preview:");

        List<String> frames = anim.getFrames();
        for (int i = 0; i < Math.min(frames.size(), 10); i++) {
            sender.sendMessage(ChatColor.GRAY + "  " + i + ": " + ChatColor.translateAlternateColorCodes('&', frames.get(i)));
        }

        if (frames.size() > 10) {
            sender.sendMessage(ChatColor.GRAY + "  ... and " + (frames.size() - 10) + " more frames");
        }
    }

    private void validateAnimations(CommandSender sender) {
        List<String> errors = AnimationManager.validateAnimations();

        if (errors.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "✓ All animations are valid!");
            return;
        }

        sender.sendMessage(ChatColor.RED + "Found " + errors.size() + " validation issues:");
        for (String error : errors) {
            sender.sendMessage(ChatColor.RED + "  ✗ " + error);
        }
    }

    private void reloadAnimations(CommandSender sender) {
        try {
            int oldCount = AnimationManager.getAnimationCount();
            AnimationManager.reload();
            int newCount = AnimationManager.getAnimationCount();

            sender.sendMessage(ChatColor.GREEN + "Reloaded animations: " + oldCount + " → " + newCount);

            // Validate after reload
            List<String> errors = AnimationManager.validateAnimations();
            if (!errors.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "⚠ Found " + errors.size() + " validation issues after reload");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading animations: " + e.getMessage());
        }
    }

    private void testAnimation(CommandSender sender, String id) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        AnimationManager.Animation anim = AnimationManager.getAnimation(id);
        if (anim == null) {
            sender.sendMessage(ChatColor.RED + "Animation '" + id + "' not found!");
            return;
        }

        Player player = (Player) sender;
        player.sendMessage(ChatColor.GOLD + "Testing animation: " + id);
        player.sendMessage(ChatColor.GRAY + "Frames will be shown for 10 seconds...");

        // Show all frames in quick succession
        List<String> frames = anim.getFrames();
        for (int i = 0; i < frames.size(); i++) {
            final int frameIndex = i;
            final String frame = ChatColor.translateAlternateColorCodes('&', frames.get(i));

            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("SimpleTabList"),
                    () -> player.sendMessage(ChatColor.GRAY + "Frame " + frameIndex + ": " + frame),
                    i * 10L
            );
        }
    }
}