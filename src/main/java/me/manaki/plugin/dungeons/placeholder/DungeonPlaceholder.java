package me.manaki.plugin.dungeons.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.manaki.plugin.dungeons.buff.Buff;
import me.manaki.plugin.dungeons.dungeon.Dungeon;
import me.manaki.plugin.dungeons.dungeon.player.DPlayer;
import me.manaki.plugin.dungeons.dungeon.status.DStatus;
import me.manaki.plugin.dungeons.dungeon.turn.DTurn;
import me.manaki.plugin.dungeons.dungeon.util.DGameUtils;
import me.manaki.plugin.dungeons.dungeon.util.DPlayerUtils;
import me.manaki.plugin.dungeons.util.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class DungeonPlaceholder extends PlaceholderExpansion  {

    /*
    dungeon_name
    dungeon_name_uppercase
    dungeon_percent
    dungeon_time_remain
    dungeon_objective_line1
    dungeon_objective_line2
    dungeon_current_players
    dungeon_max_players
    dungeon_respawn_left
    dungeon_respawn_max
    dungeon_turn_mob_left
    dungeon_turn_mob_max
    dungeon_turn_slave_left
    dungeon_turn_slave_max
    dungeon_turn_guarded_health_percent
     */

    @Override
    public String getIdentifier() {
        return "dungeon";
    }

    @Override
    public String getAuthor() {
        return "MankaiStep";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String s){

        if (s.equalsIgnoreCase("buff_drop")) {
            var buff = Buff.DROP * 100;
            DPlayer dp = DPlayer.from(player);
            buff += dp.getDropRateBuff();
            return buff + "%";
        }
        else if (s.equalsIgnoreCase("buff_revive")) {
            DPlayer dp = DPlayer.from(player);
            return dp.getReviveBuff() + "";
        }

        if (!DPlayerUtils.isInDungeon(player)) return "no dungeon";

        String id = DPlayerUtils.getCurrentDungeon(player);
        Dungeon d = Dungeon.get(id);
        DStatus status = DGameUtils.getStatus(id);
        DTurn turn = d.getTurn(status.getTurn());

        String o = turn.getObjective();
        List<String> objective = Utils.toList(o, 20, "");

        if (s.equalsIgnoreCase("name")) {
            return d.getInfo().getName();
        }
        else if (s.equalsIgnoreCase("name_uppercase")) {
            return d.getInfo().getName().toUpperCase();
        }
        else if (s.equalsIgnoreCase("percent")) {
            return (status.getTurn() - 1) * 100 / d.getTurns().size() + "";
        }
        else if (s.equalsIgnoreCase("time_remain")) {
            int remain = d.getOption().getMaxTime() - new Long((System.currentTimeMillis() - status.getStart()) / 1000).intValue();
            return Utils.getFormat(remain);
        }
        else if (s.equalsIgnoreCase("objective_line_1")) {
            return objective.get(0);
        }
        else if (s.equalsIgnoreCase("objective_line_2")) {
            if (objective.size() > 1) return objective.get(1);
            return "";
        }
        else if (s.equalsIgnoreCase("current_players")) {
            return status.getPlayers().size() + "";
        }
        else if (s.equalsIgnoreCase("max_players")) {
            return d.getOption().getPlayer().getMax() + "";
        }
        else if (s.equalsIgnoreCase("respawn_left")) {
            return d.getRule().getRespawnTime() - status.getStatistic(player).getDead() + "";
        }
        else if (s.equalsIgnoreCase("respawn_max")) {
            return  d.getRule().getRespawnTime() + DPlayer.from(player).getReviveBuff() + "";
        }
        else if (s.equalsIgnoreCase("turn_mob_left")) {
            return (DGameUtils.countMobs(d.getTurn(status.getTurn())) - status.getTurnStatus().getStatistic().getMobKilled()) + "";
        }
        else if (s.equalsIgnoreCase("turn_mob_max")) {
            return DGameUtils.countMobs(d.getTurn(status.getTurn())) + "";
        }
        else if (s.equalsIgnoreCase("turn_slave_left")) {
            return status.getTurnStatus().getSlaveToSaves().size() + "";
        }
        else if (s.equalsIgnoreCase("turn_slave_max")) {
            return DGameUtils.countSlaves(d.getTurn(status.getTurn())) + "";
        }
        else if (s.equalsIgnoreCase("turn_guarded_health_percent")) {
            LivingEntity g = status.getTurnStatus().getGuarded();
            if (g == null) return "0";
            double max = g.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double current = status.getTurnStatus().getGuarded().getHealth();
            return Double.valueOf(current / max * 100).intValue() + "";
        }

        return "Wrong placeholder";
    }

}
