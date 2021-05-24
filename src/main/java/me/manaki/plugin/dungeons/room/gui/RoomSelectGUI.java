package me.manaki.plugin.dungeons.room.gui;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.room.Room;
import me.manaki.plugin.dungeons.room.RoomStatus;
import me.manaki.plugin.dungeons.ticket.Tickets;
import me.manaki.plugin.dungeons.util.ItemStackManager;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoomSelectGUI {

    private static final int CREATE_BUTTON_SLOT = 11;
    private static final int EXIT_BUTTON_SLOT = 15;
    private static final List<Integer> ROOM_SLOTS = Lists.newArrayList(29, 30, 31, 32, 33, 38, 39, 40, 41, 42);

    public static void open(Player player, String dungeonID) {
        var d = DDataUtils.getDungeon(dungeonID);
        var inv = Bukkit.createInventory(new RSGUIHolder(dungeonID), 54, "§0§lPHÒNG PHÓ BẢN " + d.getInfo().getName().toUpperCase());
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);

        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.get(), () -> {
            var rm = Dungeons.get().getRoomManager();
            for (int i = 0 ; i < inv.getSize() ; i++) if (!ROOM_SLOTS.contains(i)) inv.setItem(i, Utils.getBackIcon());
            inv.setItem(CREATE_BUTTON_SLOT, getCreateButton());
            inv.setItem(EXIT_BUTTON_SLOT, getExitButton());

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getOpenInventory().getTopInventory() != inv) {
                        this.cancel();
                        return;
                    }
                    for (Integer slot : ROOM_SLOTS) inv.setItem(slot, null);
                    var rooms = rm.getRooms(dungeonID);
                    synchronized (rooms) {
                        for (int i = 0; i < rooms.size(); i++) {
                            var room = rooms.get(i);
                            inv.setItem(ROOM_SLOTS.get(i), rm.getStatus(player, room.getID()).getIcon(player, room));
                        }
                    }
                }
            }.runTaskTimerAsynchronously(Dungeons.get(), 0, 20);
        });

    }

    public static void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof RSGUIHolder)) return;
        e.setCancelled(true);

        var rm = Dungeons.get().getRoomManager();
        var player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
        var holder = (RSGUIHolder) e.getInventory().getHolder();
        var dungeonID = holder.getDungeonID();
        var d = DDataUtils.getDungeon(dungeonID);
        int slot = e.getSlot();

        if (slot == EXIT_BUTTON_SLOT) {
            DungeonSelectGUI.open(player);
        }

        else if (slot == CREATE_BUTTON_SLOT) {
            if (rm.getCurrentRoom(player) != -1) {
                player.sendMessage("§cThoát phòng hiện tại để tạo phòng mới!");
                return;
            }
            if (d.getOption().hasDifficulty()) DifficultySelectGUI.open(player, dungeonID);
            else {
                checkAndCreate(dungeonID, null, player);
            }
        }

        else if (ROOM_SLOTS.contains(slot)) {
            var room = getRoomFromSlot(dungeonID, slot);
            if (room == null) return;
            var status = rm.getStatus(player, room.getID());

            // Join
            if (status == RoomStatus.CAN_JOIN) {
                if (d.getOption().isTicketRequired()) Tickets.takeOne(player, dungeonID);
                room.addPlayer(player);
            }

            // Out
            if (status == RoomStatus.READY || status == RoomStatus.NOT_READY) {
                room.removePlayer(player);
            }
        }
    }

    private static Room getRoomFromSlot(String dungeonID, int slot) {
        for (int i = 0; i < ROOM_SLOTS.size(); i++) {
            if (ROOM_SLOTS.get(i) == slot){
                var l = Dungeons.get().getRoomManager().getRooms(dungeonID);
                if (i >= l.size()) return null;
                return l.get(i);
            }
        }
        return null;
    }

    private static ItemStack getCreateButton() {
        var is = new ItemStack(Material.REDSTONE_BLOCK);
        var ism = new ItemStackManager(is);
        ism.setName("§c§lTạo phòng mới");
        List<String> lore = Lists.newArrayList();
        lore.add("§cLưu ý: §fCó thể mất vé khi tạo nếu phó bản yêu cầu");
        ism.setLore(lore);
        return is;
    }

    private static ItemStack getExitButton() {
        var is = new ItemStack(Material.BARRIER);
        var ism = new ItemStackManager(is);
        ism.setName("§c§lThoát");
        List<String> lore = Lists.newArrayList();
        lore.add("§fThoát ra menu chọn phó bản");
        ism.setLore(lore);
        return is;
    }

    public static boolean checkAndCreate(String dungeonID, Difficulty difficulty, Player player) {
        var rm = Dungeons.get().getRoomManager();
        var d = DDataUtils.getDungeon(dungeonID);

        if (difficulty != null) {
            if (!difficulty.getLevelRequired().test(player.getLevel())) {
                player.sendMessage("§cCấp độ không đạt yêu cầu!");
                return false;
            }
        }

        if (rm.getRooms().size() >= ROOM_SLOTS.size()) {
            player.sendMessage("§cNhiều phòng quá rồi, đợi thêm chỗ trống rồi tạo!");
            return false;
        }

        // Ticket
        if (d.getOption().isTicketRequired()) {
            if (!Tickets.takeOne(player, dungeonID)) {
                player.sendMessage("§cBạn cần có x1 Vé để tạo phòng " + d.getInfo().getName());
                return false;
            } else player.sendMessage("§aĐã lấy 1 vé phó bản từ kho đồ của bạn");
        }

        // Create
        rm.createRoom(dungeonID, difficulty, player);

        return true;
    }

}

class RSGUIHolder implements InventoryHolder {

    private String dungeonID;

    public RSGUIHolder(String dungeonID) {
        this.dungeonID = dungeonID;
    }

    public String getDungeonID() {
        return dungeonID;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
