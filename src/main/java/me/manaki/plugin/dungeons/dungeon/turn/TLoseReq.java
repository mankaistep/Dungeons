package me.manaki.plugin.dungeons.dungeon.turn;

public class TLoseReq {

    private String guardedKilled;

    public TLoseReq(String guardedKilled) {
        this.guardedKilled = guardedKilled;
    }

    public String getGuardedKilled() {
        return guardedKilled;
    }
}
