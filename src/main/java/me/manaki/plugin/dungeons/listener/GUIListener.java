package me.manaki.plugin.dungeons.listener;

import me.manaki.plugin.dungeons.queue.DQueueGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		DQueueGUI.onClick(e);
	}
	
}
