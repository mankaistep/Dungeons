package me.manaki.plugin.dungeons.listener;

import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DSlaveUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.votekick.KickVote;
import me.manaki.plugin.dungeons.votekick.KickVotes;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Map.Entry;

public class PlayerListener implements Listener {

	private final Dungeons plugin;

	public PlayerListener(Dungeons plugin) {
		this.plugin = plugin;
	}

	/*
	 * Vote kick
	 */
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if (e.getMessage().equalsIgnoreCase("yes")) {
			if (!DPlayerUtils.isInDungeon(player)) return;
			String dungeonCache = DPlayerUtils.getCurrentDungeonCache(player);

			if (!KickVotes.hasVote(dungeonCache)) return;
			KickVote vote = KickVotes.get(dungeonCache);
			
			if (vote.isVoted(player.getName())) {
				Lang.DUNGEON_VOTE_KICK_VOTED.send(player);
				return;
			}
			e.setCancelled(true);
			
			Lang.DUNGEON_VOTE_KICK_VOTED.send(player);
			KickVotes.addVote(dungeonCache, player.getName());

			DStatus status = Dungeons.get().getDungeonManager().getStatus(dungeonCache);
			status.getPlayers().forEach(p -> {
				Player pVoter = Bukkit.getPlayer(vote.getVoter());
				Player pTarget = Bukkit.getPlayer(vote.getTarget());
				
				Map<String, String> plh = Maps.newHashMap();
				plh.put("%voter%", pVoter.getName());
				plh.put("%target%", pTarget.getName());
				plh.put("%vote%", vote.getVotedYes() + "");
				plh.put("%max%", vote.getMax() + "");
				
				Lang.DUNGEON_VOTE_KICK_RESULT.send(Bukkit.getPlayer(p), plh);
			});
			
			if (vote.canEndVote(KickVotes.VOTE_TIME)) {
				KickVotes.endVote(dungeonCache, true);
			}
		}
	}
	
	
	/*
	 * Save slaves
	 */
	
	@EventHandler
	public void onSlaveSlave(PlayerToggleSneakEvent e) {
		if (!e.isSneaking()) return;
		Player player = e.getPlayer();
		
		Location locationn = null;
		LivingEntity lee = null;
		
		for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
			if (DSlaveUtils.isSlave(entity)) {
				if (DSlaveUtils.isSaved(entity)) continue;
				lee = (LivingEntity) entity;
				locationn = lee.getLocation();
				break;
			}
		}
		
		if (lee == null) return;
		
		Location location = locationn;
		LivingEntity le = lee;
		new BukkitRunnable() {
			
			int i = 0;
			final int seconds = 5;
			final int count = seconds * 4;
			
			@Override
			public void run() {
				i++;
				if (DSlaveUtils.isSaved(le)) {
					this.cancel();
					return;
				}
				
				if (!player.isSneaking()) {
					this.cancel();
					return;
				}
				
				if (player.getLocation().distance(location) > 2) {
					this.cancel();
					return;
				} 
				
				if (i >= count) {
					player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, le.getLocation().add(0, 1, 0), 20, 0.5f, 0.5f, 0.5f, 0);
					this.cancel();
					DSlaveUtils.save(player, le);
					return;
				}
				
				int percent = i * 100 / count;
				String s = "§a§o" + percent + "%";
				
				player.sendTitle(s, Lang.DUNGEON_SLAVE_SAVING.get(), 0, 10, 0);
				player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1, 1);
				player.getWorld().spawnParticle(Particle.CRIT, le.getLocation().add(0, 1, 0), 20, 0.4f, 0.2f, 0.4f, 0);
			}
		}.runTaskTimerAsynchronously(Dungeons.get(), 0, 5);
	}
	
	/*
	 * Cancel teleport
	 */
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		if (player.hasPermission("dungeon.teleport")) return;

		Location to = e.getTo();
		for (Entry<String, Dungeon> entry : DDataUtils.getDungeons().entrySet()) {
			var id = entry.getKey();
			var d = entry.getValue();
			if (plugin.getWorldManager().getActiveWorldNames(id).contains(to.getWorld().getName())) {
				if (!DGameUtils.checkTeleport(player)) {
					e.setCancelled(true);
					Lang.DUNGEON_TELEPORT_FAIL.send(player);
					return;
				}
				// Check mob nearby
				else {
					var dungeonCache = DPlayerUtils.getCurrentDungeonCache(player);
					var status = Dungeons.get().getDungeonManager().getStatus(dungeonCache);

					// Check if nearby player = 0
					if (Utils.countNearBy(player.getLocation(), 20, EntityType.PLAYER) > 1) return;

					// Check if distance > 100
					for (Entry<LivingEntity, String> entry2 : status.getTurnStatus().getMobToKills().entrySet()) {
						LivingEntity le = entry2.getKey();
						if (le.getLocation().distance(to) > 100) {
							le.teleport(to);
						}
					}
				}
			}
		}
	}
	
	/*
	 * Cancel gliding
	 */
	@EventHandler
	public void onGlide(EntityToggleGlideEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		if (player.hasPermission("dungeon.teleport")) return;
		if (DPlayerUtils.getCurrentDungeonCache(player) == null) return;

		var dungeonCache = DPlayerUtils.getCurrentDungeonCache(player);
		var status = Dungeons.get().getDungeonManager().getStatus(dungeonCache);
		Dungeon d = DDataUtils.getDungeon(status.getCache().getDungeonID());
		Location l = player.getLocation();
		
		if (plugin.getWorldManager().getActiveWorldNames(d.getID()).contains(l.getWorld().getName())) {
			player.sendMessage("§cKhông thể bay trong dungeon");
			e.setCancelled(true);
		}
		
	}
	
	/*
	 * Cancel fly
	 */
	@EventHandler
	public void onGlide(PlayerToggleFlightEvent e) {
		Player player = e.getPlayer();
		if (player.hasPermission("dungeon.teleport")) return;
		if (DPlayerUtils.getCurrentDungeonCache(player) == null) return;

		var dungeonCache = DPlayerUtils.getCurrentDungeonCache(player);
		var status = Dungeons.get().getDungeonManager().getStatus(dungeonCache);
		Dungeon d = DDataUtils.getDungeon(status.getCache().getDungeonID());
		Location l = player.getLocation();
		
		if (plugin.getWorldManager().getActiveWorldNames(d.getID()).contains(l.getWorld().getName())) {
			player.sendMessage("§cKhông thể bay trong dungeon");
			e.setCancelled(true);
		}
		
	}
	
	/*
	 * Check velocity
	 */
	@EventHandler
	public void onVelocity(PlayerVelocityEvent e) {
		Player player = e.getPlayer();
		if (player.hasPermission("dungeon.teleport")) return;
		if (DPlayerUtils.getCurrentDungeonCache(player) == null) return;

		var dungeonCache = DPlayerUtils.getCurrentDungeonCache(player);
		var status = Dungeons.get().getDungeonManager().getStatus(dungeonCache);
		Dungeon d = DDataUtils.getDungeon(status.getCache().getDungeonID());
		if (d.getOption().isVelocityAllowed()) return;

		Location l = player.getLocation();
		
		if (plugin.getWorldManager().getActiveWorldNames(d.getID()).contains(l.getWorld().getName())) {
			player.sendMessage("§cHành động đã bị chặn trong dungeon này");
			e.setCancelled(true);
		}
		
	}
	
	/*
	 * Player god
	 */
	@EventHandler
	public void onDamaged(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (DPlayerUtils.isGod(player)) e.setCancelled(true);
		}
	}
	
}
