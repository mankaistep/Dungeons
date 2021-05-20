package me.manaki.plugin.dungeons.dungeon.task;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.guarded.Guarded;
import me.manaki.plugin.dungeons.guarded.Guardeds;
import me.manaki.plugin.dungeons.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class DGuardedTask extends BukkitRunnable {

    private final long LOOK_COOLDOWN = 5000;

    private String dungeonID;
    private String id;
    private Villager entity;
    private Location location;
    private DStatus status;
    private boolean isSpawned;

    private long lastLook;

    public DGuardedTask(String id, String dungeonID, Location location, DStatus status) {
        this.dungeonID = dungeonID;
        this.id = id;
        this.location = location;
        this.status = status;
        this.isSpawned = false;
        this.runTaskTimer(Dungeons.get(), 0, 5);
    }

    @Override
    public void run() {
        checkSpawn();
        checkLocation();
        checkValid();
        checkTarget();
        entityLook();
    }

    public void checkTarget() {
        if (!isSpawned) return;
        for (LivingEntity le : this.status.getTurnStatus().getMobToKills().keySet()) {
            if (!(le instanceof Monster)) continue;
            var m = (Monster) le;
            var g = Guardeds.get(this.id);

            if (m.getWorld() == entity.getWorld() && m.getLocation().distanceSquared(entity.getLocation()) > g.getTargetRadius() * g.getTargetRadius()) continue;
            if (!Guardeds.canTarget(m)) continue;

            m.setTarget(entity);
        }
    }

    public void checkSpawn() {
        if (isSpawned) return;
        boolean hasPlayerNear = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == location.getWorld()) {
                if (player.getLocation().distance(location) < 30) {
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
        this.entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        this.isSpawned = true;

        status.getTurnStatus().setGuarded(entity);

        Guarded g = Guardeds.get(this.id);

        entity.setAI(false);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(Double.MAX_VALUE);;
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(g.getHealth());
        entity.setHealth(g.getHealth());
        entity.setCustomName(g.getName().replace("&", "§"));
        entity.setCustomNameVisible(true);
        entity.setProfession(Villager.Profession.values()[g.getColor() - 1]);
        entity.setMetadata("NPC", new FixedMetadataValue(Dungeons.get(), "shit"));
        entity.setMetadata("Dungeon3", new FixedMetadataValue(Dungeons.get(), ""));
        entity.setMetadata("Guarded", new FixedMetadataValue(Dungeons.get(), ""));

        Dungeon d = DDataUtils.getDungeon(dungeonID);
        if (d.getOption().isMobGlow()) entity.setGlowing(true);
    }

    public void checkValid() {
        if (this.isCancelled()) return;
        if (!isSpawned) return;
        if (!entity.isValid()) {
            // Check Lose Req
            Dungeon d = DDataUtils.getDungeon(this.dungeonID);
            DTurn turn = d.getTurn(status.getTurn());
            if (turn.getLoseRequirement().getGuardedKilled() != null) {
                if (turn.getLoseRequirement().getGuardedKilled().equalsIgnoreCase(this.id)) {
                    // Lose turn
                    Dungeons.get().getDungeonManager().lose(status.getCache().toID());
                    Lang.DUNGEON_LOSE_GUARDED_DEATH.broadcast("%dungeon%", d.getInfo().getName());;
                }
            }

            // Cancel task
            this.cancel();
        }
    }

    public void checkLocation() {
        if (!isSpawned) return;
        if (isFar()) entity.teleport(location);
    }

    public boolean isFar() {
        if (!isSpawned) return false;
        if (entity.getLocation().getWorld() != location.getWorld()) return true;
        if (entity.getLocation().distance(location) > 1) return true;
        return false;
    }

    public void entityLook() {
        if (!isSpawned) return;
        if (System.currentTimeMillis() < lastLook + LOOK_COOLDOWN) return;
        lastLook = System.currentTimeMillis();

        Location l = entity.getLocation();

        LivingEntity nearestE = null;
        double dSmin = 1000;

        for (Entity ent : l.getWorld().getNearbyEntities(l, 10, 10, 10)) {
            if (!(ent instanceof LivingEntity) || ent == entity) continue;
            LivingEntity le = (LivingEntity) ent;
            double dS = le.getLocation().distanceSquared(l);
            if (dS < dSmin) {
                dSmin = dS;
                nearestE = le;
            }
        }

        if (nearestE == null) return;
        l.setDirection(nearestE.getLocation().subtract(entity.getLocation()).toVector());
        entity.teleport(l);
    }

    public String getId() {
        return id;
    }

    public Villager getEntity() {
        return entity;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isSpawned() {
        return isSpawned;
    }
}