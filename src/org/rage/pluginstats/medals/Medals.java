package org.rage.pluginstats.medals;

import org.rage.pluginstats.tags.Tags;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public enum Medals {
	
	NOSTALGIAPLAYER(MLevel.GOD, 0, "Play On Server", Tags.MEMBER),															// #0 Played on server
	GOD(MLevel.GOD, 1, "Get every medal in GOD level", Tags.GOD, 818435741905977394L),										// #1 When you have all the medals in god Level
	BETA(MLevel.GOD, 2, "You needed to have played on Beta", Tags.BETA),		  											// #2 Beta Player
	DONATOR(MLevel.GOD, 3, "Donate to Me :D to help the server!", Tags.DONATOR),         									// #3 Player Donator 
	DESTROYER(20000, 5, 4, "Blocks Destroyed", "Destroy some blocks", Tags.DESTROYER, 818435248035201054L),					// #4 Blocks Destroyed
	BUILDER(20000, 4, 5, "Blocks Placed", "Place some blocks", Tags.BUILDER, 818433125003558913L),							// #5 Blocks Placed
	PVPMASTER(30, 3, 6, "Kills", "Kill some players", Tags.PVP, 818432834095022090L),			  							// #6 Players Killed
	MOBSLAYER(5000, 2, 7, "Mob Kills", "Kill some mobs", Tags.MOBSLAYER),			  										// #7 Mobs Killed
	WORLDTRAVELLER(500000, 2, 8, "Meters Travelled", "Travel arround the world :D", Tags.TRAVELLER),		  				// #8 Meters Walked
	TIMETRAVELLER(5, 1.5, 9, "Versions Played", "Play in multiple versions on the server", Tags.ARCHIVIST),		  			// #9 Versions Played				 
	REDSTONENGINEER(64, 6, 10, "Redstone Used", "Use some Redstone", Tags.REDSTONE, 818432472475369482L),					// #10 Redstone Used
	VETERAN(2, 1.5, 11, "Years Playing", "Play Since a long time", Tags.VETERAN, 829051983288860724L),						// #11 Player Since                  
	ZOMBIE(50, 2, 12, "Deaths", "DIE B)", Tags.SKIPDEATH),               													// #12 Deaths
	LOGINNER(100, 2, 13, "Times Login", "Login on server", Tags.MEMBER),				  									// #13 Times Login
	DRAGONSLAYER(1, 10, 14, "EnderDragon Kills", "Kill the EnderDragon B)", Tags.ENDERKILLER),			  					// #14 Kill EnderDragon
	WITHERSLAYER(1, 10, 15, "Whither Kills", "Kill the Whither B)", Tags.NETHERKILLER),			  							// #15 Kill Wither
	TIMEWALKER(100, 2, 16, "Time Played", "Play some time on the server :DDD", Tags.TIMEWALKER, 952897726901813258L),		// #16 Time Played					 
	FISHERMAN(10, 2, 17, "Fish Caught", "Catch some fish", Tags.FISHERMAN, 824004714479878144L),							// #17 Fish Caught
	NAMEHOLDER(2, 1.5, 18, "Number of names", "Change your minecraft player name", Tags.NAMEHOLDER),			  			// #18 Number of names
	MINER(10000, 3, 19, "Mined Blocks", "Break blocks under the sea level (63)", Tags.MINER);								// #19 Mined Blocks
	
	private int index;
	private long transition, roleId=0;
	private double multiplyer;
	private String stat, howToGet;
	private MLevel level;
	private Tags tag;
	
	Medals(long transition, double multiplayer, int index, String stat, String howToGet, Tags tag, long roleId) {
		level = MLevel.I;
		this.index = index;
		this.transition = transition;
		this.multiplyer = multiplayer;
		this.stat = stat;
		this.howToGet = howToGet;
		this.tag = tag;
		this.roleId = roleId;
	}
	
	Medals(long transition, double multiplayer, int index, String stat, String howToGet, Tags tag) {
		level = MLevel.I;
		this.index = index;
		this.transition = transition;
		this.multiplyer = multiplayer;
		this.stat = stat;
		this.howToGet = howToGet;
		this.tag = tag;
	}
	
	Medals(MLevel level, int index, String howToGet, Tags tag) {
		this.index = index;
		this.level = level;
		this.transition = 0;
		this.howToGet = howToGet;
		this.tag = tag;
	}
	
	Medals(MLevel level, int index, String howToGet, Tags tag, long roleId) {
		this.index = index;
		this.level = level;
		this.transition = 0;
		this.howToGet = howToGet;
		this.tag = tag;
		this.roleId = roleId;
	}
	
	public long getRoleId() {
		return roleId;
	}
	
	public String getHowToGet() {
		return howToGet;
	}
	
	public MLevel getMedalLevel() {
		return level;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getStatName() {
		return stat;
	}
	
	public double getMultiplyer() {
		return multiplyer;
	}
	
	public long getTransition() {
		return transition;
	}
	
	public Tags getTag() {
		return tag;
	}
	
}
