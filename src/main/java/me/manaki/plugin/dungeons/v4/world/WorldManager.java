package me.manaki.plugin.dungeons.v4.world;

import me.manaki.plugin.dungeons.Dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldManager {

    private final Dungeons plugin;

    private final List<WorldCache> activeWorlds;

    public WorldManager(Dungeons plugin) {
        this.plugin = plugin;
        this.activeWorlds = new ArrayList<>();
    }

    public List<String> getActiveWorldNames(String dungeonID) {
        return activeWorlds.stream().map(WorldCache::toWorldName).collect(Collectors.toList());
    }

    public void addActiveWorld(WorldCache cache) {
        activeWorlds.add(cache);
    }

    public void removeActiveWorld(WorldCache cache) { ;
        activeWorlds.remove(cache);
    }

}
