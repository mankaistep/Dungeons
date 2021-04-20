package me.manaki.plugin.dungeons.dungeon.turn;

public class TSGuarded {

    private String slv;
    private String loc;

    public TSGuarded(String slv, String loc) {
        this.slv = slv;
        this.loc = loc;
    }

    public TSGuarded(String s) {
        if (s == null) return;
        if (s.equalsIgnoreCase("")) return;
        this.slv = s.split(";")[0];
        this.loc = s.split(";")[1];
    }

    public String getGuarded() {
        if (this.slv == null || this.slv.equalsIgnoreCase("null")) return null;
        return this.slv;
    }

    public boolean isGuardedNull() {
        return this.slv == null || this.slv.equals("");
    }

    public String getLocation() {
        return this.loc;
    }

    public String toString() {
        if (this.slv == null) return "";
        return this.slv + ";" + this.loc;
    }

}
