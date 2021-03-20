package me.manaki.plugin.dungeons.guarded;

import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.main.Dungeons;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public class Guardeds {

    public static final long TARGET_COOLDOWN = 2500;

    public static Map<String, Guarded> guardeds = Maps.newHashMap();

    public static void reload(FileConfiguration config) {
        config.getConfigurationSection("guarded").getKeys(false).forEach(id -> {
            String name = config.getString("guarded." + id + ".name");
            int color = config.getInt("guarded." + id + ".color");
            double health = config.getDouble("guarded." + id + ".health");
            Guarded slv = new Guarded(name, color, health);
            guardeds.put(id, slv);
        });
    }

    public static Guarded get(String id) {
        return guardeds.getOrDefault(id, null);
    }

    public static boolean is(Entity e) {
        return e.hasMetadata("Guarded");
    }

    public static void setLastEntityTarget(LivingEntity monster, long cooldown) {
        monster.removeMetadata("lastTarget", Dungeons.get());
        monster.setMetadata("lastTarget", new FixedMetadataValue(Dungeons.get(), System.currentTimeMillis() + cooldown));
    }

    public static boolean canTarget(LivingEntity monster) {
        if (!monster.hasMetadata("lastTarget")) return true;
        return monster.getMetadata("lastTarget").get(monster.getMetadata("lastTarget").size() - 1).asLong() < System.currentTimeMillis();
    }
    
}
