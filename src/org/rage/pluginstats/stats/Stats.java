package org.rage.pluginstats.stats;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public enum Stats {
	//index, query, stat name, if is printable or not, first value
	PLAYERID(0, "playerId", "Player ID", false),
	NAME(1, "name", "Player Name", true),
	NAMES(2, "names","Number of names", true),
	BLOCKSDEST(3, "blcksDestroyed", "Blocks Destroyed", true, 0L),
	BLOCKSPLA(4, "blcksPlaced", "Blocks Placed", true, 0L),
	KILLS(5, "kills", "Kills", true, 0L),
	MOBKILLS(6, "mobKills", "Monster Kills", true, 0L),
	TRAVELLED(7, "mTravelled", "Meters Travelled", true, 0L),
	DEATHS(8, "deaths", "Deaths", true, 0L),
	MEDALS(9, "medals", "Medals", false, Arrays.asList(new Medal(Medals.NOSTALGIAPLAYER).createMedalDoc())),
	REDSTONEUSED(10, "redstoneUsed", "Redstone Used", true, 0L),
	FISHCAUGHT(11, "fishCaught", "Fish Caught", true, 0L),
	ENDERDRAGONKILLS(12, "enderdragonKills", "Ender Dragon Kills", true, 0L),
	WITHERKILLS(13, "witherKills", "Wither Kills", true, 0L),
	VERSIONS(14, "versionPlayed", "Number of Versions Played", true, Arrays.asList(Main.currentServer.getVersion())),
	TIMESLOGIN(15, "timeslogin", "Number of Logins", true, 0L),
	LASTLOGIN(16, "lastLogin", "Last LogIn", true, new SimpleDateFormat("dd/MM/yyyy").format(new Date())),
	PLAYERSINCE(17, "playerSince", "Player Since", true, new SimpleDateFormat("dd/MM/yyyy").format(new Date())),
	TIMEPLAYED(18, "timePlayed", "Time Played", true, "0 Hr 0 Min"),
	ONLINE(19, "online", "Is Online?", false),
	MINEDBLOCKS(20, "blockMined", "Mined Blocks", true, 0L);
	
	
	private int index;
	private String query, text;
	private boolean print;
	private Object firstValue;
	
	Stats(int index, String query, String text, boolean print, Object firstValue) {
		this.index = index;
		this.query = query;
		this.text = text;
		this.print = print;
		this.firstValue = firstValue;
	}
	
	Stats(int index, String query, String text, boolean print) {
		this.index = index;
		this.query = query;
		this.text = text;
		this.print = print;
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
	
	public Object getFirstValue() {
		return firstValue;
	}
}
