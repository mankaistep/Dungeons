package me.manaki.plugin.dungeons.v4.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldCache {

    private final int id;
    private final String worldSource;

    public WorldCache(int id, String worldSource) {
        this.id = id;
        this.worldSource = worldSource;
    }

    public int getId() {
        return id;
    }

    public String getWorldSource() {
        return worldSource;
    }

    public String toWorldName() {
        return worldSource + "_" + id;
    }

    public World toWorld() {
        return Bukkit.getWorld(toWorldName());
    }

    public static WorldCache parse(String s) {
        var worldSource = s.split(" ")[0];
        var id = Integer.parseInt(s.split(" ")[1]);
        return new WorldCache(id, worldSource);
    }

}
