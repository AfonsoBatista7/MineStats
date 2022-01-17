package org.rage.pluginstats.medals;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public enum Medals {
	
	NOSTALGIAPLAYER(MLevel.GOD, 0, "Play On Server"),														// #0 Played on server
	GOD(MLevel.GOD, 1, "Get every medal in GOD level"),														// #1 When you have all the medals in god Level
	BETA(MLevel.GOD, 2, "You needed to have played on Beta"),		  										// #2 Beta Player
	DONATOR(MLevel.GOD, 3, "Donate to Me :D to help the server!"),         									// #3 Player Donator 
	DESTROYER(20000, 5, 4, "Blocks Destroyed", "Destroy some blocks"),			  							// #4 Blocks Destroyed
	BUILDER(20000, 4, 5, "Blocks Placed", "Place some blocks"),				  								// #5 Blocks Placed
	PVPMASTER(30, 3, 6, "Kills", "Kill some players"),			  											// #6 Players Killed
	MOBSLAYER(5000, 2, 7, "Mob Kills", "Kill some mobs"),			  										// #7 Mobs Killed
	WORLDTRAVELLER(500000, 2, 8, "Meters Travelled", "Travel arround the world :D"),		  				// #8 Meters Walked
	TIMETRAVELLER(5, 1.5, 9, "Versions Played", "Play in multiple versions on the server"),		  			// #9 Versions Played				 
	REDSTONENGINEER(64, 6, 10, "Redstone Used", "Use some Redstone"),		  								// #10 Redstone Used
	VETERAN(2, 1.5, 11, "Player Since", "Play Since a long time"),					  						// #11 Player Since                  
	ZOMBIE(50, 2, 12, "Deaths", "DIE B)"),               													// #12 Deaths
	LOGINNER(100, 2, 13, "Times Login", "Login on server"),				  									// #13 Times Login
	DRAGONSLAYER(1, 10, 14, "EnderDragon Kills", "Kill the EnderDragon B)"),			  					// #14 Kill EnderDragon
	WITHERSLAYER(1, 10, 15, "Whither Kills", "Kill the Whither B)"),			  							// #15 Kill Wither
	TIMEWALKER(100, 2, 16, "Time Played", "Play some time on the server :DDD"),			  					// #16 Time Played					 
	FISHERMAN(10, 2, 17, "Fish Caught", "Catch some fish"),													// #17 Fish Caught
	NAMEHOLDER(2, 1.5, 18, "Number of names", "Change your minecraft player name"),				  			// #18 Number of names
	MINER(10000, 3, 19, "Mined Blocks", "Break stone and ore blocks under the sea level (62)");				// #19 Mined Blocks
	
	private int index;
	private long transition;
	private double multiplyer;
	private String stat, howToGet;
	private MLevel level;
	
	Medals(long transition, double multiplayer, int index, String stat, String howToGet) {
		level = MLevel.I;
		this.index = index;
		this.transition = transition;
		this.multiplyer = multiplayer;
		this.stat = stat;
		this.howToGet = howToGet;
	}
	
	Medals(MLevel level, int index, String howToGet) {
		this.index = index;
		this.level = level;
		this.transition = 0;
		this.howToGet = howToGet;
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
	
	
	
}
