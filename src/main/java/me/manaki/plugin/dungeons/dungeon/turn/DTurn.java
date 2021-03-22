package me.manaki.plugin.dungeons.dungeon.turn;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.configable.Configable;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class DTurn extends Configable {

	private String objective;
	private TWinReq wr;
	private TLoseReq lr;
	private TCommand cmd;
	private TSpawn spawn;
	private List<TChest> chests;
	
	public DTurn(FileConfiguration config, String path) {
		super(config, path);
	}
	
	public DTurn(TWinReq wr, TCommand cmd, TSpawn spawn, List<TChest> chests) {
		this.wr = wr;
		this.cmd = cmd;
		this.spawn = spawn;
		this.chests = chests;
	}
	
	public TWinReq getWinRequirement() {
		return this.wr;
	}

	public TLoseReq getLoseRequirement() {
		return this.lr;
	}

	public TCommand getCommand() {
		return this.cmd;
	}
	
	public TSpawn getSpawn() {
		return this.spawn;
	}
	
	public List<TChest> getChests() {
		return this.chests;
	}
	
	public TChest getChest(String lid) {
		for (TChest chest : this.chests) {
			if (chest.getLocation().equals(lid)) return chest;
		}
		return null;
	}

	public String getObjective() {
		return this.objective;
	}

	@Override
	public void load(FileConfiguration config, String path) {
		this.objective = config.contains(path + ".objective") ? config.getString(path + ".objective") : "";

		// Win Req
		int save = config.getInt(path + ".win-req.slave-save");
		List<String> kills = Utils.from(config.getString(path + ".win-req.kill"), ";");
		boolean killAll = config.getBoolean(path + ".win-req.kill-all");
		TWinReq wr = new TWinReq(save, kills, killAll);

		// Lose Req
		String guardedKilled = config.getString(path + ".lose-req.guarded-killed");
		TLoseReq lr = new TLoseReq(guardedKilled);

		// Commands
		List<String> start = config.getStringList(path + ".commands.on-start");
		List<String> lose = config.getStringList(path + ".commands.on-lose");
		List<String> win = config.getStringList(path + ".commands.on-win");
		TCommand cmd = new TCommand(start, lose, win);
		
		// Spawns
		int delay = config.getInt(path + ".spawn.delay");
		List<String> blocks = Utils.from(config.getString(path + ".spawn.block-break"), ";");
		
		// Slaves
		List<TSSlave> slaves = config.getStringList(path + ".spawn.slave").stream().map(s -> new TSSlave(s)).collect(Collectors.toList());
		
		// Mobs
		List<TSMob> mobs = config.getStringList(path + ".spawn.mob").stream().map(s -> new TSMob(s)).collect(Collectors.toList());

		// Guarded
		TSGuarded guarded = new TSGuarded(config.getString(path + ".spawn.guarded"));

		TSpawn spawn = new TSpawn(delay, guarded, blocks, mobs, slaves);
		
		List<TChest> chests = Lists.newArrayList();
		if (config.contains(path + ".chest")) {
			config.getConfigurationSection(path + ".chest").getKeys(false).forEach(l -> {
				double chance = config.getDouble(path + ".chest." + l + ".chance");
				List<String> cmds = config.getStringList(path + ".chest." + l + ".commands");
				Rank rank = Rank.valueOf(config.getString(path + ".chest." + l + ".rank"));
				chests.add(new TChest(l, rank, chance, cmds));
			});
		}
		
		// Set
		this.cmd = cmd;
		this.spawn = spawn;
		this.wr = wr;
		this.lr = lr;
		this.chests = chests;
	}

	@Override
	public void save(FileConfiguration config, String path) {
		config.set(path + ".objective", this.getObjective());

		// Win Req
		config.set(path + ".win-req.slave-save", this.getWinRequirement().getSlaveSave());
		config.set(path + ".win-req.kill", this.getWinRequirement().killsToString());
		config.set(path + ".win-req.kill-all", this.getWinRequirement().isKillAll());

		// Lose Req
		config.set(path + ".lose-req.guarded-killed", this.getLoseRequirement().getGuardedKilled());

		// Commands
		config.set(path + ".commands.on-start", this.getCommand().getStarts());
		config.set(path + ".commands.on-lose", this.getCommand().getLoses());
		config.set(path + ".commands.on-win", this.getCommand().getWins());
		
		// Spawns
		config.set(path + ".spawn.delay", this.getSpawn().getDelay());
		config.set(path + ".spawn.block-break", this.getSpawn().blocksToString());
		config.set(path + ".spawn.guarded", this.getSpawn().getGuarded().toString());
		config.set(path + ".spawn.slave", this.getSpawn().slavesToStringList());
		config.set(path + ".spawn.mob", this.getSpawn().mobsToStringList());
		
		// Chests
		this.chests.forEach(chest -> {
			config.set(path + ".chest." + chest.getLocation() + ".chance", chest.getChance());
			config.set(path + ".chest." + chest.getLocation() + ".commands", chest.getCommands());
			config.set(path + ".chest." + chest.getLocation() + ".rank", chest.getRank().name());
		});
	}
	
}
