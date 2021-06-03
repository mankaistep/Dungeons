package me.manaki.plugin.dungeons.room.gui;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DifficultySelectGUI {

    private static final Map<Difficulty, Integer> slots = Map.of(Difficulty.EASY, 1, Difficulty.NORMAL, 3, Difficulty.HARD, 5, Difficulty.INSANE, 7);

    public static void open(Player player, String dungeonID) {
        var inv = Bukkit.createInventory(new DSGUIHolder(dungeonID), 9, "§0§lCHỌN ĐỘ KHÓ");
        player.openInventory(inv);

        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.get(), () -> {
            for (int i = 0 ; i < inv.getSize() ; i++) inv.setItem(i, Utils.getBackIcon());
            slots.forEach((d, slot) -> {
                inv.setItem(slot, d.getIcon());
            });
        });
    }

    public static void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof DSGUIHolder)) return;
        e.setCancelled(true);

        var player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
        var holder = (DSGUIHolder) e.getInventory().getHolder();
        int slot = e.getSlot();
        if (slots.containsValue(slot)) {
            for (Difficulty dct : slots.keySet()) {
                if (slots.get(dct) == slot) {
                    if (RoomSelectGUI.checkAndCreate(holder.getDungeonID(), dct, player)) RoomSelectGUI.open(player, holder.getDungeonID());
                    return;
                }
            }
        }
    }

}

class DSGUIHolder implements InventoryHolder {

    private String dungeonID;

    public DSGUIHolder(String dungeonID) {
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
