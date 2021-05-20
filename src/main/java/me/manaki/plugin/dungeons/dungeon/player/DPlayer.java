package me.manaki.plugin.dungeons.dungeon.player;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class DPlayer {
	
	private static final String DROP_RATE_PERM = "dungeon3.droprate.buff.";
	private static final String REVIVE_RATE_PERM = "dungeon3.revive.buff.";
	
	private double dropRateBuff;
	private int reviveBuff;
	
	public DPlayer(double dropRateBuff, int reviveBuff) {
		this.dropRateBuff = dropRateBuff;
		this.reviveBuff = reviveBuff;
	}
	
	public double getDropRateBuff() {
		return this.dropRateBuff;
	}
	
	public int getReviveBuff() {
		return this.reviveBuff;
	}
	
	public int getMaxDead(String id) {
		Dungeon d = DDataUtils.getDungeon(id);
		return d.getRule().getRespawnTime() + this.getReviveBuff();
	}
	
	public static DPlayer from(Player p) {
		double dr = 0;
		int rb = 0;

		if (p != null) {
			for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
				var node = perm.getPermission();
				if (node.contains(DROP_RATE_PERM)) dr = Math.max(dr, Double.parseDouble(node.replace(DROP_RATE_PERM, "")));
				if (node.contains(REVIVE_RATE_PERM)) rb = Math.max(rb, Integer.parseInt(node.replace(REVIVE_RATE_PERM, "")));
			}
		}

		return new DPlayer(dr, rb);
	}
	
}
