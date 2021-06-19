package me.manaki.plugin.dungeons;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.buff.Buff;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.guarded.Guardeds;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.listener.DungeonListener;
import me.manaki.plugin.dungeons.listener.EntityListener;
import me.manaki.plugin.dungeons.listener.GUIListener;
import me.manaki.plugin.dungeons.listener.PlayerListener;
import me.manaki.plugin.dungeons.placeholder.DungeonPlaceholder;
import me.manaki.plugin.dungeons.plugincommand.AdminPluginCommand;
import me.manaki.plugin.dungeons.plugincommand.PlayerPluginCommand;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.room.RoomManager;
import me.manaki.plugin.dungeons.room.gui.DungeonSelectGUI;
import me.manaki.plugin.dungeons.slave.Slaves;
import me.manaki.plugin.dungeons.task.DMoneyCoinTask;
import me.manaki.plugin.dungeons.task.RoomTask;
import me.manaki.plugin.dungeons.ticket.Tickets;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.v4.config.V4Config;
import me.manaki.plugin.dungeons.v4.dungeon.manager.DungeonManager;
import me.manaki.plugin.dungeons.v4.world.WorldListener;
import me.manaki.plugin.dungeons.v4.world.WorldLoader;
import me.manaki.plugin.dungeons.v4.world.WorldManager;
import me.manaki.plugin.dungeons.v4.world.WorldTask;
import me.manaki.plugin.dungeons.yaml.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Dungeons extends JavaPlugin {
	
	public boolean loaded = false;
	public String featherBoard = null;

	private V4Config v4Config;
	private WorldLoader worldLoader;
	private WorldManager worldManager;
	private DungeonManager dungeonManager;
	private RoomManager roomManager;

	@Override
	public void onEnable() {
		this.registerCommands();
		this.registerListeners();
		this.hookPlugins();
		this.runTasks();

		// V4
		this.v4Config = new V4Config(this);
		this.worldLoader = new WorldLoader(this);
		this.worldManager = new WorldManager(this);
		this.dungeonManager = new DungeonManager(this);
		this.roomManager = new RoomManager(this);

		// WorldGuard
		this.v4Config.reload();
		Utils.clearWorldGuardTemporaryData();

		if (Bukkit.getOnlinePlayers().size() > 0) this.reloadConfig();
	}
	
	@Override
	public void onDisable() {
		try {
			// Quit dungeons
			for (DStatus status : this.getDungeonManager().getStatuses()) {
				status.stopAllSounds();
				for (UUID uuid : Lists.newArrayList(status.getPlayers())) {
					var player = Bukkit.getPlayer(uuid);
					this.getDungeonManager().kick(player, true);
					assert player != null;
					player.sendMessage("§cHệ thống phó bản tái khởi động!");
				}
			}
		}
		finally {
			// Remove all temporary worlds
			this.getWorldLoader().unloadAllTemporaryWorlds(false);
		}
	}
	
	@Override
	public void reloadConfig() {
		this.saveDefaultConfig();
		YamlFile.reloadAll(this);
		Lang.init(this, YamlFile.MESSAGE);
		DDataUtils.loadAll(YamlFile.CONFIG.get());
		DDataUtils.saveAll();
		Rank.loadAll(YamlFile.CONFIG.get());
		Buff.init(YamlFile.CONFIG.get());
		Tickets.init(YamlFile.CONFIG.get());
		Slaves.reload(YamlFile.CONFIG.get());
		Guardeds.reload(YamlFile.CONFIG.get());
		DungeonSelectGUI.load(YamlFile.CONFIG.get());
		this.v4Config.reload();

		// Featherboard
		if (Bukkit.getPluginManager().isPluginEnabled("FeatherBoard")) {
			if (YamlFile.CONFIG.get().getBoolean("featherboard.enable")) {
				this.featherBoard = YamlFile.CONFIG.get().getString("featherboard.board");
			} else this.featherBoard = null;
		} else this.featherBoard = null;

		this.registerTasks();
	}
	
	public void registerCommands() {
		this.getCommand("dungeon").setExecutor(new PlayerPluginCommand(this));
		this.getCommand("dungeons").setExecutor(new AdminPluginCommand(this));
	}
	
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
		Bukkit.getPluginManager().registerEvents(new DungeonListener(this), this);
		Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
	}
	
	public void registerTasks() {
		RoomTask.start(this);
		WorldTask.start(this);
	}
	
	public void hookPlugins() {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new DungeonPlaceholder().register();
		}
	}
	
	public void runTasks() {
		DMoneyCoinTask.start();
	}

	public V4Config getV4Config() {
		return v4Config;
	}

	public WorldLoader getWorldLoader() {
		return worldLoader;
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

	public DungeonManager getDungeonManager() {
		return dungeonManager;
	}

	public RoomManager getRoomManager() {
		return roomManager;
	}

	public static Dungeons getPlugin() {
		return (Dungeons) Bukkit.getPluginManager().getPlugin("Dungeons");
	}

	public static Dungeons get() {
		return (Dungeons) Bukkit.getPluginManager().getPlugin("Dungeons");
	} 
	
}
