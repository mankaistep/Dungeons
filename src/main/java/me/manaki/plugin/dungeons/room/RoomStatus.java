package me.manaki.plugin.dungeons.room;

import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.util.ItemStackManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public enum RoomStatus {

    CAN_JOIN {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.LIME_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§a§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§a§o§lCó thể vào phòng");
            lore.add("§a§oClick để vào");

            ism.setLore(lore);

            return is;
        }
    },

    NOT_READY {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.GREEN_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§2§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());
            for (Player p : room.getPlayers()) {
                lore.add("§6 » §e" + p.getName());
            }

            lore.add("");
            lore.add("§e§o§lĐang đợi thêm người...");
            lore.add("§e§oClick để thoát phòng");

            ism.setLore(lore);

            return is;
        }
    },

    READY {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.LIME_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§a§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());
            for (Player p : room.getPlayers()) {
                lore.add("§6 » §e" + p.getName());
            }

            lore.add("");
            lore.add("§a§oĐếm ngược: §a§o§l" + room.getCountdown() + "s");
            lore.add("§a§oClick để thoát phòng");

            ism.setLore(lore);

            return is;
        }
    },

    ALREADY_IN_A_ROOM {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.ORANGE_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§6§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§c§oBạn đang ở trong một phòng khác rồi");

            ism.setLore(lore);

            return is;
        }
    },

    FULL {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.CYAN_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§3§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§3§oHết chỗ trống");

            ism.setLore(lore);

            return is;
        }
    },

    NO_TICKET {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.MAGENTA_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§d§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§c§oYêu cầu x1 Vé để vào");

            ism.setLore(lore);

            return is;
        }
    },

    NOT_ENOUGH_LEVEL {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.MAGENTA_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§d§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§c§oYêu cầu cấp " + room.getDifficulty().getLevelRequired().getMin() + " » " + room.getDifficulty().getLevelRequired().getMax());

            ism.setLore(lore);

            return is;
        }
    },

    EMPTY {
        @Override
        public ItemStack getIcon(Player player, Room room) {
            var d = DDataUtils.getDungeon(room.getDungeonID());
            var is = new ItemStack(Material.RED_CONCRETE);
            var ism = new ItemStackManager(is);
            ism.setName("§c§lPhòng #" + room.getID());
            var lore = new ArrayList<String>();
            lore.add("");
            lore.add("§aPhó bản: §f" + d.getInfo().getName());

            var dfc = d.getOption().hasDifficulty() ? room.getDifficulty() : null;
            var dfcname = d.getOption().hasDifficulty() ? room.getDifficulty().getName() : "Không có";
            lore.add("§aĐộ khó: §f" + dfcname);

            lore.add("");
            lore.add("§6Người chơi: §f" + room.getPlayers().size() + "/" + d.getOption().getPlayer().getMax());

            lore.add("");
            lore.add("§c§oPhòng trống, xóa phòng sau §c§o§l" + room.getRemoveCountdown() + "s");

            ism.setLore(lore);

            return is;
        }
    };

    public abstract ItemStack getIcon(Player player, Room room);

}
