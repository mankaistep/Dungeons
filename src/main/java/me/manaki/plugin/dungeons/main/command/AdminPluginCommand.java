package me.manaki.plugin.dungeons.main.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.manaki.plugin.dungeons.dungeon.manager.DGameStarts;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyCoin;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.util.DDataUtils;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.location.DLocation;
import me.manaki.plugin.dungeons.dungeon.manager.DGameEnds;
import me.manaki.plugin.dungeons.dungeon.turn.TSMob;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.queue.DQueues;
import me.manaki.plugin.dungeons.ticket.Tickets;
import me.manaki.plugin.dungeons.util.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class AdminPluginCommand implements CommandExecutor {

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
				List<UUID> list = Lists.newArrayList(player.getUniqueId());
				if (args.length > 2) {
					for (int i = 2 ; i < args.length ; i++) list.add(Bukkit.getPlayerUniqueId(args[i]));
				}
				for (UUID uuid : list) {
					Utils.toSpawn(Bukkit.getPlayer(uuid));
				}
				Bukkit.getScheduler().runTaskLater(Dungeons.get(), () -> {
					if (DDataUtils.checkProblem(id)) {
						if (DQueues.hasQueue(id)) {
							DQueues.remove(id);
						}
						if (DGameUtils.isPlaying(id)) {
							DGameEnds.loseDungeon(id);
						}
						DGameStarts.startDungeon(id, list);
						DQueues.doStart(id);
					}
				}, 20);

			}
			
			else if (args[0].equalsIgnoreCase("setlocation")) {
				Player player = (Player) sender;
				String dungeon = args[1];
				int radius = Integer.valueOf(args[2]);
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
			
			else if (args[0].equalsIgnoreCase("tp")) {
				Player player = (Player) sender;
				String dungeon = args[1];
				Dungeon dg = DDataUtils.getDungeon(dungeon);
				DLocation dl = dg.getLocations().getOrDefault(cmd, null);
				if (dl == null) {
					player.sendMessage(Utils.c("&cWrong location ID"));
					return false;
				}
				player.teleport(dl.getLocation());
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
			
		}
		catch (ArrayIndexOutOfBoundsException e) {
			sendTut(sender);
			e.printStackTrace();
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
		m.add(Utils.c("&2/dungeon3 tp <dungeon> <locationID>: &aTeleport"));
		m.add(Utils.c("&2/dungeon3 count <mobID> <dungeon>: &aCount amount of mobs"));
		m.add(Utils.c("&2/dungeon3 getticket <dungeon>: &aGet ticket of <dungeon>"));
		m.add(Utils.c("&2/dungeon3 dropmoney <value>: &aDrop money coin"));
		m.add("");
		
		m.forEach(s -> {
			sender.sendMessage(s);
		});
	}

}
