package me.manaki.plugin.dungeons.room.gui;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.util.ItemBuilder;
import me.manaki.plugin.dungeons.util.ItemStackUtils;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DungeonSelectGUI {

    public static final String TITLE = "§0§lCHỌN PHÓ BẢN";

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

    public static void open(Player player) {
        int size = guiSize;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);

        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.get(), () -> {
            for (Integer i : backgroundSlots) {
                inv.setItem(i, checkPlaceholderAPI(player, background.clone()));
            }
            inv.setItem(infoSlot, checkPlaceholderAPI(player, info.clone()));
            inv.setItem(tutorialSlot, checkPlaceholderAPI(player, tutorial.clone()));
            inv.setItem(quitSlot, checkPlaceholderAPI(player, quit.clone()));

            DDataUtils.getDungeons().forEach((id, dungeon) -> {
                inv.setItem(dungeon.getOption().getGUISlot(), getIcon(id));
            });

        });
    }

    public static void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equalsIgnoreCase(TITLE)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getWhoClicked().getOpenInventory().getTopInventory()) return;

        Player player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
        int slot = e.getSlot();

        if (slot == quitSlot) {
            player.closeInventory();
            return;
        }

        for (Map.Entry<String, Dungeon> entry : DDataUtils.getDungeons().entrySet()) {
            var id = entry.getKey();
            var dungeon = entry.getValue();
            if (dungeon.getOption().getGUISlot() == slot) {
                // Test requirement
                if (dungeon.getOption().getLevel().test(player.getLevel())) {
                    RoomSelectGUI.open(player, id);
                }
                else player.sendMessage("§cCấp độ không đạt yêu cầu!");
            }
        }
    }

    public static ItemStack getIcon(String id) {
        Dungeon d = DDataUtils.getDungeon(id);

        ItemStack item = new ItemStack(d.getInfo().getIcon().toItemStack());
        ItemStackUtils.setDisplayName(item, "§3§lPhó bản §b§l" + d.getInfo().getName());

        List<String> lore = Lists.newArrayList();
        lore.addAll(Utils.toList(d.getInfo().getDesc(), 25, "§7§o"));
        lore.add("");
        if (!d.getOption().hasDifficulty()) lore.add("§3Cấp độ: §f" + d.getOption().getLevel().getMin() + " » " + d.getOption().getLevel().getMax());
        lore.add("§3Số lượng: §f" + d.getOption().getPlayer().getMin() + " » " + d.getOption().getPlayer().getMax());
        if (d.getOption().isTicketRequired()) {
            lore.add("§3Tiêu thụ vé: §fx1 Vé");
        }
        else lore.add("§3Tiêu thụ vé: §fKhông");

        ItemStackUtils.setLore(item, lore);

        return item;
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
