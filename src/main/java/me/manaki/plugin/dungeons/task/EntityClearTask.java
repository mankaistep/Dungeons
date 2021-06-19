package me.manaki.plugin.dungeons.task;

import me.manaki.plugin.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityClearTask extends BukkitRunnable {

    private Dungeons plugin;

    public EntityClearTask(Dungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (String wn : plugin.getWorldManager().getActiveWorlds()) {
            var w = Bukkit.getWorld(wn);
            if (w == null) continue;
            plugin.getDungeonManager().clearEntities(w);
        }
    }

    public static void start(Dungeons plugin) {
        var task = new EntityClearTask(plugin);
        task.runTaskTimer(plugin, 0, 10);
    }

}
