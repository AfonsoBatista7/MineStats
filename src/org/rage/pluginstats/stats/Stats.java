package org.rage.pluginstats.stats;

/**
 * @author Afonso Batista
 * 2021
 */
public enum Stats {
	
	PLAYERID(0, "playerId", "Player ID", false),
	NAME(1, "name", "Player Name", true),
	NAMES(2, "names","Number of names", true),
	BLOCKSDEST(3, "blcksDestroyed", "Blocks Destroyed", true),
	BLOCKSPLA(4, "blcksPlaced", "Blocks Placed", true),
	KILLS(5, "kills", "Kills", true),
	MOBKILLS(6, "mobKills", "Monster Kills", true),
	TRAVELLED(7, "mTravelled", "Meters Travelled", true),
	DEATHS(8, "deaths", "Deaths", true),
	MEDALS(9, "medals", "Medals", false),
	REDSTONEUSED(10, "redstoneUsed", "Redstone Used", true),
	FISHCAUGHT(11, "fishCaught", "Fish Caught", true),
	ENDERDRAGONKILLS(12, "enderdragonKills", "Ender Dragon Kills", true),
	WITHERKILLS(13, "witherKills", "Wither Kills", true),
	VERSIONS(14, "versionPlayed", "Number of Versions Played", true),
	TIMESLOGIN(15, "timeslogin", "Number of Logins", true),
	LASTLOGIN(16, "lastLogin", "Last LogIn", true),
	PLAYERSINCE(17, "playerSince", "Player Since", true),
	TIMEPLAYED(18, "timePlayed", "Time Played", true),
	ONLINE(19, "online", "Is Online?", false),
	MEDALNAME(20, "medalName", "", false),
	MEDALLEVEL(21, "medalLevel", "", false);
	
	private int index;
	private String query, text;
	private boolean print;
	
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
}
