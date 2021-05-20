package me.manaki.plugin.dungeons.buff;

import org.bukkit.configuration.file.FileConfiguration;

public class Buff {
	
	public static double DROP = 0;
	
	public static void init(FileConfiguration config) {
		DROP = config.getInt("buff.drop");
	}
	
}
