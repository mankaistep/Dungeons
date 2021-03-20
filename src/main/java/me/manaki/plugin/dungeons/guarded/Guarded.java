package me.manaki.plugin.dungeons.guarded;

public class Guarded {

    private String name;
    private int color;
    private double health;
    private double targetRadius;

    public Guarded(String name, int color, double health, double targetRadius) {
        this.name = name;
        this.color = color;
        this.health = health;
        this.targetRadius = targetRadius;
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

    public double getTargetRadius() {
        return targetRadius;
    }
}
