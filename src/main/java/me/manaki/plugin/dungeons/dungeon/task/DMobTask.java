package me.manaki.plugin.dungeons.dungeon.task;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import me.manaki.plugin.dungeons.dungeon.event.DungeonMobKilledEvent;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyCoin;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.util.Tasks;
import me.manaki.plugin.shops.storage.ItemStorage;
import me.manaki.plugin.dungeons.buff.Buff;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class DMobTask extends BukkitRunnable {

	private final int SPAWN_DELAY_TICK = 30;

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
		this.loc = loc.getBlock().getLocation().add(0.5, 0, 0.5);
		this.runTaskTimer(Dungeons.get(), 0, 20);
	}

	@Override
	public void run() {
		try {
			checkSpawn();
			checkLocation();
			checkValid();
		}
		catch (IllegalArgumentException e) {
			Dungeons.get().getLogger().severe("Mob task catch IllegalArgumentException");
			Dungeons.get().getLogger().severe("Maybe for UnloadedWorld? -> Stop task");
			this.cancel();
		}
	}

	public void checkSpawn() {
		if (isSpawned) return;

		// Has near player
		boolean hasPlayerNear = false;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getWorld() == loc.getWorld()) {
				if (player.getLocation().distance(loc) < 30) {
					hasPlayerNear = true;
					break;
				}
			}
		}
		if (!hasPlayerNear) return;

		// Current maxmobs
		var d = DDataUtils.getDungeon(dungeon);
		int maxmobs = d.getTurn(status.getTurn()).getMaxMobs();
		if (status.getTurnStatus().getCurrentMobs() >= maxmobs) return;

		// Spawn
		status.getTurnStatus().addCurrentMobs(1);
		Tasks.async(this::playReadyEffect, 0, 4, this.SPAWN_DELAY_TICK * 50L);
		Bukkit.getScheduler().runTaskLater(Dungeons.get(), this::spawn, this.SPAWN_DELAY_TICK);
	}

	public void spawn() {
		var d = DDataUtils.getDungeon(dungeon);
		var dct = status.getCache().getDifficulty();
		var mythicmobID = d.getMob(this.mobID, dct);
		ActiveMob activeMob = MythicMobs.inst().getMobManager().spawnMob(mythicmobID, loc);
		LivingEntity le = (LivingEntity) activeMob.getEntity().getBukkitEntity();
		le.setRemoveWhenFarAway(false);
		this.mob = le;
		this.isSpawned = true;
		status.getTurnStatus().addMobToKill(mobID, le);

		// Set
		le.setRemoveWhenFarAway(false);
		le.setMetadata("Dungeon3", new FixedMetadataValue(Dungeons.get(), this.mobID));

		if (d.getOption().isMobGlow())
			le.setGlowing(true);

		for (UUID uuid : status.getPlayers()) {
			var player = Bukkit.getPlayer(uuid);
			if (player == null) continue;
			player.playSound(le.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			player.spawnParticle(Particle.PORTAL, le.getLocation(), 20, 0.3f, 0.3f, 0.3f, 0.5);
		}
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
			status.getTurnStatus().removeCurrentMobs(1);

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

	private void playReadyEffect() {
		var lShow = Utils.getGroundBlock(loc.clone()).add(0, 1.2, 0);
		circleParticles(new Particle.DustOptions(Color.PURPLE, 1), lShow, 1);
	}

	private void circleParticles(Particle.DustOptions doo, Location location, double radius) {
		int amount = new Double(radius * 20).intValue();
		double increment = (2 * Math.PI) / amount;
		ArrayList<Location> locations = new ArrayList<Location>();

		for (int i = 0; i < amount; i++) {
			double angle = i * increment;
			double x = location.getX() + (radius * Math.cos(angle));
			double z = location.getZ() + (radius * Math.sin(angle));
			locations.add(new Location(location.getWorld(), x, location.getY(), z));
		}

		for (Location l : locations) {
//        	ParticleAPI.sendParticle(e, l, 0, 0, 0, 0, 1);
			location.getWorld().spawnParticle(Particle.REDSTONE, l, 1, 0, 0, 0, 0, doo);
		}
	}

}
