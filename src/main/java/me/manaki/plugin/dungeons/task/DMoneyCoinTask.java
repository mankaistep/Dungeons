package me.manaki.plugin.dungeons.task;

import com.google.common.collect.Lists;
import me.manaki.plugin.dungeons.dungeon.moneycoin.DMoneyCoin;
import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.moneyapi.MoneyAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class DMoneyCoinTask extends BukkitRunnable {
	
	private List<Item> checked;
	
	private DMoneyCoinTask() {
		this.checked = Lists.newArrayList();
	}
	
	@Override
	public void run() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.getWorld().getNearbyEntities(player.getLocation(), 0.5, 0.5, 0.5).forEach(i -> {
				if (!(i instanceof Item)) return;
				if (checked.contains(i)) return;
				var item = (Item) i;
				DMoneyCoin coin = DMoneyCoin.from(item);
				if (coin == null) return;
				if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
					MoneyAPI.giveMoney(player, coin.getValue());
					player.sendMessage("§f§l+" + coin.getValue() + "$");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				Bukkit.getScheduler().runTask(Dungeons.get(), () -> {
					checked.remove(i);
					i.remove();
				});
			});
		});	
	}
	
	public static void start() {
		new DMoneyCoinTask().runTaskTimer(Dungeons.get(), 0, 5);
	}

}
