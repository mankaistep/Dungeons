package me.manaki.plugin.dungeons.util;


import me.manaki.plugin.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Tasks {

    public static void sync(Runnable r) {
        Bukkit.getScheduler().runTask(Dungeons.get(), r);
    }

    public static void sync(Runnable r, int later) {
        Bukkit.getScheduler().runTaskLater(Dungeons.get(), r, later);
    }

    public static int sync(Runnable r, int later, int interval) {
        return Bukkit.getScheduler().runTaskTimer(Dungeons.get(), r, later, interval).getTaskId();
    }

    public static void sync(Runnable r, int later, int interval, int times) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                i++;
                if (i > times) {
                    this.cancel();
                    return;
                }
                r.run();
            }
        }.runTaskTimer(Dungeons.get(), later, interval);
    }

    public static void sync(Runnable r, int later, int interval, long period) {
        long start = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - start >= period) {
                    this.cancel();
                    return;
                }
                r.run();
            }
        }.runTaskTimer(Dungeons.get(), later, interval);
    }

    public static void async(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.get(), r);
    }

    public static void async(Runnable r, int later) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Dungeons.get(), r, later);
    }

    public static void async(Runnable r, int later, int interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Dungeons.get(), r, later, interval);
    }

    public static void async(Runnable r, int later, int interval, int times) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                i++;
                if (i > times) {
                    this.cancel();
                    return;
                }
                r.run();
            }
        }.runTaskTimerAsynchronously(Dungeons.get(), later, interval);
    }

    public static void async(Runnable r, int later, int interval, long period) {
        long start = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - start >= period) {
                    this.cancel();
                    return;
                }
                r.run();
            }
        }.runTaskTimerAsynchronously(Dungeons.get(), later, interval);
    }

}
