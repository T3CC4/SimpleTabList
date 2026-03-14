package de.sesosas.simpletablist.utils;

import de.sesosas.simpletablist.config.CurrentConfig;
import de.sesosas.simpletablist.api.utils.MessageUtil;
import org.bukkit.entity.Player;

public class MessageSender {

    public static void Send(Player player, String text){
        MessageUtil.Send(player, text, CurrentConfig.getBoolean("Chat.ActionbarMessage"));
    }
}
