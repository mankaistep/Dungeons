package me.manaki.plugin.dungeons.dungeon.turn;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.util.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class TSpawn {
	
	private int delay;
	private TSGuarded guard;
	private List<String> blocks;
	private List<TSMob> mobs;
	private List<TSSlave> slaves;
	
	public TSpawn(int delay, TSGuarded guard, List<String> blocks, List<TSMob> mobs, List<TSSlave> slaves) {
		this.delay = delay;
		this.guard = guard;
		this.blocks = blocks;
		this.mobs = mobs;
		this.slaves = slaves;
	}

	public TSGuarded getGuarded() {
		return guard;
	}

	public List<String> getBlockBreaks() {
		return this.blocks;
	}
	
	public List<TSMob> getMobs() {
		return this.mobs;
	}
	
	public List<TSSlave> getSlaves() {
		return this.slaves;
	}
	
	public int getDelay() {
		return this.delay;
	}
	
	public String blocksToString() {
		return Utils.to(this.blocks, ";");
	}
	
	public List<String> slavesToStringList() {
		return this.slaves.stream().map(slv -> slv.getSlave() + ";" + slv.getLocation()).collect(Collectors.toList());
	}
	
	public List<String> mobsToStringList() {
		List<String> list = Lists.newArrayList();
		this.mobs.forEach(m -> {
			list.add(m.toString());
		});
		return list;
	}
	
}
