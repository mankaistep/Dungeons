package me.manaki.plugin.dungeons.dungeon.reward;

import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.configable.Configable;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class DReward extends Configable {

    private List<String> rewards;
    private Map<Difficulty, List<String>> rewardWithDifficulties;

    public DReward(FileConfiguration config, String path) {
        super(config, path);
    }

    public List<String> getReward(Difficulty difficulty) {
        if (difficulty == null) return this.rewards;
        return rewardWithDifficulties.getOrDefault(difficulty, null);
    }

    @Override
    public void load(FileConfiguration config, String path) {
        if (config.contains(path + ".EASY")) {
            // Load with difficulties
            this.rewardWithDifficulties = Maps.newHashMap();
            for (Difficulty difficulty : Difficulty.values()) {
                this.rewardWithDifficulties.put(difficulty, config.getStringList(path + "." + difficulty.name()));
            }
            return;
        }

        this.rewards = config.getStringList(path);
    }

    @Override
    public void save(FileConfiguration config, String path) {
        if (this.rewards == null) {
            // Save with difficulties
            for (Difficulty difficulty : Difficulty.values()) {
                config.set(path + "." + difficulty.name(), getReward(difficulty));
            }
            return;
        }

        config.set(path, this.rewards);
    }
}
