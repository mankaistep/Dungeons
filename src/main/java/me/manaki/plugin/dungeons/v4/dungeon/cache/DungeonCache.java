package me.manaki.plugin.dungeons.v4.dungeon.cache;

import me.manaki.plugin.dungeons.v4.world.WorldCache;

public class DungeonCache {

    private final String dungeonID;
    private final WorldCache worldCache;

    public DungeonCache(String dungeonID, WorldCache worldCache) {
        this.dungeonID = dungeonID;
        this.worldCache = worldCache;
    }

    public String getDungeonID() {
        return dungeonID;
    }

    public WorldCache getWorldCache() {
        return worldCache;
    }

    public String toID() {
        return this.getDungeonID() + " " + worldCache.toWorldName();
    }

    public static DungeonCache parse(String id) {
        var dungeonID = id.split(" ", 2)[0];
        var worldCache = WorldCache.parse(id.split(" ", 2)[1]);
        return new DungeonCache(dungeonID, worldCache);
    }

}
