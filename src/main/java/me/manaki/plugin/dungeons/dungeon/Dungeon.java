package me.manaki.plugin.dungeons.dungeon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.manaki.plugin.dungeons.configable.Configable;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.dungeon.drop.DDrop;
import me.manaki.plugin.dungeons.dungeon.info.DInfo;
import me.manaki.plugin.dungeons.dungeon.location.DLocation;
import me.manaki.plugin.dungeons.dungeon.mob.DMob;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyDrop;
import me.manaki.plugin.dungeons.dungeon.option.DOption;
import me.manaki.plugin.dungeons.dungeon.reward.DReward;
import me.manaki.plugin.dungeons.dungeon.rule.DRule;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.block.DBlock;
import me.manaki.plugin.dungeons.dungeon.rewardreq.DRewardReq;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dungeon extends Configable {

	private String id;

	private DInfo info;
	private DOption option;
	private DRule rule;
	private Map<String, DLocation> locs;
	private Map<String, DBlock> blocks;
	private Map<String, DMob> mobs;
	private List<DMoneyDrop> moneyDrops;
	private List<DDrop> drops;
	private DRewardReq rewardReq;
	private DReward reward;
	private List<DTurn> turns;
	private List<String> checkPoints;
	
	public Dungeon(String id, DInfo info, DOption option, DRule rule, DReward reward, Map<String, DLocation> locs, Map<String, DBlock> blocks, Map<String, DMob> mobs, List<DMoneyDrop> moneyDrops, List<DDrop> drops, DRewardReq rewardReq, List<DTurn> turns, List<String> checkPoints) {
		this.id = id;
		this.info = info;
		this.option = option;
		this.rule = rule;
		this.locs = locs;
		this.blocks = blocks;
		this.mobs = mobs;
		this.moneyDrops = moneyDrops;
		this.drops = drops;
		this.rewardReq = rewardReq;
		this.turns = turns;
		this.checkPoints = checkPoints;
		this.reward = reward;
	}
	
	public Dungeon(String id, FileConfiguration config, String path) {
		super(config, path);
		this.id = id;
	}

	public String getID() {
		return this.id;
	}

	public DInfo getInfo() {
		return this.info;
	}
	
	public DOption getOption() {
		return this.option;
	}
	
	public DRule getRule() {
		return this.rule;
	}
	
	public Map<String, DLocation> getLocations() {
		return this.locs;
	}
	
	public DLocation getLocation(String id) {
		return this.locs.getOrDefault(id, null);
	}
	
	public Map<String, DBlock> getBlocks() {
		return this.blocks;
	}
	
	public DBlock getBlock(String id) {
		return this.blocks.getOrDefault(id, null);
	}
	
	public List<DMoneyDrop> getMoneyDrops() {
		return this.moneyDrops;
	}
	
	public List<DDrop> getDrops() {
		return this.drops;
	}
	
	public DRewardReq getRewardReq() {
		return this.rewardReq;
	}
	
	public List<DTurn> getTurns() {
		return this.turns;
	}

	public DTurn getTurn(int turnID) {
		return this.turns.get(turnID - 1);
	}

	public List<String> getCheckPoints() {
		return this.checkPoints;
	}

	public Map<String, DMob> getMobs() {
		return mobs;
	}

	public String getMob(String id, Difficulty difficulty) {
		if (this.option.hasDifficulty()) {
			if (!this.mobs.containsKey(id)) return id;
			return this.mobs.get(id).getMob(id, difficulty);
		}
		return id;
	}

	public List<String> getMobs(String id) {
		if (this.option.hasDifficulty()) {
			if (!this.mobs.containsKey(id)) return Lists.newArrayList(id);
			return this.mobs.get(id).getMobs();
		}
		return Lists.newArrayList(id);
	}

	public DReward getReward() {
		return this.reward;
	}
	
	public void setLocation(String id, Location l, int r) {
		this.locs.put(id, new DLocation(r, l));
	}
	
	@SuppressWarnings("deprecation")
	public void setBlock(String id, Block b) {
		this.blocks.put(id, new DBlock(b.getType(), b.getData(), b.getLocation()));
	}
	
	@Override
	public void load(FileConfiguration config, String path) {
		this.info = new DInfo(config, path + ".info");
		this.option = new DOption(config, path + ".option");
		this.rule = new DRule(config, path + ".rule");
		this.rewardReq = new DRewardReq(config, path + ".reward-req");

		this.reward = new DReward(config, path + ".reward");

		this.locs = Maps.newHashMap();
		if (config.contains(path + ".location")) {
			config.getConfigurationSection(path + ".location").getKeys(false).forEach(id -> {
				this.locs.put(id, new DLocation(config, path + ".location." + id));
			});
		}

		this.blocks = Maps.newHashMap();
		if (config.contains(path + ".block")) {
			config.getConfigurationSection(path + ".block").getKeys(false).forEach(id -> {
				this.blocks.put(id, new DBlock(config, path + ".block." + id));
			});
		}

		this.mobs = Maps.newHashMap();
		if (config.contains(path + ".mob")) {
			config.getConfigurationSection(path + ".mob").getKeys(false).forEach(id -> {
				this.mobs.put(id, new DMob(config, path + ".mob." + id));
			});
		}
		
		this.moneyDrops = config.getStringList(path + ".money-drop").stream().map(s -> new DMoneyDrop(s)).collect(Collectors.toList());
		
		this.drops = Lists.newArrayList();
		config.getStringList(path + ".drop").forEach(s -> {
			drops.add(new DDrop(s));
		});

		this.turns = Lists.newArrayList();
		int i = 1;
		while (config.contains(path + ".turn.t" + i) || config.contains(path + ".turn." + i)) {
			var turnpath = config.contains(path + ".turn.t" + i) ? path + ".turn.t" + i : path + ".turn." + i;
			this.turns.add(new DTurn(config, turnpath));
			i++;
		}

		this.checkPoints = config.getStringList(path + ".check-points");
	}

	@Override
	public void save(FileConfiguration config, String path) {
		this.info.save(config, path + ".info");
		this.option.save(config, path + ".option");
		this.rule.save(config, path + ".rule");
		this.rewardReq.save(config, path + ".reward-req");
		this.reward.save(config, path + ".reward");
		
		this.locs.forEach((id, l) -> {
			l.save(config, path + ".location." + id);
		});
		this.blocks.forEach((id, b) -> {
			b.save(config, path + ".block." + id);
		});
		this.mobs.forEach((id, m) -> {
			m.save(config, path + ".mob." + id);
		});
		
		config.set(path + ".money-drop", this.moneyDrops.stream().map(s -> s.toString()).collect(Collectors.toList()));
		
		List<String> dl = Lists.newArrayList();
		this.drops.forEach(drop -> {
			dl.add(drop.toString());
		});
		config.set(path + ".drop", dl);

		config.set(path + ".check-points", this.checkPoints);

		for (int i = 0 ; i < turns.size() ; i++) {
			turns.get(i).save(config, path + ".turn." + (i + 1));
		}

	}

	public static Dungeon get(String id) {
		return DDataUtils.getDungeon(id);
	}

}
