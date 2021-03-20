package me.manaki.plugin.dungeons.dungeon.moneycoin;

import me.manaki.plugin.dungeons.configable.ConfigableListContent;

public class DMoneyDrop extends ConfigableListContent {
	
	public DMoneyDrop(String s) {
		load(s);
	}
	
	private String mobID;
	private double value;
	private double chance;
	
	public String getMobID() {
		return this.mobID;
	}
	
	public double getValue() {
		return this.value;
	}
	
	public double getChance() {
		return this.chance;
	}

	@Override
	public void load(String s) {
		this.mobID = s.split(";")[0];
		this.value = Double.valueOf(s.split(";")[1]);
		this.chance = Double.valueOf(s.split(";")[2]);
	}

	@Override
	public String toString() {
		return this.mobID + ";" + this.value + ";" + this.chance;
	}
	
}
