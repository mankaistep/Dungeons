package me.manaki.plugin.dungeons.dungeon.util;

import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import org.bukkit.entity.Player;

import java.util.Map;

public class DPlayerUtils {
	
	public static Map<String, Long> godList = Maps.newHashMap();
	
	public static boolean isGod(Player player) {
		if  (godList.containsKey(player.getName())) {
			boolean isGod = godList.get(player.getName()) > System.currentTimeMillis();
			if (!isGod) godList.remove(player.getName());
			return isGod;
		}
		return false;
	}
	
	public static void setGod(Player player, int tick) {
		godList.put(player.getName(), System.currentTimeMillis() + tick * 1000 / 20);
	}
	
	public static String getCurrentDungeonCache(Player player) {
		return Dungeons.get().getDungeonManager().getCurrentDungeonCache(player);
	}
	
	public static DStatus getStatus(Player player) {
		var plugin = Dungeons.get();
		String cacheID = plugin.getDungeonManager().getCurrentDungeonCache(player);
		return plugin.getDungeonManager().getStatus(cacheID);
	}
	
	public static Map<String, String> getPlaceholders(Player player) {
		var plugin = Dungeons.get();
		Map<String, String> map = Maps.newHashMap();
		map.put("%player%", player.getName());
		String cacheID = plugin.getDungeonManager().getCurrentDungeonCache(player);
		if (cacheID != null) {
			var id = plugin.getDungeonManager().getStatus(cacheID).getCache().getDungeonID();
			map.put("%dungeon%", DDataUtils.getDungeon(id).getInfo().getName());
		}
		else map.put("%dungeon%", "dungeon");
		
		
		return map;
	}
	
	public static boolean isInDungeon(Player player) {
		var plugin = Dungeons.get();
		return plugin.getDungeonManager().getCurrentDungeonCache(player) != null;
	}
	
	public static boolean isInDungeon(Player player, String cacheID) {
		var status = Dungeons.get().getDungeonManager().getStatus(cacheID);
		if (status == null) return false;
		return status.getPlayers().contains(player.getUniqueId());
	}
	
	public static boolean isInDungeonWorld(Player player) {
		for (DStatus status : Dungeons.get().getDungeonManager().getStatuses()) {
			if (status.getCache().getWorldCache().toWorldName().equalsIgnoreCase(player.getWorld().getName())) return true;
		}
		return false;
	}
	
	public static String getDungeonCachePlayerStandingOn(Player player) {
		for (DStatus status : Dungeons.get().getDungeonManager().getStatuses()) {
			if (status.getCache().getWorldCache().toWorldName().equalsIgnoreCase(player.getWorld().getName())) return status.getCache().toID();
		}
		return null;
	}
	
}
