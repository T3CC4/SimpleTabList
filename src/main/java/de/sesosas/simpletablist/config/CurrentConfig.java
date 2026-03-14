package de.sesosas.simpletablist.config;

import de.sesosas.simpletablist.SimpleTabList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class CurrentConfig {

    public static FileConfiguration config() {
        return SimpleTabList.getPlugin().config;
    }

    public static String getString(String name){
        return config().getString(name);
    }

    public static Boolean getBoolean(String name){
        return config().getBoolean(name);
    }

    public static List<?> getList(String name){
        return config().getList(name);
    }

    public static Integer getInt(String name){
        return config().getInt(name);
    }

    public static Long getLong(String name) {
        return config().getLong(name);
    }

    public static List<Float> getFloat(String name){
        return config().getFloatList(name);
    }

    public static Object get(String name){
        return config().get(name);
    }
}
