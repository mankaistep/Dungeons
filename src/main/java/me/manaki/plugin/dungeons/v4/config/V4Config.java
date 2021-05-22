package me.manaki.plugin.dungeons.v4.config;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.v4.world.WorldTemplate;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class V4Config {

    private final Dungeons plugin;
    
    private Map<String, WorldTemplate> worldTemplates;
    private int roomCountdown;
    private int removeRoomCountdown;

    public V4Config(Dungeons plugin) {
        this.plugin = plugin;
        this.worldTemplates = new HashMap<>();
        this.roomCountdown = 30;
        this.removeRoomCountdown = 5;
    }
    
    public void reload() {
        var config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        var getter = new ConfigGetter(config);

        // World Templates
        worldTemplates.clear();
        for (String world : config.getConfigurationSection("world").getKeys(false)) {
            var path = "world." + world;
            var seed = getter.getInt(path + ".seed", 0);
            var environment = World.Environment.valueOf(getter.getString(path + ".environment", "NORMAL"));
            var type = config.contains(path + ".type") ? WorldType.valueOf(getter.getString(path + ".type")) : null;
            var generator = getter.getString(".generator");
            worldTemplates.put(world, new WorldTemplate(world, seed, environment, type, generator));
        }

        roomCountdown = config.getInt("room-count-down");

        removeRoomCountdown = config.getInt("remove-room-count-down");
    }

    public WorldTemplate getWorldTemplate(String id) {
        return worldTemplates.getOrDefault(id, new WorldTemplate(id));
    }

    public Map<String, WorldTemplate> getWorldTemplates() {
        return worldTemplates;
    }

    public int getRoomCountdown() {
        return roomCountdown;
    }

    public int getRemoveRoomCountdown() {
        return removeRoomCountdown;
    }
}
