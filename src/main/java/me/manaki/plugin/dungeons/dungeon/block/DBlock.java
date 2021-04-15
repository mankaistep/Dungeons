package me.manaki.plugin.dungeons.dungeon.block;

import me.manaki.plugin.dungeons.configable.Configable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class DBlock extends Configable {
	
	private Material m;
	private int d;

	private double x;
	private double y;
	private double z;

	private float pitch;
	private float yaw;
	
	public DBlock(FileConfiguration config, String path) {
		super(config, path);
	}
	
	public DBlock(Material material, int durability, Location location) {
		this.m = material;
		this.d = durability;
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}
	
	public Material getMaterial() {
		return this.m;
	}
	
	public int getBlockDamage() {
		return this.d;
	}

	public Location getLocation(World world) {
		return new Location(world, x, y, z, yaw, pitch);
	}

	@Override
	public void load(FileConfiguration config, String path) {
		String s = config.getString(path);
		
		Material m = Material.valueOf(s.split(";")[0]);
		int d = Integer.parseInt(s.split(";")[1]);
		double x = Double.parseDouble(s.split(";")[2]);
		double y = Double.parseDouble(s.split(";")[3]);
		double z = Double.parseDouble(s.split(";")[4]);
		float p = Float.parseFloat(s.split(";")[5]);
		float yw = Float.parseFloat(s.split(";")[6]);

		this.m = m;
		this.d = d;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = p;
		this.yaw = yw;
	}

	@Override
	public void save(FileConfiguration config, String path) {
		String s = m.name() + ";" + d + ";" + this.x + ";" + this.y+ ";" + this.z + ";" + this.pitch + ";" + this.yaw;
		config.set(path, s);
	}
	
}
