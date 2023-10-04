package org.rage.pluginstats.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.player.PlayerProfile;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.stats.Stats;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class Util {
	
	private static List<String> colors = Arrays.asList("&4", "&c", "&6", "&e", "&a", "&2", "&b", "&3", "&9", "&1", "&5", "&d");
	
	public static String chat(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static String secondsToTimestamp(long seconds) {
		
		long hours = seconds / 3600;
		double decimal = seconds/3600.0F-hours;
		int minutes = (int) ((decimal*3600)/60);
		
		if(minutes==0)
			return String.format("%s Hours", hours);
		else
			return String.format("%s Hr %s Min", hours, minutes);
	}
	
	public static String rainbowText(String message) {
		
		StringBuilder builder = new StringBuilder(message);
				
		int index = 0;
		
		
		
		for(int i=0; i<builder.length(); i+=3) {
			
			if(index == colors.size()) index=0;
			
			builder.insert(i, colors.get(index));
			
			index++;
			
		}
		
		return builder.toString();
	}
	
	public static long getMedalVariable(ServerPlayer pp, Medals medal) {
		
		switch(medal) {
			case DESTROYER:
				return pp.getBlockStats().getBlocksDestroyed();
			case BUILDER:
				return pp.getBlockStats().getBlocksPlaced();
			case PVPMASTER:
				return pp.getMobStats().getPlayersKilled();
			case MOBSLAYER:
				return pp.getMobStats().getTotalNumMobsKilled();
			case WORLDTRAVELLER:
				return pp.getMetersTraveled();
			case TIMETRAVELLER:
				return pp.getNumberOfVersions();
			case REDSTONENGINEER:
				return pp.getBlockStats().getRedstoneUsed();
			case VETERAN:
				return pp.getLastLoginDate().getYear() - pp.getLastLoginDate().getYear();
			case ZOMBIE:
				return pp.getDeaths();
			case LOGINNER:
				return pp.getTimesLogin();
			case DRAGONSLAYER:
				return pp.getMobStats().getEnderDragonKills();
			case WITHERSLAYER:
				return pp.getMobStats().getWitherKills();
			case TIMEWALKER:
				return pp.getTimePlayed()/3600;
			case FISHERMAN:
				return pp.getMobStats().getFishCaught();
			case MINER:
				return pp.getBlockStats().getMinedBlocks();
			default:
		}
		return 0;
	}
	
	public static Object getStatVariable(PlayerProfile pp, Stats stat) {
		switch(stat) {
			case PLAYERID:
				return pp.getPlayerID();
			case NAME:
				return pp.getName();
			case BLOCKSDEST:
				return pp.getBlockStats().getBlocksDestroyed();
			case BLOCKSPLA:
				return pp.getBlockStats().getBlocksPlaced();
			case KILLS:
				return pp.getMobStats().getPlayersKilled();
			case MOBKILLS:
				return pp.getMobStats().getTotalNumMobsKilled();
			case TRAVELLED:
				return pp.getMetersTraveled();
			case DEATHS:
				return pp.getDeaths();
			case TIMESLOGIN:
				return pp.getTimesLogin();
			case LASTLOGIN:
				return pp.getLastLogin();
			case PLAYERSINCE:
				return pp.getPlayerSince();
			case TIMEPLAYED:
				return pp.getTotalPlaytime();
			case ONLINE:
				return pp.isOnline();
			case MEDALS:
				return pp.getMedals();
			case REDSTONEUSED:
				return pp.getBlockStats().getRedstoneUsed();
			case FISHCAUGHT:
				return pp.getMobStats().getFishCaught();
			case ENDERDRAGONKILLS:
				return pp.getMobStats().getEnderDragonKills();
			case WITHERKILLS:
				return pp.getMobStats().getWitherKills();
			case VERSIONS:
				return pp.getNumberOfVersions();
			case BLOCKSMINED:
				return pp.getBlockStats().getMinedBlocks();
			case MOBSKILLED:
				return pp.getMobStats().getNumMobsKilledList();
			default:
			
		}
		return 0;
	}
}
