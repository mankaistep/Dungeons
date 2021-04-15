package me.manaki.plugin.dungeons.dungeon.info;

import me.manaki.plugin.dungeons.configable.Configable;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class DInfo extends Configable {
	
	private String name;
	private String desc;
	private String world;
	
	public DInfo(FileConfiguration config, String path) {
		super(config, path);
	}
	
	public DInfo(String name, String desc, String world) {
		this.name = name;
		this.desc = desc;
		this.world = world;
	}

	public String getName() {
		return this.name;
	}
	
	public String getDesc() {
		return this.desc;
	}

	public String getWorld() {
		return world;
	}

	@Override
	public void load(FileConfiguration config, String path) {
		this.name = config.getString(path + ".name");
		this.desc = config.getString(path + ".desc");
		this.world = config.getString(path + ".world");
	}

	@Override
	public void save(FileConfiguration config, String path) {
		config.set(path + ".name", name);
		config.set(path + ".desc", desc);
		config.set(path + ".world", world);
	}
	
}
