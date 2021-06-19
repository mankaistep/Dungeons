package me.manaki.plugin.dungeons.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.dungeon.util.DSlaveUtils;
import me.manaki.plugin.dungeons.guarded.Guardeds;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.v4.world.WorldCache;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class EntityListener implements Listener {

	private final Dungeons plugin;

	public EntityListener(Dungeons plugin) {
		this.plugin = plugin;
	}

	/*
	 * Villager interact
	 */
	@EventHandler
	public void onSlaveClick(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof Villager) {
			Player player = e.getPlayer();
			if (DPlayerUtils.isInDungeon(player)) {
				var entity = e.getRightClicked();
				var status = DPlayerUtils.getStatus(player);
				if (status.getTurnStatus().getSlaveToSaves().contains(entity)) {
					e.setCancelled(true);
					player.sendMessage("§aGiữ nút Shift để Giải cứu");
					player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
				}
			}
		}
	}

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

		boolean isPlayer = false;
		Player player = null;
		if (e.getDamager() instanceof Player) {
			isPlayer = true;
			player = (Player) e.getDamager();
		}
		if (e.getDamager() instanceof Projectile
				&& ((Projectile) e.getDamager()).getShooter() instanceof Player) {
			isPlayer = true;
			player = (Player) ((Projectile) e.getDamager()).getShooter();
		}

		if (!isPlayer) return;

		var entity = (Monster) e.getEntity();
		for (DStatus status : plugin.getDungeonManager().getStatuses()) {
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
		if (!(e.getEntity() instanceof LivingEntity)) return;
		World world = e.getEntity().getWorld();;
		if (!plugin.getWorldManager().isWorldFromDungeon(world.getName())) return;

		LivingEntity le = (LivingEntity) e.getEntity();
		le.setMetadata("Dungeon3.checking", new FixedMetadataValue(Dungeons.get(), ""));
		Bukkit.getScheduler().runTaskLater(Dungeons.get(), ()	 -> {
			if (le instanceof Player) return;
			if (!le.hasMetadata("Dungeon3")
					&& le.getType() != EntityType.PLAYER
					&& MythicMobs.inst().getMobManager().getMythicMobInstance(le) == null
			) le.remove();
		}, 10);
	}
	
}
