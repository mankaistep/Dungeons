package me.manaki.plugin.dungeons.v4.world;

import me.manaki.plugin.dungeons.Dungeons;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class WorldTask extends BukkitRunnable {

    public static void start(Dungeons plugin) {
        var wt = new WorldTask(plugin);
        wt.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    private final Dungeons plugin;

    private WorldTask(Dungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        var worlds = plugin.getWorldLoader().getPendingCaches();
        for (Map.Entry<WorldCache, Long> e : worlds.entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            if ((System.currentTimeMillis() - v) >= (plugin.getV4Config().getWorldLastTime() * 1000L)) {
                plugin.getWorldManager().removeActiveWorld(k);
                worlds.remove(k);
                plugin.getWorldLoader().unload(k.toWorldName(), true);
                plugin.getLogger().warning("Removed temporary world " + k.toWorldName());
            }
        }
    }

}
