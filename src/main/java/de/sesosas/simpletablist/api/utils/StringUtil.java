package de.sesosas.simpletablist.api.utils;

import de.sesosas.simpletablist.config.CurrentConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class StringUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9A-Fa-f])");

    private static String ph(String text) {
        return "[" + text + "]";
    }

    public static String Convert(String text, Player player) {
        if (text == null) return null;

        String result = text;

        if(result.startsWith("stl.format.")){
            result = ensureCapitalization(result);
            result = result.replace("stl.format.", "");
        }

        result = result.replace(ph("player_name"), player.getName());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        if (CurrentConfig.getBoolean("Worlds.Enable")) {
            String worldPrefix = (String) WorldUtil.GetWorldConfig(player.getWorld(), "Names.Prefix");
            String worldSuffix = (String) WorldUtil.GetWorldConfig(player.getWorld(), "Names.Suffix");
            result = result.replace(ph("world_prefix"), worldPrefix != null ? worldPrefix : "");
            result = result.replace(ph("world_suffix"), worldSuffix != null ? worldSuffix : "");
        }

        if (CurrentConfig.getBoolean("Names.Global.Enable")) {
            String globalPrefix = CurrentConfig.getString("Names.Global.Prefix");
            String globalSuffix = CurrentConfig.getString("Names.Global.Suffix");
            result = result.replace(ph("global_prefix"), globalPrefix != null ? globalPrefix : "");
            result = result.replace(ph("global_suffix"), globalSuffix != null ? globalSuffix : "");
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        result = result.replace(ph("player_health"), df.format(Objects.requireNonNull(player).getHealth()));
        result = result.replace(ph("player_food"), df.format(player.getFoodLevel()));
        result = result.replace(ph("player_xp"), df.format(player.getExp()));
        result = result.replace(ph("player_lvl"), Integer.toString(player.getLevel()));
        result = result.replace(ph("player_gamemode"), player.getGameMode().toString());

        result = hex(result);

        return result;
    }

    public static String hex(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }

        return customTranslateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String ensureCapitalization(String text) {
        Pattern capitalizePattern = Pattern.compile("\\[#(?i)(cap)\\((\\w+)\\)]|\\[#(?i)(capall)\\((\\w+)\\)]");
        Matcher matcher = capitalizePattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String capitalized = null;

            if (matcher.group(1) != null) {
                String word = matcher.group(2);
                capitalized = word.substring(0, 1).toUpperCase() + word.substring(1);
            } else if (matcher.group(3) != null) {
                String word = matcher.group(4);
                capitalized = word.toUpperCase();
            }

            // Replace the marker with the processed word
            matcher.appendReplacement(buffer, capitalized);
        }

        matcher.appendTail(buffer);

        return buffer.toString();
    }


    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

}
