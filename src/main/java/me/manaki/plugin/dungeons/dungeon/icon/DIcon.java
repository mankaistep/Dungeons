package me.manaki.plugin.dungeons.dungeon.icon;

import me.manaki.plugin.dungeons.configable.Configable;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class DIcon extends Configable {

    private Material m;
    private int model;

    public DIcon(FileConfiguration config, String path) {
        super(config, path);
    }

    public Material getMaterial() {
        return m;
    }

    public int getModel() {
        return model;
    }

    public ItemStack toItemStack() {
        var is = new ItemStack(m);
        var meta = is.getItemMeta();
        meta.setCustomModelData(this.model);
        is.setItemMeta(meta);
        return is;
    }

    @Override
    public void load(FileConfiguration config, String path) {
        this.m = Material.valueOf(config.getString(path).split(";")[0]);
        this.model = Integer.parseInt(config.getString(path).split(";")[1]);
    }

    @Override
    public void save(FileConfiguration config, String path) {
        config.set(path, this.m.name() + ";" + this.model);
    }
}
