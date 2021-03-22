package me.manaki.plugin.dungeons.dungeon.manager;

import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class DGamePlays {
	
	public static void kick(Player player, boolean toSpawn) {
		String id = DPlayerUtils.getCurrentDungeon(player);
		if (id == null) return;
		DStatus sd = DGameUtils.getStatus(id);
		BossBar bb = sd.getBossBar();
		if (bb != null) bb.removePlayer(player);
		sd.removePlayer(player);
		if (toSpawn) player.teleport(Utils.getPlayerSpawn());
		Lang.DUNGEON_PLAYER_KICK.send(player);
	}
	
	public static void dead(Player player, boolean teleport) {
		String id = DPlayerUtils.getCurrentDungeon(player);
		Dungeon d = DDataUtils.getDungeon(id);
		if (id == null) return;
		DStatus sd = DGameUtils.getStatus(id);
		DStatistic pds = sd.getStatistic(player);
		
		pds.addDead(1);
		sd.getAllStatistic().addDead(1);
		sd.getTurnStatus().getStatistic().addDead(1);
		
		int maxDead = d.getRule().getRespawnTime() + DPlayer.from(player).getReviveBuff();
		if (pds.getDead() > maxDead) {
			BossBar bb = sd.getBossBar();
			if (bb != null) bb.removePlayer(player);
			sd.removePlayer(player);
			if (teleport) player.teleport(Utils.getPlayerSpawn());
			sd.getPlayers().forEach(uuid -> {
				Player p = Bukkit.getPlayer(uuid);
				Lang.DUNGEON_PLAYER_DEAD_KICK_OTHER.send(p, "%player%", "" + player.getName());
			});
			Lang.DUNGEON_PLAYER_DEAD_KICK.send(player, "%dead_remain%", "" + (maxDead - pds.getDead()));

			// Featherboard
			DGameEnds.featherBoardCheck(player);

			return;
		}
		
		// God in 3s
		DPlayerUtils.setGod(player, 20 * 3);
		if (teleport) DGameUtils.teleport(player, d.getLocation(sd.getCheckpoint()).getLocation()); 
		Lang.DUNGEON_PLAYER_DEAD_RESPAWN.send(player, "%dead_remain%", "" + (maxDead - pds.getDead()));
		Lang.DUNGEON_PLAYER_GOD.send(player, "%second%", "3");
	}



	
}
