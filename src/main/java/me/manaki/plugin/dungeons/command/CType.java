package me.manaki.plugin.dungeons.command;

import com.google.common.collect.Lists;
import io.lumine.xikage.mythicmobs.MythicMobs;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.location.DLocation;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.rank.RankUtils;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public enum CType {

	OPPLAYERCMD {
		@Override
		public void execute(String cmd, Player player) {
			player.setOp(true);
			try {
				Bukkit.dispatchCommand(player, cmd);
			}
			catch (Exception e) {
				player.setOp(false);
				e.printStackTrace();
			}
			player.setOp(false);
		}
	},
	PLAYERCMD {
		@Override
		public void execute(String cmd, Player player) {
			Bukkit.dispatchCommand(player, cmd);
		}
	},
	CONSOLECMD {
		@Override
		public void execute(String cmd, Player player) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
		}
	},
	TELE {
		@Override
		public void execute(String cmd, Player player) {
			var cacheID = DPlayerUtils.getCurrentDungeonCache(player);
			var status = Dungeons.get().getDungeonManager().getStatus(cacheID);
			var dungeonID = status.getCache().getDungeonID();
			var world = status.getCache().getWorldCache().toWorld();
			Dungeon dg = DDataUtils.getDungeon(dungeonID);
			DLocation dl = dg.getLocations().getOrDefault(cmd, null);
			if (dl == null) {
				player.sendMessage(Utils.c("&cLỗi location, thông báo quản trị viên để fix"));
			}
			DGameUtils.teleport(player, dl.getLocation(world));
		}
	},
	MESS {
		@Override
		public void execute(String cmd, Player player) {
			player.sendMessage(Utils.c(cmd));
		}
	},
	BROADCAST {
		@Override
		public void execute(String cmd, Player player) {
			Bukkit.getOnlinePlayers().forEach(p -> {
				p.sendMessage(Utils.c(cmd));
			});
		}
	},
	TITLE {
		@Override
		public void execute(String cmd, Player player) {
			String t = cmd.split(";")[0];
			String s = cmd.split(";")[1];
			int i1 = 10;
			int i2 = 40;
			int i3 = 10;
			if (cmd.split(";").length > 2) {
				i1 = Integer.valueOf(cmd.split(";")[2]);
				i2 = Integer.valueOf(cmd.split(";")[3]);
				i3 = Integer.valueOf(cmd.split(";")[4]);
			}
			player.sendTitle(Utils.c(t), Utils.c(s), i1, i2, i3);
		}
	},
	SOUND {
		@Override
		public void execute(String cmd, Player player) {
			Sound s = Sound.valueOf(cmd.split(";")[0]);
			float f1 = Float.valueOf(cmd.split(";")[1]);
			float f2 = Float.valueOf(cmd.split(";")[2]);
			player.playSound(player.getLocation(), s, f1, f2);
		}
	},
	SHOWRANK {
		@Override
		public void execute(String cmd, Player player) {
			var cacheID = DPlayerUtils.getCurrentDungeonCache(player);
			var status = Dungeons.get().getDungeonManager().getStatus(cacheID);
			var dungeonID = status.getCache().getDungeonID();
			Rank r = RankUtils.getRank(dungeonID, status.getStatistic(player));
			RankUtils.showRank(player, r);
		}
	},
	KILL {
		@Override
		public void execute(String cmd, Player player) {
			List<String> kills = cmd.equals("*") ? null : Lists.newArrayList(cmd.split(";"));
			DStatus s = DPlayerUtils.getStatus(player);
			if (kills == null || kills.contains("slave")) {
				s.getTurnStatus().getSlaveToSaves().forEach(Entity::remove);
			}
			s.getTurnStatus().getMobToKills().forEach((le, id) -> {
				if (kills != null) {
					if (!kills.contains(id)) return;
				}
				le.setMetadata("commandKilled", new FixedMetadataValue(Dungeons.get(), ""));
				le.remove();
			});
			player.getWorld().getEntities().forEach(entity -> {
				if (entity instanceof Item) return;
				if (MythicMobs.inst().getMobManager().getAllMythicEntities().contains(entity))  {
					entity.remove();
				}
			});
			if (s.getTurnStatus().getGuarded() != null) {
				int currentTurn = s.getTurn();
				var cacheID = Dungeons.get().getDungeonManager().getCurrentDungeonCache(player);
				var status = Dungeons.get().getDungeonManager().getStatus(cacheID);
				String id = status.getCache().getDungeonID();
				if (!DGameUtils.isLastTurn(id, currentTurn)) {
					int nextTurn = currentTurn + 1;
					var turn = DDataUtils.getDungeon(id).getTurn(currentTurn);
					DTurn turnNext = DDataUtils.getDungeon(id).getTurn(nextTurn);
					if (!turnNext.getSpawn().getGuarded().isGuardedNull() && turnNext.getSpawn().getGuarded().getGuarded().equals(turn.getSpawn().getGuarded().getGuarded())) return;
				}
			}
			if (s.getTurnStatus().getGuarded() != null) s.getTurnStatus().getGuarded().remove();
		}
	};

	CType() {}

	public abstract void execute(String cmd, Player player);

}
