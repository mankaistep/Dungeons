package me.manaki.plugin.dungeons.queue;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.util.ItemStackUtils;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public enum DQueueStatus {

	CAN_JOIN {
		@Override
		public ItemStack getIcon(String id, Player player) {
			Dungeon d = DDataUtils.getDungeon(id);
			DQueue q = DQueues.getQueue(id);

			ItemStack item = new ItemStack(d.getOption().getGUIIcon());
			item.setDurability(Utils.getColorDurability(DyeColor.GREEN, d.getOption().getGUIIcon()));
			ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

			List<String> lore = Lists.newArrayList();
			lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
			lore.add("");
			lore.add("§3Người chơi: §f" + q.getPlayers().size() + "/" + q.getMax());
			lore.add("§3Trạng thái: §fCó thể vào");
			if (d.getOption().isTicketRequired()) {
				lore.add("§3Tiêu thụ: §fx1 Vé");
			}

			ItemStackUtils.setLore(item, lore);

			item = ItemStackUtils.setTag(item, "dungeonClick", "join");
			item = ItemStackUtils.setTag(item, "dungeonID", id);

			return item;
		}
	},
	CANT_JOIN {
		@Override
		public ItemStack getIcon(String id, Player player) {
			Dungeon d = DDataUtils.getDungeon(id);

			ItemStack item = new ItemStack(d.getOption().getGUIIcon());
			item.setDurability(Utils.getColorDurability(DyeColor.RED, d.getOption().getGUIIcon()));
			ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

			List<String> lore = Lists.newArrayList();
			lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
			lore.add("");
			lore.add("§3Trạng thái: §cKhông thể vào");
			lore.add("§3Yêu cầu: ");
			lore.add("§c  Số lượng: §f" + d.getOption().getPlayer().getMin() + " >> " + d.getOption().getPlayer().getMax());
			lore.add("§c  Cấp độ: §f" + d.getOption().getLevel().getMin() + " >> " + d.getOption().getLevel().getMax());
			lore.add("§c  Vé vào: §f" + (d.getOption().isTicketRequired() ? "x1 Vé" : "Không"));

			ItemStackUtils.setLore(item, lore);

			return item;
		}
	},
	WAITING {
		@Override
		public ItemStack getIcon(String id, Player player) {
			Dungeon d = DDataUtils.getDungeon(id);
			DQueue q = DQueues.getQueue(id);

			ItemStack item = new ItemStack(d.getOption().getGUIIcon());
			item.setDurability(Utils.getColorDurability(DyeColor.YELLOW, d.getOption().getGUIIcon()));
			ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

			List<String> lore = Lists.newArrayList();
			lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
			lore.add("");
			lore.add("§3Người chơi: §f" + q.getPlayers().size() + "/" + q.getMax() + "§7 (Tối thiểu: " + d.getOption().getPlayer().getMin() + ")");
			lore.add("§3Trạng thái: §fĐang chờ, có thể thoát");
			lore.add("§3Đếm ngược:  §f" + DQueues.getSecondsRemain(id) + "s");

			ItemStackUtils.setLore(item, lore);

			item = ItemStackUtils.setTag(item, "dungeonClick", "leave");
			item = ItemStackUtils.setTag(item, "dungeonID", id);

			return item;
		}
	},
	DELAY {
		@Override
		public ItemStack getIcon(String id, Player player) {
			Dungeon d = DDataUtils.getDungeon(id);

			ItemStack item = new ItemStack(d.getOption().getGUIIcon());
			item.setDurability(Utils.getColorDurability(DyeColor.BLUE, d.getOption().getGUIIcon()));
			ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

			List<String> lore = Lists.newArrayList();
			lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
			lore.add("");
			lore.add("§3Trạng thái: §fBị trì hoãn");
			lore.add("§3Đếm ngược:  §f" + Utils.format(DQueues.getRemainDelaySeconds(id, player) * 1000));

			ItemStackUtils.setLore(item, lore);

			return item;
		}
	},
	PLAYING {
		@Override
		public ItemStack getIcon(String id, Player player) {
			Dungeon d = DDataUtils.getDungeon(id);

			ItemStack item = new ItemStack(d.getOption().getGUIIcon());
			item.setDurability(Utils.getColorDurability(DyeColor.PINK, d.getOption().getGUIIcon()));
			ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

			List<String> lore = Lists.newArrayList();
			lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
			lore.add("");
			lore.add("§3Trạng thái: §fĐang chiến đấu");
			lore.add("§3Thời gian:  §f" + Utils.formatMinute(DGameUtils.getStatus(id).getAllStatistic().getTimeSurvived() * 1000));

			ItemStackUtils.setLore(item, lore);

			return item;
		}
	};

	public abstract ItemStack getIcon(String id, Player player);

}
