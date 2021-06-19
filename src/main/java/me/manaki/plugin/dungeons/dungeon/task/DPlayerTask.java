package me.manaki.plugin.dungeons.dungeon.task;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DPlayerTask extends BukkitRunnable {

	private final Dungeons plugin;

	private final Player player;
	private final String dungeon;
	private final DStatus status;
	
	public DPlayerTask(Player player, String dungeon, DStatus status) {
		this.player = player;
		this.dungeon = dungeon;
		this.status = status;
		this.runTaskTimer(Dungeons.get(), 0, 5);
		this.plugin = Dungeons.get();
	}
	
	@Override
	public void run() {
		if (status.isEnded()) {
			this.cancel();
			return;
		}

		if (!checkResult()) return;
		if (!checkOnline()) return;
		if (!checkPlace()) return;
		if (!checkDead()) return;
		
		checkCheckpoint();
	}
	
	public boolean checkResult() {
		if (!status.isPlaying()) {
			this.cancel();
			return false;
		}
		return true;
	}
	
	public boolean checkOnline() {
		if (!player.isOnline()) {
			plugin.getDungeonManager().kick(player, false);
			this.cancel();
			return false;
		}
		return true;
	}
	
	public void checkCheckpoint() {
		Location l = player.getLocation();
		String to = DGameUtils.getStandingCheckpoint(dungeon, l);
		String from = status.getCheckpoint();
		if (DGameUtils.canChangeCheckpoint(dungeon, from, to)) {
			status.setCheckpoint(to);
			Lang.DUNGEON_NEW_CHECKPOINT.broadcast(status.getPlayers());;
		}
	}
	
	public boolean checkPlace() {
		Dungeon d = DDataUtils.getDungeon(dungeon);
		var cacheID = DPlayerUtils.getDungeonCachePlayerStandingOn(player);
		if (cacheID == null) {
			plugin.getDungeonManager().kick(player, false);
			return false;
		}
		var status = plugin.getDungeonManager().getStatus(cacheID);
		if (!status.getCache().getWorldCache().toWorld().getName().equalsIgnoreCase(player.getWorld().getName())) {
			plugin.getDungeonManager().kick(player, false);
			this.cancel();
			return false;
		}
		return true;
	}
	
	public boolean checkDead() {
		if (player.isDead()) {
			player.spigot().respawn();
			return false;
		}
		Dungeon d = DDataUtils.getDungeon(dungeon);
		Material m = player.getLocation().getBlock().getType();
		if (d.getRule().isLavaDead()) {
			if (m == Material.LAVA) {
				plugin.getDungeonManager().dead(player, true);
				return false;
			}
		}
		if (d.getRule().isWaterDead()) {
			if (m == Material.WATER) {
				plugin.getDungeonManager().dead(player, true);
				return false;
			}
		}
		if (player.getLocation().getY() <= d.getRule().getYDead()) {
			plugin.getDungeonManager().dead(player, true);
			return false;
		}
		
		
		return true;
	}

	
}
