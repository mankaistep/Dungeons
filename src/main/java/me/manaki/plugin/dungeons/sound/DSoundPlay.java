package me.manaki.plugin.dungeons.sound;

import java.util.List;

public class DSoundPlay {

    private final int delay;
    private final int times;
    private final List<String> sounds;

    public DSoundPlay(int delay, int times, List<String> sounds) {
        this.delay = delay;
        this.times = times;
        this.sounds = sounds;
    }

    public int getDelay() {
        return delay;
    }

    public int getTimes() {
        return times;
    }

    public List<String> getSounds() {
        return sounds;
    }
}
