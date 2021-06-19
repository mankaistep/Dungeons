package me.manaki.plugin.dungeons.plugincommand;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.difficulty.Difficulty;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyCoin;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.turn.TSMob;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.ticket.Tickets;
import me.manaki.plugin.dungeons.util.Tasks;
import me.manaki.plugin.dungeons.util.Utils;
import me.manaki.plugin.dungeons.v4.world.WorldLoader;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class AdminPluginCommand implements CommandExecutor {

	private Dungeons plugin;

	public AdminPluginCommand(Dungeons plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String var3, String[] args) {
		
		if (args.length == 0) {
			sendTut(sender);
			return false;
		}
		
		try {
			if (args[0].equalsIgnoreCase("reload")) {
				Dungeons.get().reloadConfig();
				sender.sendMessage(ChatColor.GREEN + "Config reloaded");
			}
			
			else if (args[0].equalsIgnoreCase("start")) {
				Player player = (Player) sender;
				String id = args[1];
				Difficulty dct = null;
				List<UUID> list = Lists.newArrayList(player.getUniqueId());
				if (args.length > 2) {
					dct = Difficulty.valueOf(args[2].toUpperCase());
				}
				if (DDataUtils.checkProblem(id)) {
					plugin.getDungeonManager().start(id, dct, list);
				}
			}
			
			else if (args[0].equalsIgnoreCase("setlocation")) {
				Player player = (Player) sender;
				String dungeon = args[1];
				int radius = Integer.parseInt(args[2]);
				String id = args[3];
				
				Dungeon d = DDataUtils.getDungeon(dungeon);
				d.setLocation(id, player.getLocation(), radius);
				DDataUtils.saveAll();
				
				sender.sendMessage(ChatColor.GREEN + "Set place you are standing id " + id);
			}
			
			else if (args[0].equalsIgnoreCase("setblock")) {
				Player player = (Player) sender;
				String dungeon = args[1];
				Block b = player.getTargetBlock(Sets.newHashSet(Material.AIR), 10);
				String id = args[2];
				
				Dungeon d = DDataUtils.getDungeon(dungeon);
				d.setBlock(id, b);
				DDataUtils.saveAll();
				
				sender.sendMessage(ChatColor.GREEN + "Set block type " + b.getType() + " you are looking at id " + id);
			}
			
			else if (args[0].equalsIgnoreCase("count")) {
				int c = 0;
				String mobID = args[1];
				String dID = args[2];
				Dungeon dg = DDataUtils.getDungeon(dID);
				for (DTurn turn : dg.getTurns()) {
					for (TSMob mob : turn.getSpawn().getMobs()) {
						if (mob.getMob().equals(mobID)) c += mob.getAmount(); 
					}
				}
				sender.sendMessage("Total of " + mobID + ": " + c);
			}
			
			else if (args[0].equalsIgnoreCase("setgod")) {
				Player player = Bukkit.getPlayer(args[1]);
				DPlayerUtils.setGod(player, Integer.parseInt(args[2]));
				sender.sendMessage("§aSet god to " + player.getName() + "!");
			}
			
			else if (args[0].equalsIgnoreCase("getticket")) {
				String id = args[1];
				ItemStack is = Tickets.getTicket(id);
				Player p = (Player) sender;
				p.getInventory().addItem(is);
				
			}
			
			else if (args[0].equalsIgnoreCase("dropmoney") ) {
				double value = Double.valueOf(args[1]);
				Player p = (Player) sender;
				DMoneyCoin.drop(p.getLocation(), new DMoneyCoin(value));
			}
			
			else if (args[0].equalsIgnoreCase("tofolder")) {
				File folder = new File(Dungeons.get().getDataFolder(), "dungeons");
				folder.mkdirs();
				DDataUtils.getDungeons().forEach((id, d) -> {
					File file = new File(Dungeons.get().getDataFolder() + "//dungeons//" + id + ".yml");
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					DDataUtils.save(id);
				});
				sender.sendMessage("Oke");
			}

			else if (args[0].equalsIgnoreCase("tp")) {
				var world = args[1];
				var dungeonID = args[2];
				var lid = args[3];
				var d = DDataUtils.getDungeon(dungeonID);
				var l = d.getLocation(lid).getLocation(Bukkit.getWorld(world));
				var player = (Player) sender;
				player.teleport(l);
			}

			else if (args[0].equalsIgnoreCase("editmap")) {
				var world = args[1];

				// Load world
				if (Bukkit.getWorld(world) != null) return false;

				var creator = new WorldCreator(world);
				var tempWorldFolder = new File(Bukkit.getWorldContainer() + "//" + world);
				var sourceWorld = new File(plugin.getDataFolder() + "//" + WorldLoader.PATH + "//" + world);
				if (!sourceWorld.exists()) {
					plugin.getLogger().warning(sourceWorld.getName() + " world doesnt exist!");
					return false;
				}

				// Remove uid.dat file
				var uiddatfile = new File(sourceWorld, "uid.dat");
				if (uiddatfile.exists()) {
					try {
						FileUtils.forceDelete(uiddatfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// Remove and create folder
				FileUtils.deleteDirectory(tempWorldFolder);
				tempWorldFolder.mkdirs();
				FileUtils.copyDirectory(sourceWorld, tempWorldFolder);

				try {
					// Load and add cache
					long start = System.currentTimeMillis();
					plugin.getLogger().warning("Loading world " + world + "... (from async task)");
					creator.createWorld();
					plugin.getLogger().warning("Loaded world " + world + "!");
					plugin.getLogger().warning("Took " + (System.currentTimeMillis() - start) + "ms");
				}
				catch (Exception e) {
					plugin.getWorldLoader().unload(world, true);
					plugin.getLogger().warning("It seems like there is an exception interfere it from loading");
					plugin.getLogger().warning("Unloaded world " + world);
					e.printStackTrace();
				}

				// Done
				sender.sendMessage("§aDone!");
				sender.sendMessage("§aUse /d3 tp " + world + " <dungeon> <dlocation>");
			}

			else if (args[0].equalsIgnoreCase("savemap")) {
				var worldName = args[1];
				var world = Bukkit.getWorld(worldName);
				if (world == null) {
					sender.sendMessage("§cWorld null!");
					return false;
				}

				// Save world
				world.save();
				var tempWorldFolder = new File(Bukkit.getWorldContainer() + "//" + worldName);
				var sourceWorld = new File(plugin.getDataFolder() + "//" + WorldLoader.PATH + "//" + worldName);
				if (!sourceWorld.exists()) {
					plugin.getLogger().warning(sourceWorld.getName() + " world doesnt exist!");
					return false;
				}

				// Remove uid.dat file
				var uiddatfile = new File(tempWorldFolder, "uid.dat");
				if (uiddatfile.exists()) {
					try {
						FileUtils.forceDelete(uiddatfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// Remove and create folder
				FileUtils.deleteDirectory(sourceWorld);
				sourceWorld.mkdirs();
				FileUtils.copyDirectory(tempWorldFolder, sourceWorld);

				// Unload
				plugin.getWorldLoader().unload(worldName, false, false, false);

				// Done
				sender.sendMessage("§aDone!");
			}

	}
		catch (ArrayIndexOutOfBoundsException | IOException e) {
			e.printStackTrace();
			sendTut(sender);
		}
		
		return false;
	}
	
	public void sendTut(CommandSender sender) {
		List<String> m = Lists.newArrayList();
		
		m.add("");
		m.add(Utils.c("&c&lDungeon3 by MankaiStep"));
		m.add(Utils.c("&2/dungeon3 reload: &aReload plugin"));
		m.add(Utils.c("&2/dungeon3 tofolder: &aSave dungeons to folder"));
		m.add(Utils.c("&2/dungeon3 setgod <player> <tick>: &aSet player to god"));
		m.add(Utils.c("&2/dungeon3 start <dungeon>: &aStart dungeon only you"));
		m.add(Utils.c("&2/dungeon3 setblock <dungeon> <id>: &aSet block of dungeon"));
		m.add(Utils.c("&2/dungeon3 setlocation <dungeon> <radius> <id>: &aSet location of dungeon"));	
		m.add(Utils.c("&2/dungeon3 tp <world> <dungeon> <locationID>: &aTeleport"));
		m.add(Utils.c("&2/dungeon3 count <mobID> <dungeon>: &aCount amount of mobs"));
		m.add(Utils.c("&2/dungeon3 getticket <dungeon>: &aGet ticket of <dungeon>"));
		m.add(Utils.c("&2/dungeon3 dropmoney <value>: &aDrop money coin"));
		m.add(Utils.c("&2/dungeon3 editmap <world>: &aEdit map"));
		m.add(Utils.c("&2/dungeon3 savemap <world>: &aEdit map"));
		m.add("");
		
		m.forEach(sender::sendMessage);
	}

}
