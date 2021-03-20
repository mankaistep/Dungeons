package me.manaki.plugin.dungeons.main;

import jdk.dynalink.linker.support.Guards;
import me.manaki.plugin.dungeons.buff.Buff;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.guarded.Guardeds;
import me.manaki.plugin.dungeons.listener.EntityListener;
import me.manaki.plugin.dungeons.queue.DQueueTask;
import me.manaki.plugin.dungeons.yaml.YamlFile;
import me.manaki.plugin.dungeons.dungeon.manager.DGameEnds;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.lang.Lang;
import me.manaki.plugin.dungeons.listener.DungeonListener;
import me.manaki.plugin.dungeons.listener.GUIListener;
import me.manaki.plugin.dungeons.listener.PlayerListener;
import me.manaki.plugin.dungeons.main.command.AdminPluginCommand;
import me.manaki.plugin.dungeons.main.command.PlayerPluginCommand;
import me.manaki.plugin.dungeons.queue.DQueues;
import me.manaki.plugin.dungeons.rank.Rank;
import me.manaki.plugin.dungeons.slave.Slaves;
import me.manaki.plugin.dungeons.task.DMoneyCoinTask;
import me.manaki.plugin.dungeons.ticket.Tickets;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Dungeons extends JavaPlugin {
	
	public boolean loaded = false;
	
	@Override
	public void onEnable() {
		this.registerCommands();
		this.registerListeners();
		this.hookPlugins();
		this.runTasks();
		if (Bukkit.getOnlinePlayers().size() > 0) this.reloadConfig();
	}
	
	@Override
	public void onDisable() {
		DDataUtils.getDungeons().keySet().forEach(id -> {
			if (DGameUtils.isPlaying(id)) DGameEnds.loseDungeon(id);
		});
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
		this.createQueues();
		this.registerTasks();
	}
	
	public void registerCommands() {
		this.getCommand("dungeon").setExecutor(new PlayerPluginCommand());
		this.getCommand("dungeons").setExecutor(new AdminPluginCommand());
	}
	
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
		Bukkit.getPluginManager().registerEvents(new DungeonListener(), this);
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
	}
	
	public void registerTasks() {
		new DQueueTask().runTaskTimerAsynchronously(this, 0, 100);
	}
	
	public void createQueues() {
		DDataUtils.getDungeons().forEach((id, dungeon) -> {
			if (!DGameUtils.isPlaying(id) && !DQueues.hasQueue(id)) {
				DQueues.newQueue(id);
			}
		});
	}
	
	public void hookPlugins() {
		
	}
	
	public void runTasks() {
		DMoneyCoinTask.start();
	}
	
	public static Dungeons getPlugin() {
		return (Dungeons) Bukkit.getPluginManager().getPlugin("Dungeons");
	}

	public static Dungeons get() {
		return (Dungeons) Bukkit.getPluginManager().getPlugin("Dungeons");
	} 
	
}
