package me.manaki.plugin.dungeons.v4.config;

import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.sound.DSound;
import me.manaki.plugin.dungeons.sound.DSoundPlay;
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
    private int worldLastTime;

    private Map<String, DSound> sounds;
    private Map<String, DSoundPlay> soundPlays;

    public V4Config(Dungeons plugin) {
        this.plugin = plugin;
        this.worldTemplates = new HashMap<>();
        this.roomCountdown = 30;
        this.removeRoomCountdown = 5;
        this.worldLastTime = 3600;
        this.soundPlays = Maps.newConcurrentMap();
        this.sounds = Maps.newConcurrentMap();
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

        // Room
        roomCountdown = config.getInt("room.room-count-down");
        removeRoomCountdown = config.getInt("room.remove-room-count-down");
        worldLastTime = config.getInt("room.world-last-time");

        // Sound
        this.sounds.clear();
        for (String id : config.getConfigurationSection("sounds").getKeys(false)) {
            var source = config.getString("sounds." + id + ".source");
            long length = config.getLong("sounds." + id + ".length");
            var sound = new DSound(id, source, length);
            this.sounds.put(id, sound);
        }

        // Sound play
        this.soundPlays.clear();;
        for (String id : config.getConfigurationSection("sound-play").getKeys(false)) {
            int delay = config.getInt("sound-play." + id + ".delay");
            int times = config.getInt("sound-play." + id + ".play-times");
            var sounds = config.getStringList("sound-play." + id + ".sounds");
            var sp = new DSoundPlay(delay, times, sounds);
            this.soundPlays.put(id, sp);
        }

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

    public int getWorldLastTime() {
        return worldLastTime;
    }

    public DSound getSound(String id) {
        return getSounds().getOrDefault(id, null);
    }

    public Map<String, DSound> getSounds() {
        return sounds;
    }

    public DSoundPlay getSoundPlay(String event) {
        return soundPlays.getOrDefault(event, null);
    }

    public Map<String, DSoundPlay> getSoundPlays() {
        return soundPlays;
    }
}
