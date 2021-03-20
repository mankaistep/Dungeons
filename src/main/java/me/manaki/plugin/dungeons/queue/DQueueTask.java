package me.manaki.plugin.dungeons.queue;

import org.bukkit.scheduler.BukkitRunnable;

public class DQueueTask extends BukkitRunnable {
	
	@Override
	public void run() {	
		DQueues.checkAll();
	}

	
}