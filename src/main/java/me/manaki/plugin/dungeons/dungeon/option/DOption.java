package me.manaki.plugin.dungeons.dungeon.option;

import me.manaki.plugin.dungeons.configable.Configable;
import me.manaki.plugin.dungeons.util.MinMax;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class DOption extends Configable {
	
	private boolean ticketRequired;
	private int maxTime;
	private MinMax player;
	private MinMax level;
	private int guiSlot;
	private boolean mobGlow;
	private Material guiIcon;
	private int cooldown;
	private boolean allowVelocity;
	private boolean difficulty;
	private double spawnRadius;
	private String permission;
	
	public DOption(FileConfiguration config, String path) {
		super(config, path);
	}
	
//	public DOption(int maxTime, int minPlayer, int maxPlayer, int minLevel, int maxLevel, int guiSlot, boolean lavaDead, boolean waterDead, int respawn, boolean mobGlow) {
//		this.maxTime = maxTime;
//		this.player = new MinMax(minPlayer, maxPlayer);
//		this.level = new MinMax(minLevel, maxLevel);
//		this.guiSlot = guiSlot;
//		this.mobGlow = mobGlow;
//	}
	
	public int getMaxTime() {
		return this.maxTime;
	}
	
	public MinMax getPlayer() {
		return this.player;
	}
	
	public MinMax getLevel() {
		return this.level;
	}
	
	public int getGUISlot() {
		return this.guiSlot;
	}

	public boolean isMobGlow() {
		return this.mobGlow;
	}
	
	public boolean isVelocityAllowed() {
		return this.allowVelocity;
	}
	
	public Material getGUIIcon() {
		return this.guiIcon;
	}
	
	public int getCooldown() {
		return this.cooldown;
	}
	
	public boolean isTicketRequired() {
		return this.ticketRequired;
	}

	public boolean hasDifficulty() {
		return difficulty;
	}

	public double getSpawnRadius() {
		return spawnRadius;
	}

	public String getPermission() {
		return permission;
	}

	@Override
	public void load(FileConfiguration config, String path) {
		this.maxTime = config.getInt(path + ".max-time");
		this.player = new MinMax(config.getString(path + ".player"));
		this.level = new MinMax(config.getString(path + ".level"));
		this.guiSlot = config.getInt(path + ".gui-slot");
		this.mobGlow = config.getBoolean(path + ".mob-glow");
		if (config.contains(path + ".gui-icon")) {
			this.guiIcon = Material.valueOf(config.getString(path + ".gui-icon"));
		} else this.guiIcon = Material.LEGACY_BANNER;
		if (config.contains(path + ".cooldown")) {
			this.cooldown = config.getInt(path + ".cooldown");
		} else this.cooldown = 0;
		if (config.contains(path + ".allow-velocity")) {
			this.allowVelocity = config.getBoolean(path + ".allow-velocity");
		} else this.allowVelocity = true;
		if (config.contains(path + ".ticket-required")) {
			this.ticketRequired = config.getBoolean(path + ".ticket-required");
		}
		this.difficulty = config.getBoolean(path + ".difficulty");
		this.spawnRadius = config.getDouble(path + ".spawn-radius", 35);
		this.permission = config.getString(path + ".permission", "");
	}

	@Override
	public void save(FileConfiguration config, String path) {
		config.set(path + ".ticket-required", this.ticketRequired);
		config.set(path + ".max-time", this.maxTime);
		config.set(path + ".player", this.player.toString());
		config.set(path + ".level", this.level.toString());
		config.set(path + ".mob-glow", this.mobGlow);
		config.set(path + ".gui-icon", this.guiIcon.name());
		config.set(path + ".cooldown", this.cooldown);
		config.set(path + ".allow-velocity", this.allowVelocity);
		config.set(path + ".gui-slot", this.guiSlot);
		config.set(path + ".spawn-radius", this.spawnRadius);
		config.set(path + ".difficulty", this.difficulty);
		config.set(path + ".permission", this.permission);
	}
	
}
