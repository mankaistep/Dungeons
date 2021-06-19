package me.manaki.plugin.dungeons.dungeon.location;

import me.manaki.plugin.dungeons.configable.Configable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class DLocation extends Configable {
	
	private double radius;

	private double x;
	private double y;
	private double z;

	private float pitch;
	private float yaw;

//	private Location location;
	
	public DLocation(FileConfiguration config, String path) {
		super(config, path);
	}
	
	public DLocation(int radius, Location location) {
		this.radius = radius;
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}
	
	public double getRadius() {
		return this.radius;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public Location getLocation(World world) {
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	@Override
	public void load(FileConfiguration config, String path) {	
		String s = config.getString(path);
		assert s != null;
		double r = Double.parseDouble(s.split(";")[0]);
		double x = Double.parseDouble(s.split(";")[1]);
		double y = Double.parseDouble(s.split(";")[2]);
		double z = Double.parseDouble(s.split(";")[3]);
		float p = Float.parseFloat(s.split(";")[4]);
		float yw = Float.parseFloat(s.split(";")[5]);
		
		this.radius = r;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = p;
		this.yaw = yw;
	}

	@Override
	public void save(FileConfiguration config, String path) {
		String s = radius + ";" + this.x + ";" + this.y+ ";" + this.z + ";" + this.pitch + ";" + this.yaw;
		config.set(path, s);
	}
	
}
