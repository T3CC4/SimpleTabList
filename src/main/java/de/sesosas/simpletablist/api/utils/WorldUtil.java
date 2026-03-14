package de.sesosas.simpletablist.api.utils;

import de.sesosas.simpletablist.SimpleTabList;
import de.sesosas.simpletablist.config.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class WorldUtil {
    public static void GenerateWorldConfig() {
        for (World world : Bukkit.getWorlds()) {
            String configPath = "worlds/" + world.getName();
            CustomConfig cf = new CustomConfig().setup(configPath);

            if (cf.isEmpty() || !cf.exist(configPath)) {
                FileConfiguration con = cf.get();

                con.set("Names.Enable", true);
                con.set("Header.Enable", true);
                con.set("Footer.Enable", true);
                con.set("Names.Prefix", "[" + world.getName() + "]");
                con.set("Names.Suffix", "");
                con.set("Header.Content", new String[]{"This is a Header!", "Welcome %player_name%!"});
                con.set("Footer.Content", new String[]{"This is a Footer!", "This is Footer line 2!"});

                try {
                    con.save(new File(Bukkit.getServer().getPluginManager()
                            .getPlugin(SimpleTabList.getPlugin().getName()).getDataFolder(), configPath + ".yml"));
                } catch (IOException e) {
                    Bukkit.getLogger().severe("Error saving world configuration for " + world.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object GetWorldConfig(World world, String path) {
        String configPath = "worlds/" + world.getName();
        CustomConfig cf = new CustomConfig().setup(configPath);
        FileConfiguration con = cf.get();
        return con.get(path);
    }
}
