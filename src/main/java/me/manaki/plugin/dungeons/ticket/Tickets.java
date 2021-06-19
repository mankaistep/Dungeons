package me.manaki.plugin.dungeons.ticket;

import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.util.ItemBuilder;
import me.manaki.plugin.dungeons.util.ItemStackUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class Tickets {

	private static ItemStack PATTERN;

	public static void init(FileConfiguration config) {
		PATTERN = ItemBuilder.buildItem(config.getConfigurationSection("ticket.item"));
	}

	public static ItemStack getTicket(String id) {
		ItemStack is = PATTERN.clone();
		ItemMeta meta = is.getItemMeta();
		String name = null;
		if (!id.equalsIgnoreCase("*")) {
			Dungeon d = DDataUtils.getDungeon(id);
			name = d.getInfo().getName();
			meta.setDisplayName(meta.getDisplayName().replace("%dungeon_name%", name));
		}

		is.setItemMeta(meta);

		return ItemStackUtils.setTag(is, "dungeon3.ticket", id);
	}

	public static boolean isTicket(ItemStack is, String id) {
		if (!ItemStackUtils.hasTag(is, "dungeon3.ticket")) return false;
		String tid = getDungeon(is);
		return tid.equalsIgnoreCase("*") || id.equalsIgnoreCase(tid);
	}

	private static String getDungeon(ItemStack ticket) {
		return ItemStackUtils.getTag(ticket, "dungeon3.ticket");
	}

	public static boolean takeOne(Player p, String dungeonID) {
		if (!has(p, dungeonID)) return false;
		PlayerInventory inv = p.getInventory();

		int count = 1;

		for (int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack i = inv.getItem(slot);
			if (count <= 0)
				break;
			if (i == null)
				continue;
			if (isTicket(i, dungeonID)) {
				if (i.getAmount() > count) {
					i.setAmount(i.getAmount() - count);
					count = 0;
				} else if (i.getAmount() <= count) {
					count -= i.getAmount();
					inv.setItem(slot, null);
				}
			}
		}

		return true;
	}

	public static int count(Player player, String id) {
		int count = 0;
		PlayerInventory inv = player.getInventory();

		for (ItemStack i : inv.getContents()) {
			if (i == null)
				continue;
			if (isTicket(i, id))
				count += i.getAmount();
		}
		return count;
	}

	public static boolean has(Player player, String id) {
		int count = count(player, id);
		return count >= 1;
	}



}