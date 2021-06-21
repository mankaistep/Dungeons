package me.manaki.plugin.dungeons.room;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.ticket.Tickets;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class RoomManager {

    private final Dungeons plugin;

    private int lastID;

    private Map<String, List<Room>> rooms;

    public RoomManager(Dungeons plugin) {
        this.plugin = plugin;
        this.rooms = Maps.newConcurrentMap();
        this.lastID = 0;
    }

    public Room getRoom(int roomID) {
        for (List<Room> rooms : rooms.values()) {
            for (Room room : rooms) {
                if (room.getID() == roomID) return room;
            }
        }
        return null;
    }

    public List<Room> getRooms(String dungeonID) {
        return rooms.getOrDefault(dungeonID, Lists.newArrayList());
    }

    public void removeRoom(int roomID) {
        for (List<Room> roomList : rooms.values()) {
            roomList.removeIf(room -> room.getID() == roomID);
        }
    }

    public Map<String, List<Room>> getRooms() {
        return rooms;
    }

    public RoomStatus getStatus(Player player, int roomID) {
        int inRoom = getCurrentRoom(player);
        boolean hasRoom = inRoom != -1;
        var room = getRoom(roomID);
        if (room.getPlayers().size() == 0 || room.isStarted()) return RoomStatus.EMPTY;
        var d = DDataUtils.getDungeon(room.getDungeonID());

        // Has room
        if (hasRoom) {
            // Test player amount
            if (inRoom == roomID) {
                if (d.getOption().getPlayer().test(room.getPlayers().size())) return RoomStatus.READY;
                else return RoomStatus.NOT_READY;
            }
            else return RoomStatus.ALREADY_IN_A_ROOM;
        }

        // Didnt has room
        else {
            // Full
            if (room.getPlayers().size() == d.getOption().getPlayer().getMax()) return RoomStatus.FULL;

            // Ticket
            if (d.getOption().isTicketRequired()) {
                if (!Tickets.has(player, room.getDungeonID())) return RoomStatus.NO_TICKET;
            }

            // Difficulty
            if (room.getDifficulty() != null) {
                if (!room.getDifficulty().getLevelRequired().test(player.getLevel())) return RoomStatus.NOT_ENOUGH_LEVEL;
            }

            return RoomStatus.CAN_JOIN;
        }
    }

    public RoomStatus getStatus(int roomID) {
        var room = getRoom(roomID);
        if (room.getPlayers().size() == 0 || room.isStarted()) return RoomStatus.EMPTY;
        var d = DDataUtils.getDungeon(room.getDungeonID());
        if (d.getOption().getPlayer().test(room.getPlayers().size())) return RoomStatus.READY;
        else return RoomStatus.NOT_READY;
    }

    public void createRoom(String dungeonID, Difficulty difficulty, Player creator) {
        lastID++;
        var r = new Room(lastID, dungeonID, difficulty, creator);
        var l = this.rooms.getOrDefault(dungeonID, Lists.newArrayList());
        synchronized (l) {
            l.add(r);
            this.rooms.put(dungeonID, l);
        }
    }

    public int getCurrentRoom(Player player) {
        for (List<Room> rooms : rooms.values()) {
            synchronized (rooms) {
                for (Room room : rooms) {
                    if (room.getPlayers().contains(player)) return room.getID();
                }
            }
        }
        return -1;
    }

}
