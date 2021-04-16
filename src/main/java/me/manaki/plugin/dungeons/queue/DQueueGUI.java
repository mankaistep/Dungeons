package me.manaki.plugin.dungeons.queue;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.ticket.Tickets;
import me.manaki.plugin.dungeons.util.ItemBuilder;
import me.manaki.plugin.dungeons.util.ItemStackUtils;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DQueueGUI {

	public static final String TITLE = "§0§lSORASKY | PHÓ BẢN - DUNGEON";

	private static int guiSize;
	private static ItemStack background;
	private static List<Integer> backgroundSlots;
	private static ItemStack tutorial;
	private static int tutorialSlot;
	private static ItemStack info;
	private static int infoSlot;
	private static ItemStack quit;
	private static int quitSlot;

	public static void load(FileConfiguration config) {
		guiSize = config.getInt("gui.size");

		background = ItemBuilder.buildItem(Objects.requireNonNull(config.getConfigurationSection("gui.background.item")));
		backgroundSlots = config.getStringList("gui.background.slots").stream().map(Integer::parseInt).collect(Collectors.toList());

		tutorial = ItemBuilder.buildItem(Objects.requireNonNull(config.getConfigurationSection("gui.tutorial.item")));
		tutorialSlot = config.getInt("gui.tutorial.slot");

		info = ItemBuilder.buildItem(Objects.requireNonNull(config.getConfigurationSection("gui.info.item")));
		infoSlot = config.getInt("gui.info.slot");

		quit = ItemBuilder.buildItem(Objects.requireNonNull(config.getConfigurationSection("gui.quit.item")));
		quitSlot = config.getInt("gui.quit.slot");
	}

	public static void openGUI(Player player) {
		int amount = 0;
		for (Dungeon d : DDataUtils.getDungeons().values()) {
			if (d.getOption().getGUISlot() > amount) amount = d.getOption().getGUISlot();
		}
		amount++;
		
		int size = guiSize;
		Inventory inv = Bukkit.createInventory(null, size, TITLE);
		player.openInventory(inv);
		
		Bukkit.getScheduler().runTaskAsynchronously(Dungeons.get(), () -> {
			for (Integer i : backgroundSlots) {
				inv.setItem(i, checkPlaceholderAPI(player, background.clone()));
			}
			inv.setItem(infoSlot, checkPlaceholderAPI(player, info.clone()));
			inv.setItem(tutorialSlot, checkPlaceholderAPI(player, tutorial.clone()));
			inv.setItem(quitSlot, checkPlaceholderAPI(player, quit.clone()));

		});
		
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.getOpenInventory().getTitle().equals(TITLE)) {
					this.cancel();
					return;
				}
				DDataUtils.getDungeons().forEach((id, dungeon) -> {
					DQueueStatus sts = DQueues.getStatus(id, player);
					inv.setItem(dungeon.getOption().getGUISlot(), sts.getIcon(id, player));
				});
			}
		};
		br.runTaskTimerAsynchronously(Dungeons.get(), 0, 10);
	}
	
	public static void onClick(InventoryClickEvent e) {
		if (!e.getView().getTitle().equalsIgnoreCase(TITLE)) return;
		e.setCancelled(true);
		if (e.getClickedInventory() != e.getWhoClicked().getOpenInventory().getTopInventory()) return;
		
		Player player = (Player) e.getWhoClicked();
		int slot = e.getSlot();

		if (slot == quitSlot) {
			player.closeInventory();
			return;
		}

		ItemStack item = e.getInventory().getItem(slot);
		if (ItemStackUtils.hasTag(item, "dungeonClick")) {
			String id = ItemStackUtils.getTag(item, "dungeonID");
			if (ItemStackUtils.getTag(item, "dungeonClick").equals("join")) {
				if (DPlayerUtils.isInDungeon(player)) {
					player.sendMessage("§cHoàn thành hoặc thoát dungeon hiện tại để vào hàng chờ");
					return;
				}
				if (DQueues.isInQueue(player) && !DQueues.isInQueue(id, player)) {
					player.sendMessage("§cRời hàng chờ rồi chọn");
					return;
				}
				
				// Check ticket
				Dungeon d = DDataUtils.getDungeon(id);
				if (d.getOption().isTicketRequired()) {
					if (!Tickets.takeOne(player)) {
						player.sendMessage("§cBạn cần có vẻ để vào hàng chờ của " + d.getInfo().getName());
						return;
					}
				} else player.sendMessage("§aĐã lấy 1 vé phó bản từ kho đồ của bạn");
				
				DQueues.add(id, player);
				player.sendMessage("§aĐã tham gia phòng chờ");
				return;
			}
			else if (ItemStackUtils.getTag(item, "dungeonClick").equals("leave")) {
				DQueues.remove(id, player);
				player.sendMessage("§aĐã rời phòng chờ");
				return;
			}
		}
	}
	
	public static ItemStack getTut() {
		ItemStack is = new ItemStack(Material.BOOK, 1);
		ItemStackUtils.setDisplayName(is, "§a§lPhó bản (Dungeon) là gì ?");
		List<String> lore = Lists.newArrayList();
		lore.add("");
		lore.add("§f§oLà nơi bạn chiến đấu với quái vật, ");
		lore.add("§f§otừ đó nhận được §a§oKinh nghiệm, Tiền,");
		lore.add("§a§oNguyên liệu chế tác, Đá cường hóa, ");
		lore.add("§a§oBùa may mắn, Đá nâng bậc,...");
		lore.add("");
		lore.add("§f§oĐể có thể tham gia phó bản, bạn phải ");
		lore.add("§f§ođợi người tham gia trước kết thúc ");
		lore.add("§f§orồi bạn mới có thể vào");
		ItemStackUtils.setLore(is, lore);
		
		return is;
	}
	
	public static ItemStack getBlackSlot() {
		ItemStack other = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		other.setDurability(Utils.getColorDurability(DyeColor.BLACK, Material.BLACK_STAINED_GLASS_PANE));
		ItemMeta meta = other.getItemMeta();
		meta.setDisplayName(" ");
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		other.setItemMeta(meta);
		return other;
	}

	public static ItemStack checkPlaceholderAPI(Player player, ItemStack is) {
		if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return is;
		var meta = is.getItemMeta();
		if (meta.hasDisplayName()) {
			meta.setDisplayName(PlaceholderAPI.setPlaceholders(player, meta.getDisplayName()));
		}
		if (meta.hasLore()) {
			var lore = meta.getLore();
			for (int i = 0; i < lore.size(); i++) {
				lore.set(i, PlaceholderAPI.setPlaceholders(player, lore.get(i)));
			}
			meta.setLore(lore);
		}
		is.setItemMeta(meta);
		return is;
	}
	
	public static List<Integer> getEmptySlot() {
		return Lists.newArrayList(29, 30, 31, 32, 33, 38, 39, 40, 41, 42).stream().map(i -> i - 9).collect(Collectors.toList());
	}
	
}
