package org.rage.pluginstats.utils;

import org.bukkit.ChatColor;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.stats.Stats;

/**
 * @author Afonso Batista
 * 2021
 */
public class Util {
	
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
	
	public static long getMedalVariable(PlayerProfile pp, Medals medal) {
		
		switch(medal) {
			case DESTROYER:
				return pp.getBlocksDestroyed();
			case BUILDER:
				return pp.getBlocksPlaced();
			case PVPMASTER:
				return pp.getPlayersKilled();
			case MOBSLAYER:
				return pp.getMobsKilled();
			case WORLDTRAVELLER:
				return pp.getMetersTraveled();
			case TIMETRAVELLER:
				return pp.getVersions().size();
			case REDSTONENGINEER:
				return pp.getRedtoneUsed();
			case VETERAN:
				return pp.lastLogIn.getYear() - pp.playerSince.getYear();
			case ZOMBIE:
				return pp.getDeaths();
			case LOGINNER:
				return pp.getTimesLogin();
			case DRAGONSLAYER:
				return pp.getEnderDragonKills();
			case WITHERSLAYER:
				return pp.getWhitherKills();
			case TIMEWALKER:
				return pp.getTimePlayed()/3600;
			case FISHERMAN:
				return pp.getFishCaught();
			case NAMEHOLDER:
				return pp.getAllNames().size();
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
			case NAMES:
				return pp.getAllNames().size();
			case BLOCKSDEST:
				return pp.getBlocksDestroyed();
			case BLOCKSPLA:
				return pp.getBlocksPlaced();
			case KILLS:
				return pp.getPlayersKilled();
			case MOBKILLS:
				return pp.getMobsKilled();
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
				return pp.getRedtoneUsed();
			case FISHCAUGHT:
				return pp.getFishCaught();
			case ENDERDRAGONKILLS:
				return pp.getEnderDragonKills();
			case WITHERKILLS:
				return pp.getWhitherKills();
			case VERSIONS:
				return pp.getVersions().size();
			default:
			
		}
		return 0;
	}
}
