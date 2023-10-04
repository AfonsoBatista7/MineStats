package org.rage.pluginstats.stats;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.server.ServerManager;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public enum Stats {
	//index, query, stat name, if is printable or not, to Upload, first value
	PLAYERID(0, "playerId", "Player ID", false, false),
	NAME(1, "name", "Player Name", true, false),
	BLOCKSDEST(3, "blcksDestroyed", "Blocks Destroyed", true, true, 0L),
	BLOCKSPLA(4, "blcksPlaced", "Blocks Placed", true, true, 0L),
	BLOCKSMINED(5, "blockMined", "Blocks Mined", true, true, 0L),
	KILLS(6, "kills", "Kills", true, true, 0L),
	MOBKILLS(7, "mobKills", "Monster Kills", true, true, 0L),
	TRAVELLED(8, "mTravelled", "Meters Travelled", true, true, 0L),
	DEATHS(9, "deaths", "Deaths", true, true, 0L),
	MEDALS(10, "medals", "Medals", false, false,Arrays.asList(new Medal(Medals.NOSTALGIAPLAYER).createMedalDoc())),
	REDSTONEUSED(11, "redstoneUsed", "Redstone Used", true, true, 0L),
	FISHCAUGHT(12, "fishCaught", "Fish Caught", true, true, 0L),
	ENDERDRAGONKILLS(13, "enderdragonKills", "Ender Dragon Kills", true, true, 0L),
	WITHERKILLS(14, "witherKills", "Wither Kills", true, true, 0L),
	VERSIONS(15, "versionPlayed", "Number of Versions Played", true, false, Arrays.asList(ServerManager.getServerVersion())),
	TIMESLOGIN(16, "timeslogin", "Number of Logins", true, true, 0L),
	LASTLOGIN(17, "lastLogin", "Last LogIn", true, true,new SimpleDateFormat("dd/MM/yyyy").format(new Date())),
	PLAYERSINCE(18, "playerSince", "Player Since", true, false, new SimpleDateFormat("dd/MM/yyyy").format(new Date())),
	TIMEPLAYED(19, "timePlayed", "Time Played", true, true, "0 Hr 0 Min"),
	ONLINE(20, "online", "Is Online?", false, true),
	MOBSKILLED(21, "mobsKilled", "Mobs Killed", false, true, Arrays.asList());
	
	private int index;
	private String query, text;
	private boolean print, toUpload;
	private Object firstValue; 
	
	Stats(int index, String query, String text, boolean print, boolean toUpload, Object firstValue) {
		this.index = index;
		this.query = query;
		this.text = text;
		this.print = print;
		this.firstValue = firstValue;
		this.toUpload = toUpload;
	}
	
	Stats(int index, String query, String text, boolean print, boolean toUpload) {
		this.index = index;
		this.query = query;
		this.text = text;
		this.print = print;
		this.toUpload = toUpload;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getQuery() {
		return query;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean toPrint() {
		return print;
	}
	
	public boolean toUpload() {
		return toUpload;
	}
	
	public Object getFirstValue() {
		return firstValue;
	}
}
