package me.manaki.plugin.dungeons.dungeon.task;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.util.Tasks;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DMobSpawnTask extends BukkitRunnable {

    private final int SPAWN_DELAY_TICK = 30;

    private final String dungeonID;
    private final DStatus status;

    private final List<DMobTask> pendings;

    public DMobSpawnTask(String dungeonID, DStatus status) {
        this.dungeonID = dungeonID;
        this.status = status;
        this.pendings = new ArrayList<>();
        this.runTaskTimer(Dungeons.get(), 0, 5);
    }

    public void add(DMobTask task) {
        this.pendings.add(task);
    }

    @Override
    public void run() {
        if (pendings.size() == 0) return;

        // Spawn current maxmobs
        var d = DDataUtils.getDungeon(dungeonID);
        int maxmobs = d.getTurn(status.getTurn()).getMaxMobs();
        if (status.getTurnStatus().getCurrentMobs() >= maxmobs) return;

        // Spawn
        var mobTask = pendings.get(0);
        pendings.remove(0);
        status.getTurnStatus().addCurrentMobs(1);
        Tasks.async(() -> {
            playReadyEffect(mobTask.getLocation());
        }, 0, 4, this.SPAWN_DELAY_TICK * 50L);
        Bukkit.getScheduler().runTaskLater(Dungeons.get(), () -> {
            mobTask.start();
            status.addTask(mobTask);
        }, this.SPAWN_DELAY_TICK);
    }

    private void playReadyEffect(Location loc) {
        var lShow = Utils.getGroundBlock(loc.clone()).add(0, 1.2, 0);
        circleParticles(new Particle.DustOptions(Color.PURPLE, 1), lShow, 1);
    }

    private void circleParticles(Particle.DustOptions doo, Location location, double radius) {
        int amount = new Double(radius * 20).intValue();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();

        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = location.getX() + (radius * Math.cos(angle));
            double z = location.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(location.getWorld(), x, location.getY(), z));
        }

        for (Location l : locations) {
            location.getWorld().spawnParticle(Particle.REDSTONE, l, 1, 0, 0, 0, 0, doo);
        }
    }

}
