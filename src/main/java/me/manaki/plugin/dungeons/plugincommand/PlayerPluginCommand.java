package me.manaki.plugin.dungeons.plugincommand;

import me.manaki.plugin.dungeons.Dungeons;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.votekick.KickVotes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerPluginCommand implements CommandExecutor {

	private Dungeons plugin;

	public PlayerPluginCommand(Dungeons plugin) {
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command var2, String var3, String[] args) {
		
		Player player = (Player) sender;
		
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("thoat")) {
				plugin.getDungeonManager().kick(player, true);
			}
			else if (args[0].equalsIgnoreCase("votekick")) {
				if (args.length <= 1) {
					sender.sendMessage("§aDùng: §f/phoban votekick <player>");
					return false;
				}
				String tn = args[1];
				Player target = Bukkit.getPlayer(tn);
				if (target == null) {
					sender.sendMessage("§cNgười chơi không tồn tại");
					return false;
				}
				if (target == player) {
					sender.sendMessage("§c?");
					sender.sendMessage("§cXài §f/phoban thoat §cđi bạn");
					return false;
				}
				if (!DPlayerUtils.isInDungeon(player)) {
					sender.sendMessage("§cBạn có ở trong phó bản đâu?");
					return false;
				}
				String cacheID = Dungeons.get().getDungeonManager().getCurrentDungeonCache(player);
				if (!DPlayerUtils.isInDungeon(target, cacheID)) {
					sender.sendMessage("§cĐối phương không cùng phó bản với bạn");
					return false;
				}
				UUID uuid = target.getUniqueId();
				KickVotes.voteKick(DPlayerUtils.getCurrentDungeonCache(player), uuid, player.getUniqueId());
			}
		}
		

		
		return false;
	}
	
}
