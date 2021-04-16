package me.manaki.plugin.dungeons.dungeon.task;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import me.manaki.plugin.dungeons.dungeon.event.DungeonMobKilledEvent;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyCoin;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.shops.storage.ItemStorage;
import me.manaki.plugin.dungeons.buff.Buff;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class DMobTask extends BukkitRunnable {

	private String dungeon;
	private String mobID;
	private LivingEntity mob;
	private Location loc;
	private DStatus status;
	private boolean isSpawned;

	public DMobTask(String dungeon, String mobID, Location loc, DStatus status) {
		this.dungeon = dungeon;
		this.mobID = mobID;
		this.status = status;
		this.isSpawned = false;
		this.loc = loc;
		this.runTaskTimer(Dungeons.get(), 0, 20);
	}

	@Override
	public void run() {
		checkSpawn();
		checkLocation();
		checkValid();
	}

	public void checkSpawn() {
		if (isSpawned) return;
		var d = DDataUtils.getDungeon(dungeon);
		boolean hasPlayerNear = false;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getWorld() == loc.getWorld()) {
				if (player.getLocation().distance(loc) <= d.getOption().getSpawnRadius()) {
					hasPlayerNear = true;
					break;
				}
			}
		}
		if (hasPlayerNear) {
			spawn();
		}
	}

	public void spawn() {
		ActiveMob activeMob = MythicMobs.inst().getMobManager().spawnMob(mobID, loc);
		LivingEntity le = (LivingEntity) activeMob.getEntity().getBukkitEntity();
		le.setRemoveWhenFarAway(false);
		this.mob = le;
		this.isSpawned = true;
		status.getTurnStatus().addMobToKill(mobID, le);

		// Set
		le.setRemoveWhenFarAway(false);
		le.setMetadata("Dungeon3", new FixedMetadataValue(Dungeons.get(), this.mobID));

		Dungeon d = DDataUtils.getDungeon(dungeon);
		if (d.getOption().isMobGlow())
			le.setGlowing(true);
	}

	public void checkLocation() {
		Dungeon d = DDataUtils.getDungeon(dungeon);
		if (this.mob == null || this.mob.getLocation() == null) return;
		Material m = this.mob.getLocation().getBlock().getType();
		if (d.getRule().isLavaDead() || d.getRule().isWaterDead()) {
			if (m == Material.LAVA) {
//				this.mob.teleport(this.loc);
				teleportToNearestPlayer(this.mob);
			}
			if (d.getRule().isWaterDead()) {
				if (m == Material.WATER) {
//					this.mob.teleport(this.loc);
					teleportToNearestPlayer(this.mob);
				}
			}
		}

		// Radius
		if (this.mob.getLocation().getY() <= d.getRule().getYDead()) {
			// Teleport to nearest player
			teleportToNearestPlayer(this.mob);
		}
	}
	
	private void teleportToNearestPlayer(Entity e) {
		Entity target = Utils.getNearest(this.loc, 50, EntityType.PLAYER);
		if (target == null) return;
		this.mob.teleport(target);
	}
	
	public void checkValid() {
		if (!isSpawned)
			return;
		if (!mob.isValid()) {

			// Check if killed by command
			if (mob.hasMetadata("commandKilled")) return;

			status.getTurnStatus().removeMobToKill(mob);

			// Turn
			status.getTurnStatus().getStatistic().addMobKilled(1);
			status.getTurnStatus().getStatistic().addKilled(mobID);

			// Add Statistic
			status.getAllStatistic().addMobKilled(1);

			// Cancel
			this.cancel();

			if (mob.getKiller() != null) {

				// Killer
				Player killer = mob.getKiller();
				DStatistic s = status.getStatistic(killer);
				s.addMobKilled(1);

				// Event
				Bukkit.getPluginManager().callEvent(new DungeonMobKilledEvent(dungeon, mobID, mob, killer));
			}


			Dungeon d = DDataUtils.getDungeon(dungeon);

			// Drop money
			d.getMoneyDrops().forEach(drop -> {
				if (drop.getMobID().equals(this.mobID) || drop.getMobID().equals("*")) {
					double originalChance = drop.getChance();
					double chance = originalChance * (1 + Buff.DROP);

					// Player buff
					Player killer = mob.getKiller();
					if (killer != null) {
						DPlayer dp = DPlayer.from(killer);
						chance *= (double) (100 + dp.getDropRateBuff()) / 100;
					}

					// Rate
					if (!Utils.rate(chance)) return;

					// Drop
					DMoneyCoin.drop(mob.getLocation(), new DMoneyCoin(drop.getValue()));
				}
			});

			// Drop item
			d.getDrops().forEach(drop -> {
				if (drop.getMobID().equals(this.mobID) || drop.getMobID().equals("*")) {
					int amount = drop.getAmount();
					for (int j = 0; j < amount; j++) {
						double originalChance = drop.getChance();
						double chance = originalChance * Buff.DROP;

						// Player buff
						Player killer = mob.getKiller();
						if (killer != null) {
							DPlayer dp = DPlayer.from(killer);
							chance *= (double) (100 + dp.getDropRateBuff()) / 100;
						}

						if (!Utils.rate(chance)) continue;
						ItemStack is = null;

						// Hook
						if (Bukkit.getPluginManager().isPluginEnabled("Shops")) {
							is = ItemStorage.get(drop.getItemID());
						}

						Item i = loc.getWorld().dropItem(Utils.getRandomSpawnLocation(mob.getLocation(), 0), is);
						i.setCustomNameVisible(true);
						i.setPickupDelay(20);
						i.setGlowing(true);
					}
				}
			});


		}
	}

}
