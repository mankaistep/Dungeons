package me.manaki.plugin.dungeons.command;

import me.manaki.plugin.dungeons.main.Dungeons;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {
	
	private int tickDelay;
	private String cmd;
	private CType type;
	
	public Command(String s) {
		load(s);
	}
	
	public String getCommand() {
		return this.cmd;
	}
	
	public CType getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return "[" + this.type.name().toLowerCase() + "] " + this.cmd;
	}
	
	public void execute(Player player) {
		Map<String, String> plh = DPlayerUtils.getPlaceholders(player);
		String c = this.cmd;
		for (Entry<String, String> e : plh.entrySet()) {
			c = c.replace(e.getKey(), e.getValue());
		}
		String cc = c;
		
		if (this.tickDelay > 0) {
			Bukkit.getScheduler().runTaskLater(Dungeons.get(), () -> {
				this.type.execute(cc, player);
			}, this.tickDelay);
		}
		else this.type.execute(cc, player);
	}

	public void load(String s) {
		for (CType t : CType.values()) {
			if (s.contains("[" + t.name().toLowerCase() + "] ")) {
				this.type = t;
				this.cmd = s.replace("[" + t.name().toLowerCase() + "] ", "");
			}
		}
		if (s.equals("")) s = "*";
		
		// Delay
		String regex = "\\{(?<delay>\\d+)}\\s";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(this.cmd);
		while (m.find()) {
			this.tickDelay = Integer.valueOf(m.group("delay"));
			break;
		}
		
		this.cmd = this.cmd.replace("{" + this.tickDelay + "} ", "");
	}
	
}
