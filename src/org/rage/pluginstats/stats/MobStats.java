package org.rage.pluginstats.stats;

/**
 * @author Afonso Batista
 * 2021 
 */
public class MobStats {
	
	private long playersKilled,
	   			 mobsKilled,
	   			 enderDragonKills,
			     witherKills,
			     fishCaught;
	
	public MobStats() {
		playersKilled = 0;
		mobsKilled = 0;
		enderDragonKills = 0;
	    witherKills = 0;
	    fishCaught = 0;
	}
	
	public MobStats(long playersKilled, long mobsKilled, long enderDragonKills, long witherKills, long fishcaught) {
		this.playersKilled = playersKilled;
		this.mobsKilled = mobsKilled;
		this.enderDragonKills = enderDragonKills;
		this.witherKills = witherKills;
		this.fishCaught = fishcaught;
	}
	
	public long getPlayersKilled() {
		return playersKilled;
	}
	
	public long getMobsKilled() {
		return mobsKilled;
	}
	
	public long getEnderDragonKills() {
		return enderDragonKills;
	}
	
	public long getWitherKills() {
		return witherKills;
	}
	
	public long getFishCaught() {
		return fishCaught;
	}
	
	public long killPlayer() {
		return playersKilled++;
	}
	
	public long killMob() {
		return mobsKilled++;
	}
	
	public long killEnderDragon() {
		return enderDragonKills++;
	}
	
	public long killWither() {
		return witherKills++;
	}
	
	public long fishCaught() {
		return fishCaught++;
	}
	
	public void setPlayersKilled(long numOfPlayers) {
		playersKilled = numOfPlayers;
	}
	
	public void setMobsKilled(long numOfMobs) {
		mobsKilled = numOfMobs;
	}
	
	public void setEnderDragonKills(long numOfEnderDragons) {
		enderDragonKills = numOfEnderDragons;
	}
	
	public void setWitherKills(long numOfWithers) {
		witherKills = numOfWithers;
	}
	
	public void setFishCaught(long numOfFish) {
		fishCaught = numOfFish;
	}
}
