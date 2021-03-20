package me.manaki.plugin.dungeons.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.dungeon.util.DSlaveUtils;
import me.manaki.plugin.dungeons.guarded.Guardeds;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.slave.Slaves;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class EntityListener implements Listener {
	
	/*
	 * Villager interact
	 */
	@EventHandler
	public void onInteractVillager(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof Villager) {
			Player player = e.getPlayer();
			if (DPlayerUtils.isInDungeon(player)) {
				e.setCancelled(true);
				Bukkit.getScheduler().runTask(Dungeons.get(), () -> {
					player.closeInventory();
				});
			}
		}
	}
	
	/*
	 * Dungeon mobs target each other
	 */
	
	@EventHandler
	public void onSlaveTargeted(EntityTargetLivingEntityEvent e) {
		LivingEntity target = e.getTarget();
		Entity targeter = e.getEntity();
		if (target == null || targeter == null) return;
		if (target.hasMetadata("Dungeon3") && targeter.hasMetadata("Dungeon3")) {
			e.setCancelled(true);
		}
	}

	/*
	Slave damage
	 */

	@EventHandler
	public void onSlaveDamaged(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity) e.getEntity();
		if (DSlaveUtils.isSlave(target)) {
			e.setCancelled(true);
		}
	}

	/*
	Guarded damaged by player
	 */
	@EventHandler
	public void onGuardDamagedByPlayer(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) return;
		Entity entity = e.getEntity();
		if (Guardeds.is(entity) && e.getDamager() instanceof Player) {
			e.setCancelled(true);
		}
	}

	/*
	Mob damaged by Player
	 */
	@EventHandler
	public void onMobDamagedByPlayer(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Monster)) return;
		if (!(e.getDamager() instanceof Player)) return;

		var entity = (Monster) e.getEntity();
		var player = (Player) e.getDamager();
		for (String dID : DGameUtils.getOnlineDungeons()) {
			var status = DGameUtils.getStatus(dID);
			if (status.getTurnStatus().getMobToKills().containsKey(entity)) {
				Guardeds.setLastEntityTarget(entity, Guardeds.TARGET_COOLDOWN);
				entity.setTarget(player);
			}
		}

	}

	/*
	 * Cancel spawn if:
	 * 1. Has not "Dungeon3" tag
	 * 2. Is not player
	 * 3. Is not Mythicmob
	 */
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity le = (LivingEntity) e.getEntity();
			DDataUtils.getDungeons().values().forEach(dungeon -> {
				dungeon.getInfo().getWorlds().forEach(w -> {
					World world = Bukkit.getWorld(w);
					if (world == null) return;
					if (le.getWorld() == world) {
						Bukkit.getScheduler().runTaskLater(Dungeons.get(), () -> {
							if (le instanceof Player) return;
							if (!le.hasMetadata("Dungeon3") && le.getType() != EntityType.PLAYER) {
								if (MythicMobs.inst().getMobManager().getMythicMobInstance(le) == null) {
									le.remove();
									if (le.getType() == EntityType.VILLAGER) System.out.println(le.getType());
								}
							}
						}, 10);
						return;
					}
				});
			});
		}
	}
	
}
