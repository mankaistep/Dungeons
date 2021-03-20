package me.manaki.plugin.dungeons.guarded;

public class Guarded {

    private String name;
    private int color;
    private double health;

    public Guarded(String name, int color, double health) {
        this.name = name;
        this.color = color;
        this.health = health;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public double getHealth() {
        return health;
    }
}
