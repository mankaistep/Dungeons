package me.manaki.plugin.dungeons.room;

import com.google.common.collect.Sets;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import org.bukkit.entity.Player;

import java.util.Set;

public class Room {

    private int id;
    private String dungeonID;
    private Difficulty difficulty;

    private Set<Player> players;
    private int countdown;
    private int removeCountdown;

    private boolean iStarted;

    public Room(int id, String dungeonID, Difficulty difficulty, Player creator) {
        this.id = id;
        this.dungeonID = dungeonID;
        this.difficulty = difficulty;
        this.players = Sets.newHashSet(creator);
        this.countdown = Dungeons.get().getV4Config().getRoomCountdown();
        this.removeCountdown = Dungeons.get().getV4Config().getRemoveRoomCountdown();
        this.iStarted = false;
    }

    public int getID() {
        return id;
    }

    public String getDungeonID() {
        return dungeonID;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getRemoveCountdown() {
        return removeCountdown;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void setRemoveCountdown(int removeCountdown) {
        this.removeCountdown = removeCountdown;
    }

    public boolean isStarted() {
        return iStarted;
    }

    public void setStarted(boolean started) {
        this.iStarted = started;
    }
}
