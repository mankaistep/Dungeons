package me.manaki.plugin.dungeons.v4.dungeon.manager;

import be.maximvdw.featherboard.api.FeatherBoardAPI;
import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.command.Command;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.block.DBlock;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.dungeon.event.DungeonFinishEvent;
import me.manaki.plugin.dungeons.dungeon.event.DungeonStartEvent;
import me.manaki.plugin.dungeons.dungeon.location.DLocation;
import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.rewardreq.DRewardReq;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.status.DungeonResult;
import me.manaki.plugin.dungeons.dungeon.task.*;
import me.manaki.plugin.dungeons.dungeon.turn.TSGuarded;
import me.manaki.plugin.dungeons.dungeon.turn.status.TStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.util.Tasks;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.v4.dungeon.cache.DungeonCache;
import me.manaki.plugin.dungeons.v4.world.WorldCache;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class DungeonManager {
    
    private final Dungeons plugin;

    private final List<DStatus> statuses;

    public DungeonManager(Dungeons plugin) {
        this.plugin = plugin;
        this.statuses = Lists.newArrayList();
    }

    public Dungeons getPlugin() {
        return plugin;
    }

    public DStatus getStatus(String cacheID) {
        for (DStatus status : statuses) {
            if (status.getCache().toID().equalsIgnoreCase(cacheID)) return status;
        }
        return null;
    }

    public List<DStatus> getStatuses() {
        return statuses;
    }

    public void removeStatus(DStatus status) {
        this.statuses.remove(status);
    }

    public void start(String dungeonID, Difficulty difficulty, List<UUID> players) {
        // Remove offline players and set not flying
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                continue;
            }
            player.setFlying(false);
            player.sendMessage("§aĐang khởi tạo phó bản, hãy đợi!");
        }

        // Create world
        var dungeon = DDataUtils.getDungeon(dungeonID);
        var worldSource = dungeon.getInfo().getWorld();
        var worldTemplate = plugin.getV4Config().getWorldTemplate(worldSource);
        var loader = plugin.getWorldLoader();
        WorldCache worldCache = null;
        try {
            worldCache = loader.load(worldTemplate, false, true);
        }
        catch (Exception e) {
            plugin.getLogger().warning("Exception appeared when world is being loaded");
            e.printStackTrace();
            return;
        }
        plugin.getWorldManager().addActiveWorld(dungeonID, worldCache);

        // Wait to do
        WorldCache finalWorldCache = worldCache;
        new BukkitRunnable() {
            @Override
            public void run() {
                // Wait till load done
                if (loader.isLoading(finalWorldCache.toWorldName())) return;

                // Cancel task
                this.cancel();

                // Create status
                var dungeonCache = new DungeonCache(dungeonID, difficulty, finalWorldCache);
                var bossbar = Dungeons.get().featherBoard == null ? Bukkit.createBossBar("§c§l" + dungeon.getInfo().getName() + " §f§l" + Utils.getFormat(dungeon.getOption().getMaxTime()), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC) : null;
                var status = new DStatus(dungeonCache, System.currentTimeMillis(), players, bossbar);
                var turnstatus = new TStatus();
                status.setTurnStatus(turnstatus);
                if (bossbar != null) players.forEach(uuid -> bossbar.addPlayer(Objects.requireNonNull(Bukkit.getPlayer(uuid))));
                statuses.add(status);

                // Spawn block
                spawnBlocks(dungeonID, dungeonCache.getWorldCache().toWorld());

                // Teleport to first check-point
                // Have to wait till world loaded
                var world = finalWorldCache.toWorld();
                status.getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    Location l = dungeon.getLocation(dungeon.getCheckPoints().get(0)).getLocation(world);
                    DGameUtils.teleport(player, l);
                });

                // Create game task
                var gameTask = new DGameTask(dungeonCache.toID(), status, System.currentTimeMillis());
                status.addTask(gameTask);

                // Create player task
                status.getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    status.addTask(new DPlayerTask(player, dungeonID, status));
                });

                // Start turn
                start(dungeonCache.toID(), 1, null);

                // Featherboard
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    featherBoardCheck(p, false);
                }

                // Event
                Bukkit.getPluginManager().callEvent(new DungeonStartEvent(dungeonID));
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public void startNextTurn(String dungeonCache, DGuardedTask guardTask) {
        var status = getStatus(dungeonCache);
        int turn = status.getTurn();

        // Check if end
        if (DGameUtils.isLastTurn(status.getCache().getDungeonID(), turn)) {
            win(dungeonCache);
        }

        // Start next
        else start(dungeonCache, turn + 1, guardTask);
    }

    public void start(String dungeonCache, int turn,  DGuardedTask guardTask) {
        var ds = getStatus(dungeonCache);
        var world = ds.getCache().getWorldCache().toWorld();
        var dungeonID = ds.getCache().getDungeonID();
        var dturn = DGameUtils.getTurn(dungeonID, turn);

        ds.setTurn(turn);
        ds.setTurnStatus(new TStatus());

        // Run commands
        runStartCommands(dungeonCache, turn);

        // Spawn
        Bukkit.getScheduler().runTaskLater(Dungeons.get(), () -> {
            if (!isPlaying(dungeonCache)) return;
            spawnBlockBreaks(dungeonCache, turn);
            spawnMobs(dungeonCache, turn);
            spawnSlaves(dungeonCache, turn);
            spawnGuarded(dungeonCache, turn, guardTask);
        }, dturn.getSpawn().getDelay());
    }

    public void win(String dungeonCache, int turn, BukkitRunnable task) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();

        // Chests
        t.getChests().forEach(chest -> {
            if (!Utils.rate(chest.getChance())) return;
            Location l = d.getLocation(chest.getLocation()).getLocation(world);
            l.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, l, 1, 0, 0, 0, 0);
            l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            l.getBlock().setType(Material.CHEST);
        });

        // Win commands
        try {
            t.getCommand().getWins().forEach(cmd -> {
                status.getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    new Command(cmd).execute(player);
                });
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Clear tasks
            DGuardedTask guardedTask = null;
            for (BukkitRunnable btask : status.getTasks()) {
                if (btask instanceof DMobTask || btask instanceof DSlaveTask) {
                    btask.cancel();
                }
                else if (btask instanceof DGuardedTask) guardedTask = (DGuardedTask) btask;
            }

            // New status
            status.setTurnStatus(new TStatus());

            // Check next
            if (DGameUtils.isLastTurn(dungeonID, turn)) {
                if (guardedTask != null) guardedTask.cancel();
                task.cancel();
                win(dungeonCache);
            }
            else startNextTurn(dungeonCache, guardedTask);
        }
    }

    public void win(String dungeonCache) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();

        Lang.DUNGEON_WIN.broadcast("%dungeon%", d.getInfo().getName());

        try {
            // Check players
            status.getPlayers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                // Check requirements
                DRewardReq rr = d.getRewardReq();
                if (DGameUtils.canGetReward(dungeonID, status.getStatistic(uuid), rr)) {
                    Difficulty difficulty = status.getCache().getDifficulty();
                    for (String s : d.getReward().getReward(difficulty)) {
                        new Command(s).execute(player);
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            doFinish(dungeonCache, DungeonResult.WIN);
        }
    }

    public void lose(String dungeonCache) {
        var status = getStatus(dungeonCache);
        var turn = status.getTurn();
        var dturn = DGameUtils.getTurn(status.getCache().getDungeonID(), turn);

        try {
            // Lose commands
            dturn.getCommand().getLoses().forEach(cmd -> {
                status.getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    new Command(cmd).execute(player);
                });
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            doFinish(dungeonCache, DungeonResult.LOSE);
        }
    }

    public void doFinish(String dungeonCache, DungeonResult result) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();

        // Clear task + boss bar
        BossBar bb = status.getBossBar();
        if (bb != null) bb.removeAll();
        status.getTasks().forEach(br -> {
            if (!br.isCancelled()) br.cancel();
        });
        List<UUID> remainPlayers = status.getPlayers();

        // Log
        System.out.println("[Dungeon3] Dungeon " + dungeonID + " finished (" + result.name() + ")");

        // FeatherBoard
        for (UUID uuid : remainPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            featherBoardCheck(player, true);
        }

        // Quitting task
        long start = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = inDungeonFilter(remainPlayers, dungeonID);
                if (System.currentTimeMillis() - start >= 60000 || players.size() == 0) {
                    // Back to spawn
                    // player.teleport(Utils.getPlayerSpawn());
                    players.forEach(Utils::toSpawn);

                    Tasks.async(() -> {
                        // Remove temporary world
                        plugin.getWorldLoader().unload(status.getCache().getWorldCache().toWorldName(), true);
                    });

                    // Remove caches
                    plugin.getWorldManager().removeActiveWorld(dungeonID, status.getCache().getWorldCache());
                    removeStatus(status);
                    this.cancel();
                    return;
                }
                int remain = new Long(60 - (System.currentTimeMillis() - start) / 1000 - 1).intValue();
                players.forEach(player -> {
                    player.sendActionBar(Lang.DUNGEON_30_SECOUNDS_OUT.get().replace("%seconds%", remain + ""));
                });
            }
        }.runTaskTimer(Dungeons.get(), 0, 5);

        // Call event
        Bukkit.getPluginManager().callEvent(new DungeonFinishEvent(dungeonID, status, result));
    }

    public boolean isPlaying(String dungeonCache) {
        for (DStatus status : this.statuses) {
            if (status.getCache().toID().equalsIgnoreCase(dungeonCache)) return true;
        }
        return false;
    }

    public void clearEntities(String dungeonID, World world) {
        if (world != null) {
            world.getEntities().forEach(DGameUtils::checkAndRemove);
        }
    }

    public void spawnBlocks(String dungeonID, World world) {
        Dungeon d = DDataUtils.getDungeon(dungeonID);
        d.getBlocks().forEach((i, b) -> {
            Location l = b.getLocation(world);
            l.getBlock().setType(b.getMaterial());
        });
    }

    public void spawnBlockBreaks(String dungeonCache, int turn) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();

        t.getSpawn().getBlockBreaks().forEach(bid -> {
            DBlock b = d.getBlocks().get(bid);
            var location = b.getLocation(world);
            location.getBlock().setType(Material.AIR);
            location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        }); 
    }

    public void runStartCommands(String dungeonCache, int turn) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        t.getCommand().getStarts().forEach(s -> {
            status.getPlayers().forEach(uudungeonID -> {
                Player player = Bukkit.getPlayer(uudungeonID);
                new Command(s).execute(player);
            });
        });
    }

    public void spawnMobs(String dungeonCache, int turn) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();
        t.getSpawn().getMobs().forEach(m -> {
            String mob = m.getMob();
            int amount = m.getAmount();
            int c = 0;
            while (c < amount) {
                c++;
                DLocation dl = d.getLocation(m.getLocation());
                Location l = Utils.random(dl.getLocation(world), dl.getRadius());
                BukkitRunnable br = new DMobTask(dungeonID, mob, l, status);
                status.addTask(br);
            }
        });
    }

    public void spawnSlaves(String dungeonCache, int turn) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();

        t.getSpawn().getSlaves().forEach(slv -> {
            DLocation dl = d.getLocation(slv.getLocation());
            Location l = dl.getLocation(world);
            BukkitRunnable br = new DSlaveTask(slv.getSlave(), dungeonID, l, status);
            status.addTask(br);
        });
    }

    public void spawnGuarded(String dungeonCache, int turn, DGuardedTask guardedTask) {
        var status = getStatus(dungeonCache);
        var dungeonID = status.getCache().getDungeonID();
        var t = DGameUtils.getTurn(dungeonID, turn);
        var d = DDataUtils.getDungeon(dungeonID);
        var world = status.getCache().getWorldCache().toWorld();
        TSGuarded guard = t.getSpawn().getGuarded();
        if (guard.getGuarded() == null) return;
        if (guardedTask != null && guardedTask.getId().equalsIgnoreCase(guard.getGuarded())) {
            status.addTask(guardedTask);
            status.getTurnStatus().setGuarded(guardedTask.getEntity());

            return;
        }
        if (guardedTask != null) status.cancelTask(guardedTask);
        if (guard.isGuardedNull()) return;


        DLocation dl = d.getLocation(guard.getLocation());
        Location l = dl.getLocation(world);
        var br = new DGuardedTask(guard.getGuarded(), dungeonID, l, status);
        status.addTask(br);
    }

    public void featherBoardCheck(Player player, boolean isDefault) {
        if (plugin.featherBoard == null) return;
        if (isDefault) FeatherBoardAPI.resetDefaultScoreboard(player);
        else FeatherBoardAPI.showScoreboard(player, Dungeons.get().featherBoard);
    }

    public List<Player> inDungeonFilter(List<UUID> uuids, String id) {
        Dungeon d = DDataUtils.getDungeon(id);
        return uuids.stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null && plugin.getWorldManager().getActiveWorldNames(d.getID()).contains(Bukkit.getPlayer(uuid).getWorld().getName()))
                .map(Bukkit::getPlayer)
                .collect(Collectors.toList());
    }

    public String getCurrentDungeonCache(Player player) {
        for (DStatus status : this.statuses) {
            if (status.getPlayers().contains(player.getUniqueId())) return status.getCache().toID();
        }
        return null;
    }

    public void kick(Player player, boolean toSpawn) {
        String id = getCurrentDungeonCache(player);
        if (id == null) return;

        DStatus sd = getStatus(id);
        BossBar bb = sd.getBossBar();

        if (bb != null) bb.removePlayer(player);
        sd.removePlayer(player);
        if (toSpawn) Utils.toSpawn(player);
        Lang.DUNGEON_PLAYER_KICK.send(player);

        featherBoardCheck(player, true);
    }

    public void dead(Player player, boolean teleport) {
        String cacheID = getCurrentDungeonCache(player);
        if (cacheID == null) return;

        var sd = getStatus(cacheID);
        var dungeonID = sd.getCache().getDungeonID();
        var world = sd.getCache().getWorldCache().toWorld();
        Dungeon d = DDataUtils.getDungeon(dungeonID);
        DStatistic pds = sd.getStatistic(player);

        pds.addDead(1);
        sd.getAllStatistic().addDead(1);
        sd.getTurnStatus().getStatistic().addDead(1);

        int maxDead = d.getRule().getRespawnTime() + DPlayer.from(player).getReviveBuff();
        if (pds.getDead() > maxDead) {
            BossBar bb = sd.getBossBar();
            if (bb != null) bb.removePlayer(player);
            sd.removePlayer(player);
            if (teleport) Utils.toSpawn(player);
            sd.getPlayers().forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                Lang.DUNGEON_PLAYER_DEAD_KICK_OTHER.send(p, "%player%", "" + player.getName());
            });
            Lang.DUNGEON_PLAYER_DEAD_KICK.send(player, "%dead_remain%", "" + (maxDead - pds.getDead()));

            // Featherboard
            featherBoardCheck(player, true);
            return;
        }

        // God in 3s
        DPlayerUtils.setGod(player, 20 * 3);
        if (teleport) DGameUtils.teleport(player, d.getLocation(sd.getCheckpoint()).getLocation(world));
        Lang.DUNGEON_PLAYER_DEAD_RESPAWN.send(player, "%dead_remain%", "" + (maxDead - pds.getDead()));
        Lang.DUNGEON_PLAYER_GOD.send(player, "%second%", "3");
    }

}
