package me.manaki.plugin.dungeons.dungeon.turn.status;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class TStatus {

	private LivingEntity guarded;
	private Map<LivingEntity, String> mobToKills;
	private List<LivingEntity> slaveToSaves;
	private DStatistic statistic;

	private int currentMobs;

	public TStatus() {
		this.guarded = null;
		this.mobToKills = Maps.newHashMap();
		this.slaveToSaves = Lists.newArrayList();
		this.statistic = new DStatistic();
		this.currentMobs = 0;
	}
	
	public Map<LivingEntity, String> getMobToKills() {
		return this.mobToKills;
	}
	
	public List<LivingEntity> getSlaveToSaves() {
		return this.slaveToSaves;
	}
	
	public DStatistic getStatistic() {
		return this.statistic;
	}
	
	public void removeMobToKill(LivingEntity entity) {
		this.mobToKills.remove(entity);
	}
	
	public void removeSlaveToSave(LivingEntity entity) {
		this.slaveToSaves.remove(entity);
	}
	
	public void addMobToKill(String mID, LivingEntity entity) {
		this.mobToKills.put(entity, mID);
	}
	
	public void addSlaveToSave(LivingEntity entity) {
		this.slaveToSaves.add(entity);
	}
	
	public void setStatistic(DStatistic statistic) {
		this.statistic = statistic;
	}

	public void setGuarded(LivingEntity guarded) {
		this.guarded = guarded;
	}

	public LivingEntity getGuarded() {
		return guarded;
	}

	public void addCurrentMobs(int amount) {
		this.currentMobs += amount;
	}

	public void removeCurrentMobs(int amount) {
		this.currentMobs -= amount;
	}

	public int getCurrentMobs() {
		return currentMobs;
	}
}
