package de.sesosas.simpletablist.api.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Set;

public class Permission {

    public static String getPermissionString(Player player, String permission) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                Set<String> permissions = user.getCachedData().getPermissionData().getPermissionMap().keySet();

                for (String perm : permissions) {
                    if (perm.startsWith(permission)) {
                        return perm;
                    }
                }
            } else {
                Bukkit.getLogger().warning("LuckPerms: User not found for player " + player.getName());
            }
        } else {
            Bukkit.getLogger().warning("LuckPerms plugin is not installed or not enabled!");
        }
        return null;
    }

    public static boolean hasPermission(Player player, String permission) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
        }
        return false;
    }

    public static boolean hasPermissionEnabled(Player player, String permission){
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                if(user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()){
                    return Node.builder(permission).build().getValue();
                }
            }
        }
        return false;
    }

    public static void addPermission(Player player, String permission) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                user.data().add(Node.builder(permission).build());
                api.getUserManager().saveUser(user);
            }
        }
    }

    public static void removePermission(Player player, String permission){
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                user.data().remove(Node.builder(permission).build());
                api.getUserManager().saveUser(user);
            }
        }
    }
}
