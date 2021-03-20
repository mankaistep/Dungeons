package me.manaki.plugin.dungeons.dungeon.moneycoin;

import me.manaki.plugin.dungeons.main.Dungeons;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class DMoneyCoin {
	
	private static final String MDATA_ID = "dungeon3.moneycoin";
	
	private double value;
	
	public DMoneyCoin(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public String toString() {
		return this.value + "";
	}
	
	public static DMoneyCoin parse(String s) {
		double v = Double.valueOf(s);
		return new DMoneyCoin(v);
	}
	
	public static DMoneyCoin from(Item i) {
		if (!i.hasMetadata(MDATA_ID)) return null;
		return DMoneyCoin.parse(i.getMetadata(MDATA_ID).get(0).asString());
	}
	
	public static void drop(Location l, DMoneyCoin coin) {
		ItemStack is = new ItemStack(Material.SUNFLOWER, 1);
		Item i = l.getWorld().dropItem(l, is);
		i.setCustomName("§f§l" + coin.getValue() + "$");
		i.setCustomNameVisible(true);
		i.setPickupDelay(Integer.MAX_VALUE);
		i.setMetadata(MDATA_ID, new FixedMetadataValue(Dungeons.get(), coin.toString()));
	}
	
}
 