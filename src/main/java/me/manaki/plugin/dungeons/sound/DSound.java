package me.manaki.plugin.dungeons.sound;

public class DSound extends Thread {

    private final String id;
    private final String source;
    private final long length;

    public DSound(String id, String source, long length) {
        this.id = id;
        this.source = source;
        this.length = length;
    }


    public String getID() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public long getLength() {
        return length;
    }
}
