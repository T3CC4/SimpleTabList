package de.sesosas.simpletablist.api.utils;

import de.sesosas.simpletablist.config.CurrentConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class MessageUtil {
    public static void Send(Player player, String text, boolean actionbar){
        String prefix = CurrentConfig.getString("Chat.Prefix");
        if(actionbar){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(prefix + " " + StringUtil.Convert(text, player)));
        }
        else{
            player.sendMessage(prefix + " " + StringUtil.Convert(text, player));
        }
    }
}
