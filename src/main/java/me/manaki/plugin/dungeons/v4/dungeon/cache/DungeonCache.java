package me.manaki.plugin.dungeons.v4.dungeon.cache;

import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.v4.world.WorldCache;

import java.util.zip.DeflaterInputStream;

public class DungeonCache {

    private final String dungeonID;
    private final Difficulty difficulty;
    private final WorldCache worldCache;

    public DungeonCache(String dungeonID, Difficulty difficulty, WorldCache worldCache) {
        this.dungeonID = dungeonID;
        this.difficulty = difficulty;
        this.worldCache = worldCache;
    }

    public String getDungeonID() {
        return dungeonID;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public WorldCache getWorldCache() {
        return worldCache;
    }

    public String toID() {
        var dct = this.difficulty == null ? "null" : this.difficulty.name();
        return this.getDungeonID() + " " + dct + " " + worldCache.toWorldName();
    }

    public static DungeonCache parse(String id) {
        var dungeonID = id.split(" ", 2)[0];

        Difficulty difficulty = null;
        var arg2 = id.split(" ", 3)[1];
        if (!arg2.equalsIgnoreCase("null")) difficulty = Difficulty.valueOf(arg2);

        var worldCache = WorldCache.parse(id.split(" ", 3)[2]);

        return new DungeonCache(dungeonID, difficulty, worldCache);
    }

}
