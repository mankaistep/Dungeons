package me.manaki.plugin.dungeons.dungeon.task;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TWinReq;
import me.manaki.plugin.dungeons.dungeon.turn.status.TStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class DGameTask extends BukkitRunnable {

	private final Dungeons plugin;

	private final String dungeonCache;
	private final DStatus status;
	private final long start;
	
	public DGameTask(String dungeonCache, DStatus status, long start) {
		this.dungeonCache = dungeonCache;
		this.status = status;
		this.start = start;
		this.runTaskTimer(Dungeons.get(), 0, 5);
		this.plugin = Dungeons.get();
	}
	
	@Override
	public void run() {
		setBossBar();
		checkDungeon();
		checkWinTurn();
		saveTimeSurvived();
	}
	
	public void saveTimeSurvived() {
		status.getAllStatistic().setTimeSurvived(new Long((System.currentTimeMillis() - this.start) / 1000).intValue());
		status.getPlayers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null || !player.isOnline()) {
				return;
			}
			DStatistic s = status.getStatistic(player);
			s.setTimeSurvived(new Long((System.currentTimeMillis() - this.start) / 1000).intValue());
		});
	}
	
	public void setBossBar() {
		BossBar bb = status.getBossBar();
		if (bb == null) return;
		var dungeonID = status.getCache().getDungeonID();
		Dungeon d = DDataUtils.getDungeon(dungeonID);
		int remain = d.getOption().getMaxTime() - new Long((System.currentTimeMillis() - start) / 1000).intValue();
		bb.setTitle("§c§l" + d.getInfo().getName() + " §f§l" + Utils.getFormat(remain));
		double progress = (double) remain / d.getOption().getMaxTime();
		if (progress < 0 || progress > 1) return;
		bb.setProgress(progress);
	}
	
	public void checkDungeon() {
		var dungeonID = status.getCache().getDungeonID();
		Dungeon d = DDataUtils.getDungeon(dungeonID);
		if (status.getPlayers().size() == 0) {
			Lang.DUNGEON_LOSE_NOPLAYER.broadcast("%dungeon%", d.getInfo().getName());
			plugin.getDungeonManager().lose(dungeonCache);
			this.cancel();
			return;
		}
		if (System.currentTimeMillis() - start >= d.getOption().getMaxTime() * 1000L) {
			Lang.DUNGEON_LOSE_OVERTIME.broadcast("%dungeon%", d.getInfo().getName());
			plugin.getDungeonManager().lose(dungeonCache);
			this.cancel();
		}
	}
	
	public void checkWinTurn() {
		if (!canWinTurn()) return;
		plugin.getDungeonManager().win(dungeonCache, status.getTurn(), this);
	}
	
	public boolean canWinTurn() {
		var dungeonID = status.getCache().getDungeonID();
		DTurn turn = DGameUtils.getTurn(dungeonID, status.getTurn());
		TStatus ts = status.getTurnStatus();
		TWinReq wr = turn.getWinRequirement();

		// Slave slaved
		int slave = wr.getSlaveSave();
		if (ts.getStatistic().getSlaveSaved() < slave) return false;

		
		// Kill all
		boolean killAll = wr.isKillAll();
		if (killAll) {
			int sum = DGameUtils.countMobs(turn);
			return ts.getStatistic().getMobKilled() >= sum;
		}
		else {
			// Kill
			List<String> killed = wr.getKills();
			for (String mid : killed) {
				if (!ts.getStatistic().getListKilled().contains(mid)) return false;
			}
		}

		return true;
	}
	
	
	
}
