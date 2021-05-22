package me.manaki.plugin.dungeons.listener;

import me.manaki.plugin.dungeons.room.gui.DifficultySelectGUI;
import me.manaki.plugin.dungeons.room.gui.DungeonSelectGUI;
import me.manaki.plugin.dungeons.room.gui.RoomSelectGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {
	
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        DungeonSelectGUI.onClick(e);
        DifficultySelectGUI.onClick(e);
        RoomSelectGUI.onClick(e);
    }
	
}
