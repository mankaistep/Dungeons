package me.manaki.plugin.dungeons.v4.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigGetter {
	
	private final FileConfiguration config;
	
	ConfigGetter(FileConfiguration config) {
		this.config = config;
	}

	public int getInt(String path) {
		return getInt(path, 0);
	}

	public int getInt(String path, int defaultValue) {
		if (config.contains(path)) return config.getInt(path);
		return defaultValue;
	}

	public double getDouble(String path) {
		if (config.contains(path)) return config.getDouble(path);
		return 0d;
	}

	public double getDouble(String path, double defaultValue) {
		if (config.contains(path)) return config.getDouble(path);
		return defaultValue;
	}

	public long getLong(String path) {
		if (config.contains(path)) return config.getLong(path);
		return 0L;
	}

	public long getLong(String path, long defaultValue) {
		if (config.contains(path)) return config.getLong(path);
		return defaultValue;
	}

	public boolean getBoolean(String path) {
		if (config.contains(path)) return config.getBoolean(path);
		return false;
	}
	
	public boolean getBoolean(String path, boolean defaultValue) {
		if (config.contains(path)) return config.getBoolean(path);
		return defaultValue;
	}

	public String getString(String path) {
		if (config.contains(path)) return config.getString(path).replace("&", "§");
		return null;
	}

	public String getString(String path, String defaultValue) {
		if (config.contains(path)) return config.getString(path).replace("&", "§");
		return defaultValue;
	}

	public List<String> getStringList(String path) {
		if (config.contains(path)) return config.getStringList(path).stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
		return null;
	}

	public List<String> getStringList(String path, List<String> defaultValue) {
		if (config.contains(path)) return config.getStringList(path).stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
		return defaultValue;
	}

	public static ConfigGetter from(FileConfiguration config) {
		return new ConfigGetter(config);
	}
	
}
