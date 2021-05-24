package me.manaki.plugin.dungeons.dungeon.status;

import be.maximvdw.featherboard.W;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.turn.status.TStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.sound.DSoundPlay;
import me.manaki.plugin.dungeons.sound.DSoundThread;
import me.manaki.plugin.dungeons.v4.dungeon.cache.DungeonCache;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DStatus {

	private long start;
	private List<UUID> starters;
	private List<UUID> players;
	private Map<UUID, DStatistic> statistics;	
	private DStatistic allStatistic;	
	private BossBar bossbar;	
	private int turn;
	private TStatus turnStatus;	
	private Set<BukkitRunnable> tasks;
	private boolean isPlaying;	
	private String checkPoint;
	private Map<UUID, List<String>> openedChests;

	// V4
	private DungeonCache cache;
	private Map<Player, DSoundThread> currentSounds;
	
	public DStatus(DungeonCache cache, long start, List<UUID> players, BossBar bossbar) {
		this.cache = cache;
		this.start = start;
		this.starters = players;
		this.players = players;
		this.bossbar = bossbar;
		this.turn = 1;
		this.statistics = Maps.newHashMap();
		players.forEach(uuid -> {
			this.statistics.put(uuid, new DStatistic());
		});
		this.allStatistic = new DStatistic();
		this.tasks = Sets.newHashSet();
		this.isPlaying = true;
		this.openedChests = Maps.newHashMap();
		this.checkPoint = DDataUtils.getDungeon(cache.getDungeonID()).getCheckPoints().get(0);
		this.currentSounds = Maps.newConcurrentMap();
	}

	public DungeonCache getCache() {
		return cache;
	}

	public Map<UUID, List<String>> getOpenedChests() {
		return this.openedChests;
	}
	
	public boolean isOpened(Player player, String lid) {
		return this.openedChests.getOrDefault(player.getUniqueId(), Lists.newArrayList()).contains(lid);
	}
	 
	public void setOpen(Player player, String lid) {
		List<String> list = this.openedChests.getOrDefault(player.getUniqueId(), Lists.newArrayList());
		list.add(lid);
		this.openedChests.put(player.getUniqueId(), list);
	}
	
	public List<UUID> getStarters() {
		return this.starters;
	}
	
	public List<UUID> getPlayers() {
		return this.players;
	}
	
	public Map<UUID, DStatistic> getStatistics() {
		return this.statistics;
	}
	
	public DStatistic getStatistic(Player player) {
		return getStatistic(player.getUniqueId());
	}
	
	public DStatistic getStatistic(UUID uuid) {
		return this.statistics.getOrDefault(uuid, null);
	}
	
	public DStatistic getAllStatistic() {
		return this.allStatistic;
	}
	
	public BossBar getBossBar() {
		return this.bossbar;
	}
	
	public int getTurn() {
		return this.turn;
	}
	
	public TStatus getTurnStatus() {
		return this.turnStatus;
	}
	
	public String getCheckpoint() {
		return this.checkPoint;
	}
	
	public boolean isPlaying()  {
		return this.isPlaying;
	}
	
	public Set<BukkitRunnable> getTasks() {
		return this.tasks;
	}
	
	public void removePlayer(Player player) {
		removePlayer(player.getUniqueId());
	}
	
	public void removePlayer(UUID uuid) {
		this.players.remove(uuid);
		Player player = Bukkit.getPlayer(uuid);
		if (player != null && this.bossbar != null) this.bossbar.removePlayer(player);
	}
	
	public void setBossBar(BossBar bossbar) {
		this.bossbar = bossbar;
	}
	
	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	public void setPlaying(boolean value) {
		this.isPlaying = value;
	}
	
	public void setAllStatistic(DStatistic as) {
		this.allStatistic = as;
	}
	
	public void addTask(BukkitRunnable br) {
		this.tasks.add(br);
	}
	
	public void setTurnStatus(TStatus status) {
		this.turnStatus = status;
	}
	
	public void setCheckpoint(String cp) {
		this.checkPoint = cp;
	}

	public long getStart() {
		return this.start;
	}

	public void cancelTask(BukkitRunnable br) {
		br.cancel();
		this.tasks.remove(br);
	}

	public void cancelAllTask(BukkitRunnable except) {
		for (BukkitRunnable task : this.tasks) {
			if (task != except) task.cancel();
		}
		this.tasks.clear();
		this.tasks.add(except);
	}

	public void stopSound(Player player) {
		if (!this.currentSounds.containsKey(player)) return;
		this.currentSounds.get(player).stopSound();
		this.currentSounds.remove(player);
	}

	public void stopAllSounds() {
		for (Map.Entry<Player, DSoundThread> e : this.currentSounds.entrySet()) {
			e.getValue().stopSound();
		}
	}

	public void playSound(Player player, DSoundPlay soundPlay, boolean override) {
		if (this.currentSounds.containsKey(player) && !override) return;

		if (currentSounds.containsKey(player)) {
			var sthread = currentSounds.get(player);
			sthread.stopSound();
		}
		var sound = Dungeons.get().getV4Config().getSound(soundPlay.getSounds().get(new Random().nextInt(soundPlay.getSounds().size())));
		var sthread = new DSoundThread(player, sound, soundPlay.getTimes());
		currentSounds.put(player, sthread);
		if (soundPlay.getDelay() != 0) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(Dungeons.get(), sthread::start, soundPlay.getDelay());
		}
		else sthread.start();
	}

}
