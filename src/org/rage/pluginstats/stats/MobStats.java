package org.rage.pluginstats.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class MobStats {
	
	private HashMap<String, Mob> mobsKilled;
	private long totalMobsKilled,
				 playersKilled,
	   			 enderDragonKills,
			     witherKills,
			     fishCaught;
	
	public MobStats() {
		playersKilled = 0;
		totalMobsKilled = 0;
		enderDragonKills = 0;
	    witherKills = 0;
	    fishCaught = 0;
	    mobsKilled = new HashMap<>();
	}
	
	public MobStats(long playersKilled, long numMobsKilled, long enderDragonKills, long witherKills, long fishcaught, HashMap<String, Mob> mobsKilled) {
		this.playersKilled = playersKilled;
		this.totalMobsKilled = numMobsKilled;
		this.enderDragonKills = enderDragonKills;
		this.witherKills = witherKills;
		this.fishCaught = fishcaught;
		this.mobsKilled = mobsKilled;
	}
	
	public long getPlayersKilled() {
		return playersKilled;
	}
	
	public long getTotalNumMobsKilled() {
		return totalMobsKilled;
	}
	
	public Mob getNumMobsKilledByName(String mobName) {
		return mobsKilled.get(mobName);
	}
	
	protected HashMap<String, Mob> getNumMobsKilled() {
		return mobsKilled;
	}
	
	public List<Document> getNumMobsKilledList() {
		List<Document> mobsKilledDocs = new ArrayList<Document>(mobsKilled.size());
		
		for(Mob mob : mobsKilled.values())
			mobsKilledDocs.add(mob.createMobDocument());
		
		return mobsKilledDocs;
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
	
	public long killMob(int mobId, String mobName) {
		Mob mobKilled = getNumMobsKilledByName(mobName);
		
		if(mobKilled == null)
			mobKilled = new Mob(mobId, mobName);	
		 
		mobKilled.incNumMobKilled();
		
		mobsKilled.put(mobName, mobKilled);	
		
		return totalMobsKilled++;
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
	
	public void setNumMobsKilled(long numOfMobs) {
		totalMobsKilled = numOfMobs;
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
