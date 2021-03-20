package me.manaki.plugin.dungeons.listener;

import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TChest;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.manager.DGamePlays;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.rank.RankUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DungeonListener implements Listener {
	
	/*
	 * Player open chest
	 */
	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (!DPlayerUtils.isInDungeonWorld(player)) return;
		
		String id = DPlayerUtils.getDungeonPlayerStandingOn(player);
		if (id == null) return;
		if (!DGameUtils.isPlaying(id)) return;

		Dungeon d = DDataUtils.getDungeon(id);
		DStatus status = DGameUtils.getStatus(id);
		for (DTurn turn : d.getTurns()) {
			Block b = e.getClickedBlock();
			Rank rank = RankUtils.getRank(id, status.getStatistic(player));
			if (b != null && b.getType() == Material.CHEST) {
				Location l = b.getLocation();
				String lid = DGameUtils.checkLocation(id, l);
				if (lid == null) return;
				if (turn.getChest(lid) == null) return;
				
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
		String id = DPlayerUtils.getCurrentDungeon(player);
		if (id == null) return;
		DStatus status = DGameUtils.getStatus(id);
		DStatistic s = status.getStatistic(player);
		Dungeon d = DDataUtils.getDungeon(id);
		
		int maxdead = DPlayer.from(player).getMaxDead(id);
		if (s.getDead() < maxdead) {
			DGamePlays.dead(player, false);
			e.setRespawnLocation(d.getLocation(status.getCheckpoint()).getLocation());
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
			if (DPlayerUtils.getCurrentDungeon(da) == null || DPlayerUtils.getCurrentDungeon(en) == null) return;
			if (DPlayerUtils.getCurrentDungeon(da).equals(DPlayerUtils.getCurrentDungeon(en))) e.setCancelled(true);
		}
	}
	
	/*
	 * Cancel break/build block
	 */
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		DDataUtils.getDungeons().values().forEach(dungeon -> {
			dungeon.getInfo().getWorlds().forEach(w -> {
				World world = Bukkit.getWorld(w);
				if (world == null) return;
				if (e.getBlock().getWorld() != world) return;
				Player player = e.getPlayer();
				if (!player.hasPermission("dungeon3.build")) {
					player.sendMessage("§cBạn không thể phá block");
					e.setCancelled(true);
				}
			});
		});
	}
	
	@EventHandler
	public void onBreak(BlockPlaceEvent e) {
		DDataUtils.getDungeons().values().forEach(dungeon -> {
			dungeon.getInfo().getWorlds().forEach(w -> {
				World world = Bukkit.getWorld(w);
				if (world == null) return;
				if (e.getBlock().getWorld() != world) return;
				Player player = e.getPlayer();
				if (!player.hasPermission("dungeon3.build")) {
					player.sendMessage("§cBạn không thể đặt block");
					e.setCancelled(true);
				}
			});
		});
	}
	
	// Remove blocks of explosion
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		DDataUtils.getDungeons().values().forEach(dungeon -> {
			dungeon.getInfo().getWorlds().forEach(w -> {
				World world = Bukkit.getWorld(w);
				if (world == null) return;
				if (e.getEntity().getWorld() != world) return;
				e.blockList().clear();
			});
		});
	}
	
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
