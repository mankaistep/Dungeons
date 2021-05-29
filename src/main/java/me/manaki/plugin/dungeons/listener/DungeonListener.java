package me.manaki.plugin.dungeons.listener;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TChest;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.rank.RankUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DungeonListener implements Listener {

	private final Dungeons plugin;

	public DungeonListener(Dungeons plugin) {
		this.plugin = plugin;
	}

	/*
	 * Player open chest
	 */
	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (!DPlayerUtils.isInDungeon(player)) return;
		
		String dungeonCache = DPlayerUtils.getDungeonCachePlayerStandingOn(player);
		if (dungeonCache == null) return;

		var status = plugin.getDungeonManager().getStatus(dungeonCache);
		var dungeonID = status.getCache().getDungeonID();

		if (!DGameUtils.isPlaying(dungeonID)) return;
		Dungeon d = DDataUtils.getDungeon(dungeonID);

		for (DTurn turn : d.getTurns()) {
			Block b = e.getClickedBlock();
			if (b != null && b.getType() == Material.CHEST) {
				Location l = b.getLocation();
				String lid = DGameUtils.checkLocation(dungeonID, l);
				if (lid == null) return;
				if (turn.getChest(lid) == null) return;

				var pstatistic = status.getStatistic(player);
				if (pstatistic == null) {
					throw new NullPointerException("PLAYER STATISTIC NULL");
				}
				Rank rank = RankUtils.getRank(dungeonID, pstatistic);

				TChest chest = turn.getChest(lid);
				if (!RankUtils.equalOrBetter(rank, chest.getRank())) return;
				
				e.setCancelled(true);
				if (status.isOpened(player, lid)) {
					Lang.DUNGEON_CHEST_OPENED.send(player);
					return;
				}
				
				turn.getChest(lid).getCommands().forEach(cmd -> {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
				});
				DGameUtils.sendChestOpen(player, l);
				status.setOpen(player, lid);
			}
		}
	}
	
	/*
	 * Check killed
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();

		String dungeonCache = DPlayerUtils.getDungeonCachePlayerStandingOn(player);
		if (dungeonCache == null) return;
		var status = plugin.getDungeonManager().getStatus(dungeonCache);
		var dungeonID = status.getCache().getDungeonID();
		var world = status.getCache().getWorldCache().toWorld();

		DStatistic s = status.getStatistic(player);
		Dungeon d = DDataUtils.getDungeon(dungeonID);
		
		int maxdead = DPlayer.from(player).getMaxDead(dungeonID);
		if (s.getDead() < maxdead) { ;
			plugin.getDungeonManager().dead(player, false);
			e.setRespawnLocation(d.getLocation(status.getCheckpoint()).getLocation(world));
		}
	}
	
	/*
	 * PvP
	 */
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			Player da = (Player) e.getDamager();
			Player en = (Player) e.getEntity();

			String dc1 = DPlayerUtils.getCurrentDungeonCache(da);
			String dc2 = DPlayerUtils.getCurrentDungeonCache(en);

			if (dc1 == null || dc2 == null) return;
			if (dc1.equals(dc2)) e.setCancelled(true);
		}
	}
	
	/*
	 * Cancel break/build block
	 */
	
//	@EventHandler
//	public void onBreak(BlockBreakEvent e) {
//		DDataUtils.getDungeons().values().forEach(dungeon -> {
//			dungeon.getInfo().getWorlds().forEach(w -> {
//				World world = Bukkit.getWorld(w);
//				if (world == null) return;
//				if (e.getBlock().getWorld() != world) return;
//				Player player = e.getPlayer();
//				if (!player.hasPermission("dungeon3.build")) {
//					player.sendMessage("§cBạn không thể phá block");
//					e.setCancelled(true);
//				}
//			});
//		});
//	}
	
//	@EventHandler
//	public void onBreak(BlockPlaceEvent e) {
//		DDataUtils.getDungeons().values().forEach(dungeon -> {
//			dungeon.getInfo().getWorlds().forEach(w -> {
//				World world = Bukkit.getWorld(w);
//				if (world == null) return;
//				if (e.getBlock().getWorld() != world) return;
//				Player player = e.getPlayer();
//				if (!player.hasPermission("dungeon3.build")) {
//					player.sendMessage("§cBạn không thể đặt block");
//					e.setCancelled(true);
//				}
//			});
//		});
//	}
	
	// Remove blocks of explosion
//	@EventHandler
//	public void onExplode(EntityExplodeEvent e) {
//		DDataUtils.getDungeons().values().forEach(dungeon -> {
//			dungeon.getInfo().getWorlds().forEach(w -> {
//				World world = Bukkit.getWorld(w);
//				if (world == null) return;
//				if (e.getEntity().getWorld() != world) return;
//				e.blockList().clear();
//			});
//		});
//	}
	
	/*
	 *  Load on first join
	 */
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Dungeons main = Dungeons.get();
		if (!main.loaded) {
			main.loaded = true;
			main.reloadConfig();
			System.out.println("[Dungeon3] Loaded on first join");
		}
	}
	
	
	
	
	
	
	
	
	
	
}
