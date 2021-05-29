package me.manaki.plugin.dungeons.task;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.room.Room;
import me.manaki.plugin.dungeons.room.RoomStatus;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomTask extends BukkitRunnable {

    public static void start(Dungeons plugin) {
        new RoomTask(plugin).runTaskTimer(plugin, 0, 20);
    }

    private final Dungeons plugin;

    private RoomTask(Dungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        var rm = plugin.getRoomManager();
        var dm = plugin.getDungeonManager();
        for (var dungeonID : rm.getRooms().keySet()) {
            var rooms = rm.getRooms(dungeonID);
            for (Room room : Lists.newArrayList(rooms)) {
                var status = rm.getStatus(room.getID());

                // Start countdown
                if (status == RoomStatus.READY) {
                    if (room.getCountdown() == 0) {
                        dm.start(room.getDungeonID(), room.getDifficulty(), room.getPlayers().stream().map(Player::getUniqueId).collect(Collectors.toList()));
                        room.setStarted(true);
                    } else room.setCountdown(room.getCountdown() - 1);
                } else room.setCountdown(plugin.getV4Config().getRoomCountdown());

                // Remove countdown
                if (status == RoomStatus.EMPTY) {
                    if (room.getRemoveCountdown() == 0) {
                        rm.removeRoom(room.getID());
                    } else room.setRemoveCountdown(room.getRemoveCountdown() - 1);
                }
            }
        }
    }

}
