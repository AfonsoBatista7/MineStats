package org.rage.pluginstats.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.player.PlayerProfile;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.stats.GamestatField;
import org.rage.pluginstats.stats.Stats;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class Util {

	private static List<String> colors = Arrays.asList("&4", "&c", "&6", "&e", "&a", "&2", "&b", "&3", "&9", "&1", "&5", "&d");

	public static String chat(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static String minutesToTimestamp(long minutes) {
		long hours = minutes / 60;
		int minutesInt = (int) (minutes % 60);
		String timeStr = minutesInt == 0 ? "%s Hours" : "%s Hr %s Min";
		return String.format(timeStr, hours, minutesInt);
	}

	public static String rainbowText(String message) {
		StringBuilder builder = new StringBuilder(message);
		int index = 0;
		for (int i = 0; i < builder.length(); i += 3) {
			if (index == colors.size()) index = 0;
			builder.insert(i, colors.get(index));
			index++;
		}
		return builder.toString();
	}

	public static long getMedalVariable(ServerPlayer pp, Medals medal) {
		switch (medal) {
			case DESTROYER:       return pp.getBlockStats().getBlocksDestroyed();
			case BUILDER:         return pp.getBlockStats().getBlocksPlaced();
			case PVPMASTER:       return pp.getMobStats().getPlayersKilled();
			case MOBSLAYER:       return pp.getMobStats().getTotalNumMobsKilled();
			case WORLDTRAVELLER:  return pp.getMetersTraveled();
			case TIMETRAVELLER:   return pp.getNumberOfVersions();
			case REDSTONENGINEER: return pp.getBlockStats().getRedstoneUsed();
			case VETERAN:         return pp.getLastLoginDate().getYear() - pp.getPlayerSinceDate().getYear();
			case ZOMBIE:          return pp.getDeaths();
			case LOGINNER:        return pp.getTimesLogin();
			case DRAGONSLAYER:    return pp.getMobStats().getEnderDragonKills();
			case WITHERSLAYER:    return pp.getMobStats().getWitherKills();
			case TIMEWALKER:      return pp.getTimePlayed() / 60;
			case FISHERMAN:       return pp.getMobStats().getFishCaught();
			case MINER:           return pp.getBlockStats().getMinedBlocks();
			case IDLER:           return pp.getTimeAFK() / 60;
			default:              return 0;
		}
	}

	/**
	 * Returns the value for a gameplay stat (nested under stats.* in MongoDB).
	 */
	public static Object getStatVariable(PlayerProfile pp, Stats stat) {
		switch (stat) {
			case BLOCKSDEST:       return pp.getBlockStats().getBlocksDestroyed();
			case BLOCKSPLA:        return pp.getBlockStats().getBlocksPlaced();
			case BLOCKSMINED:      return pp.getBlockStats().getMinedBlocks();
			case KILLS:            return pp.getMobStats().getPlayersKilled();
			case MOBKILLS:         return pp.getMobStats().getTotalNumMobsKilled();
			case TRAVELLED:        return pp.getMetersTraveled();
			case DEATHS:           return pp.getDeaths();
			case REDSTONEUSED:     return pp.getBlockStats().getRedstoneUsed();
			case FISHCAUGHT:       return pp.getMobStats().getFishCaught();
			case ENDERDRAGONKILLS: return pp.getMobStats().getEnderDragonKills();
			case WITHERKILLS:      return pp.getMobStats().getWitherKills();
			case TIMESLOGIN:       return pp.getTimesLogin();
			case MOBSKILLED:       return pp.getMobStats().getNumMobsKilledList();
			case BLOCKS:           return pp.getBlockStats().getBlockStatsList();
			default:               return 0;
		}
	}

	/**
	 * Returns the value for a top-level gamestat field.
	 */
	public static Object getGamestatFieldValue(PlayerProfile pp, GamestatField field) {
		switch (field) {
			case STATUS:    return pp.isOnline();
			case TIMEPLAYED: return pp.getTimePlayed();
			case TIMEAFK:   return pp.getTimeAFK();
			case LASTLOGIN: return pp.getLastLogin();
			default:        return null;
		}
	}
}
