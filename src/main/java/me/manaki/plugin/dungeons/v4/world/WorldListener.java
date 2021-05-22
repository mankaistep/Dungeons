package me.manaki.plugin.dungeons.v4.world;

import me.manaki.plugin.dungeons.Dungeons;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    private final Dungeons plugin;

    public WorldListener(Dungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        var w = e.getWorld();
        for (String s : plugin.getV4Config().getWorldTemplates().keySet()) {
            if (w.getName().startsWith(s)) {
                plugin.getLogger().warning("Detect temporary world " + w.getName());
                w.setKeepSpawnInMemory(false);
                w.setAutoSave(false);
                w.setGameRule(GameRule.DISABLE_RAIDS, true);
                w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            }
        }
    }

}
