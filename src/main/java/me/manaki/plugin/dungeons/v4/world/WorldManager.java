package me.manaki.plugin.dungeons.v4.world;

import me.manaki.plugin.dungeons.Dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldManager {

    private final Dungeons plugin;

    private final Map<String, List<WorldCache>> activeWorlds;

    public WorldManager(Dungeons plugin) {
        this.plugin = plugin;
        this.activeWorlds = new HashMap<>();
    }

    public List<WorldCache> getActiveWorlds(String dungeonID) {
        return activeWorlds.getOrDefault(dungeonID, new ArrayList<>());
    }

    public List<String> getActiveWorldNames(String dungeonID) {
        return activeWorlds.getOrDefault(dungeonID, new ArrayList<>()).stream().map(w -> w.toWorldName()).collect(Collectors.toList());
    }

    public Map<String, List<WorldCache>> getActiveWorlds() {
        return activeWorlds;
    }

    public void addActiveWorld(String dungeonID, WorldCache cache) {
        var list = getActiveWorlds(dungeonID);
        list.add(cache);
        activeWorlds.put(dungeonID, list);
    }

    public void removeActiveWorld(String dungeonID, WorldCache cache) {
        var list = getActiveWorlds(dungeonID);
        list.remove(cache);
        activeWorlds.put(dungeonID, list);
    }

}
