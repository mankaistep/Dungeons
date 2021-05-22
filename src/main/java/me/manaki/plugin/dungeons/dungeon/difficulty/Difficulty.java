package me.manaki.plugin.dungeons.dungeon.difficulty;

import me.manaki.plugin.dungeons.util.ItemStackManager;
import me.manaki.plugin.dungeons.util.MinMax;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public enum Difficulty {

    EASY("Dễ", new MinMax(5, 25)) {
        @Override
        public ItemStack getIcon() {
            var is = new ItemStack(Material.LIGHT_GRAY_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§f§lDễ");
            ism.setLore(List.of("§7§oYêu cầu cấp độ §f§o" + this.getLevelRequired().getMin() + " » " + this.getLevelRequired().getMax()));

            return is;
        }
    },
    NORMAL("Thường", new MinMax(25, 50)) {
        @Override
        public ItemStack getIcon() {
            var is = new ItemStack(Material.YELLOW_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§e§lThường");
            ism.setLore(List.of("§7§oYêu cầu cấp độ §e§o" + this.getLevelRequired().getMin() + " » " + this.getLevelRequired().getMax()));

            return is;
        }
    },
    HARD("Khó", new MinMax(45, 75)) {
        @Override
        public ItemStack getIcon() {
            var is = new ItemStack(Material.PURPLE_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§d§lKhó");
            ism.setLore(List.of("§7§oYêu cầu cấp độ §d§o" + this.getLevelRequired().getMin() + " » " + this.getLevelRequired().getMax()));

            return is;
        }
    },
    INSANE("Khủng khiếp", new MinMax(65, 100)) {
        @Override
        public ItemStack getIcon() {
            var is = new ItemStack(Material.RED_BANNER);
            var ism = new ItemStackManager(is);
            ism.setName("§c§lKhủng khiếp");
            ism.setLore(List.of("§7§oYêu cầu cấp độ §c§o" + this.getLevelRequired().getMin() + " » " + this.getLevelRequired().getMax()));

            return is;
        }
    };

    private final String name;
    private final MinMax levelRequired;

    Difficulty(String name, MinMax levelRequired) {
        this.name = name;
        this.levelRequired = levelRequired;
    }

    public abstract ItemStack getIcon();

    public String getName() {
        return name;
    }

    public MinMax getLevelRequired() {
        return levelRequired;
    }
}
