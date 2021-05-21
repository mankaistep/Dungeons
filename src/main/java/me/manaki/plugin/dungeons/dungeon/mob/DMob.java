package me.manaki.plugin.dungeons.dungeon.mob;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.configable.Configable;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class DMob extends Configable {

    private Map<Difficulty, String> mobWithDifficulties;

    public DMob(FileConfiguration config, String path) {
        super(config, path);
    }

    public List<String> getMobs() {
        if (this.mobWithDifficulties != null) Lists.newArrayList(mobWithDifficulties.values());
        return Lists.newArrayList();
    }

    public String getMob(String id, Difficulty difficulty) {
        if (difficulty == null) return id;
        return mobWithDifficulties.getOrDefault(difficulty, null);
    }

    @Override
    public void load(FileConfiguration config, String path) {
        var s = config.getString(path);
        this.mobWithDifficulties = Maps.newHashMap();
        mobWithDifficulties.put(Difficulty.EASY, s.split(";")[0]);
        mobWithDifficulties.put(Difficulty.NORMAL, s.split(";")[1]);
        mobWithDifficulties.put(Difficulty.HARD, s.split(";")[2]);
        mobWithDifficulties.put(Difficulty.INSANE, s.split(";")[3]);
    }

    @Override
    public void save(FileConfiguration config, String path) {
        var s = getMob(null, Difficulty.EASY) + ";" + getMob(null, Difficulty.NORMAL) + ";"+ getMob(null, Difficulty.HARD) + ";"+ getMob(null, Difficulty.INSANE);
        config.set(path, s);
    }
}
