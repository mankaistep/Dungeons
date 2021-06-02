package me.manaki.plugin.dungeons.sound;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class DSoundThread extends Thread {

    private final Player player;
    private final DSound sound;
    private final int loop;

    private boolean running;
    private int count;

    private World world;

    public DSoundThread(Player player, DSound sound) {
        this.player = player;
        this.sound = sound;
        this.loop = -1;
        this.running = false;
        this.count = 0;
        this.world = player.getWorld();
    }

    public DSoundThread(Player player, DSound sound, int loop) {
        this.player = player;
        this.sound = sound;
        this.loop = loop;
        this.running = false;
        this.count = 0;
        this.world = player.getWorld();
    }

    @Override
    public void run() {
        if (this.running) return;
        this.running = true;
        while (player.getWorld() == world && running && (loop == -1 || count < loop)) {
            count++;
            player.playSound(player.getLocation(), sound.getSource(), 100, 1);
            try {
                Thread.sleep(sound.getLength());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopSound() {
        this.running = false;
        player.stopSound(sound.getSource());
    }
}
