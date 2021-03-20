package me.manaki.plugin.dungeons.dungeon.task;

import me.manaki.plugin.dungeons.dungeon.manager.DGameEnds;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TWinReq;
import me.manaki.plugin.dungeons.dungeon.turn.status.TStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class DGameTask extends BukkitRunnable {
	
	private String dungeon;
	private DStatus status;
	private long start;
	
	public DGameTask(String dungeon, DStatus status, long start) {
		this.dungeon = dungeon;
		this.status = status;
		this.start = start;
		this.runTaskTimer(Dungeons.get(), 0, 5);
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
		Dungeon d = DDataUtils.getDungeon(dungeon);
		int remain = d.getOption().getMaxTime() - new Long((System.currentTimeMillis() - start) / 1000).intValue();
		bb.setTitle("§c§l" + d.getInfo().getName() + " §f§l" + Utils.getFormat(remain));
		double progress = (double) remain / d.getOption().getMaxTime();
		if (progress < 0 || progress > 1) return;
		bb.setProgress(progress);
	}
	
	public void checkDungeon() {
		Dungeon d = DDataUtils.getDungeon(dungeon);
		if (status.getPlayers().size() == 0) {
			Lang.DUNGEON_LOSE_NOPLAYER.broadcast("%dungeon%", d.getInfo().getName());
			DGameEnds.loseDungeon(dungeon);
			this.cancel();
			return;
		}
		if (System.currentTimeMillis() - start >= d.getOption().getMaxTime() * 1000) {
			Lang.DUNGEON_LOSE_OVERTIME.broadcast("%dungeon%", d.getInfo().getName());
			DGameEnds.loseTurn(dungeon, status.getTurn());
			this.cancel();
			return;
		}
	}
	
	public void checkWinTurn() {
		if (!canWinTurn()) return;
		DGameEnds.winTurn(dungeon, status.getTurn(), this);
	}
	
	public boolean canWinTurn() {
		DTurn turn = DGameUtils.getTurn(dungeon, status.getTurn());
		TStatus ts = status.getTurnStatus();
		TWinReq wr = turn.getWinRequirement();

		// Slave slaved
		int slave = wr.getSlaveSave();
		if (ts.getStatistic().getSlaveSaved() < slave) return false;

		
		// Kill all
		boolean killAll = wr.isKillAll();
		if (killAll) {
			int sum = DGameUtils.countMobs(turn);
			if (ts.getStatistic().getMobKilled() < sum) return false;
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
