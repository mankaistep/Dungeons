package me.manaki.plugin.dungeons.dungeon.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.location.DLocation;
import me.manaki.plugin.dungeons.dungeon.rewardreq.DRewardReq;
import me.manaki.plugin.dungeons.dungeon.statistic.DStatistic;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TSMob;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.rank.RankUtils;
import me.manaki.plugin.dungeons.util.Utils;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DGameUtils {


	public static boolean isPlaying(String id) {
		var plugin = Dungeons.get();
		for (DStatus status : plugin.getDungeonManager().getStatuses()) {
			if (status.getCache().getDungeonID().equalsIgnoreCase(id)) return true;
		}
		return false;
	}
	
	public static boolean canStart(String id) {
		return !isPlaying(id);
	}
	
	public static boolean isLastTurn(String id, int turn) {
		return turn == DDataUtils.getDungeon(id).getTurns().size();
	}
	
	public static DTurn getTurn(String id, int turn) {
		Dungeon dungeon = DDataUtils.getDungeon(id);
		return dungeon.getTurns().get(turn - 1);
	}
	
	public static void checkAndRemove(Entity e) {
		if (e instanceof Player) return;
		if (e.hasMetadata("Dungeon3.checking")) return;
		if (e.hasMetadata("NPC") && !e.hasMetadata("Dungeon3")) return;
		if (e instanceof LivingEntity || e instanceof Item) e.remove();
	}
	
	public static String getStandingCheckpoint(String id, Location l) {
		Dungeon d = DDataUtils.getDungeon(id);
		for (String cp : d.getCheckPoints()) {
			DLocation check = d.getLocation(cp);
			if (Utils.distance(check, l) <= check.getRadius()) return cp;
		}
		return null;
	}
	
	public static boolean canChangeCheckpoint(String id, String from, String to) {
		if (from == null) return true;
		if (to == null) return false;
		if (from.equals(to)) return false;
		Dungeon d = DDataUtils.getDungeon(id);
		for (String cp : d.getCheckPoints()) {
			if (cp.equals(from)) return true;
			if (cp.equals(to)) return false;
		}
		return false;
	}
	
	public static int countMobs(DTurn turn) {
		int sum = 0;
		for (TSMob m : turn.getSpawn().getMobs()) {
			sum += m.getAmount();
		}
		return sum;
 	}
	
	public static int countMobs(Dungeon d) {
		int sum = 0;
		for (DTurn turn : d.getTurns()) {
			for (TSMob m : turn.getSpawn().getMobs()) {
				sum += m.getAmount();
			}
		}

		return sum;
 	}
	
	public static int countSlaves(DTurn turn) {
		return turn.getSpawn().getSlaves().size();
	}
	
	public static int countSlaves(Dungeon d) {
		int sum = 0;
		for (DTurn turn : d.getTurns()) {
			sum += countSlaves(turn);
		}
		return sum;
	}
	
	public static boolean canGetReward(String id, DStatistic s, DRewardReq rr) {
		if (rr.getKillCount() > s.getMobKilled()) return false;
		if (rr.getMaxDead() < s.getDead()) return false;
		if (rr.getSave() > s.getSlaveSaved()) return false;
		if (!(s.getListKilled().containsAll(rr.getKills()))) return false;
		if (!RankUtils.equalOrBetter(RankUtils.getRank(id, s), rr.getRank())) return false;
		return true;
	}
	
	public static void teleport(Player player, Location location) {
		player.setMetadata("dungeon-teleport", new FixedMetadataValue(Dungeons.get(), ""));
		player.teleport(location);
	}
	
	public static boolean isDungeonTeleport(Player player) {
		return player.hasMetadata("dungeon-teleport");
	}
	
	public static boolean checkTeleport(Player player) {
		if (isDungeonTeleport(player)) {
			player.removeMetadata("dungeon-teleport", Dungeons.get());
			return true;
		}
		return false;
	}
	
	public static String checkLocation(String id, Location l) {
		Dungeon d = DDataUtils.getDungeon(id);
		for (Entry<String, DLocation> dl : d.getLocations().entrySet()) {
			if (dl.getValue().getLocation(l.getWorld()).getWorld() == l.getWorld() && dl.getValue().getLocation(l.getWorld()).getBlock().getLocation().equals(l.getBlock().getLocation())) {
				return dl.getKey();
			}
		}
		return null;
	}
	
	public static void sendChestOpen(Player player, Location l) {
		BlockPosition pos = new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, 1);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
	
}
