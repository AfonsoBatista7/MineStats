package org.rage.pluginstats.player;

import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.stats.BlockStats;
import org.rage.pluginstats.stats.MobStats;
import org.rage.pluginstats.stats.Stat;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Afonso Batista
 * 2021
 */
public class PlayerProfile {
	
	protected UUID playerID;
	protected String name;

	private List<String> names, versionsPlayed;
	
	private Medal[] medals;
	private Stat[] stats;
	
	protected boolean online;
	
	protected BlockStats blockStats;
	protected MobStats mobStats;
	
	protected long metersTraveled,
				   kilometer,
				   timePlayed,
				   deaths,
				   timesLogin;		   
	
	protected Date lastLogin,
				   playerSince;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
	public PlayerProfile(UUID playerID) {
		
		blockStats = new BlockStats();
		mobStats = new MobStats();
		
		names = new ArrayList<String>();
		versionsPlayed = new ArrayList<String>();
		
		medals = new Medal[Medals.values().length];
		stats = new Stat[Stats.values().length];
		
		this.playerID = playerID;
		
	}
	
	public BlockStats getBlockStats() {
		return blockStats;
	}
	
	public void setBlockStats(BlockStats blockStats) {
		this.blockStats = blockStats;
	}
	
	public MobStats getMobStats() {
		return mobStats;
	}
	
	public void setMobStats(MobStats mobStats) {
		this.mobStats = mobStats;
	}
	
	public List<String> getVersions() {
		return versionsPlayed;
	}
	
	public void setVersionList(List<String> versionList) {
		versionsPlayed = versionList;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public UUID getPlayerID() {
		return playerID;
	}
	
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	public void setPlayerSince(Date playerSince) {
		this.playerSince = playerSince;
	}
	
	public void setTimePlayed(long timePlayed) {
		this.timePlayed = timePlayed;
	}
	
	public void setDeaths(long numDeaths) {
		deaths = numDeaths;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String playerName) {
		name = playerName;
	}
	
	public List<String> getAllNames() {
		return names;
	}
	
	public void setNameList(List<String> allNames) {
		names = allNames;
	}
	
	public Medal[] getMedals() {
		return medals;
	}
	
	public void setMedals(Medal[] medals) {
		this.medals = medals;
	}
	
	public Stat[] getStatsArray() {
		return stats;
	}
	
	public int getNumVersions() {
		return versionsPlayed.size();
	}
	
	public Stat getStat(Stats stat) {
		return stats[stat.getIndex()];
	}
	
	public boolean haveMedal(Medals medal) {
		return medals[medal.getIndex()]!=null;
	}
	
	public Medal getMedalByMedal(Medals medal) {
		return medals[medal.getIndex()];
	}
	
	public Medal getMedalByNumber(int medalNum) {
		return medals[medalNum];
	}
	
	public void newMedal(Medal medal) {
		medals[medal.getMedal().getIndex()] = medal; 
	}
	
	
	public long getTimesLogin() {
		return timesLogin;
	}
	
	public void setTimesLogin(long timesLogin) {
		this.timesLogin = timesLogin;
	}
	
	public long getMetersTraveled() {
		return metersTraveled;
	}
	
	public void setMetersTraveled(long meters) {
		metersTraveled = meters;
	}
	
	public long getDeaths() {
		return deaths;
	}
	
	public long getTimePlayed() {
		return timePlayed;
	}
	
	public long getKilometer() {
		return kilometer;
	}
	
	/**
	 * @return true if the player have all medals and false if not
	 */
	public boolean haveAllMedalsGod() {
		Medal medal;
		
		for(int i=0; i<Medals.values().length; i++) {
			medal = medals[i];
			if(medal==null || medal.getMedalLevel()!=MLevel.GOD) return false;
		}
		
		return true;
	}
	
	public String getPlayerSince() {
		return playerSince != null ? formatter.format(playerSince) : formatter.format(new Date());
	}
	
	public String getLastLogin() {
		return lastLogin != null ? formatter.format(lastLogin) : formatter.format(new Date());
	}
	
	public Date getPlayerSinceDate() {
		return playerSince;
	}
	
	public Date getLastLoginDate() {
		return lastLogin;
	}

	public String getTotalPlaytime() {
		return Util.secondsToTimestamp(timePlayed);
	}
	
	public String getTotalPlaytimeSeconds() {
		return Long.toString(timePlayed);
	}
	
	public String getSessionPlaytime() {
		if(isOnline()) {
			long seccondsInSession = (new Date().getTime() - lastLogin.getTime()) / 1000;
			return Util.secondsToTimestamp(seccondsInSession);
		} else return "-1";
	}
	
	public String getSessionPlaytimeSeconds() {
		if(isOnline()) {
			long secondsInSession = (new Date().getTime() - lastLogin.getTime()) / 1000;
			return Long.toString(secondsInSession);
			
		} else return "-1";
	}
	
	public boolean addNewVersion(String version) {
		if(!versionsPlayed.contains(version)) {
			versionsPlayed.add(version);
			return true;
		}
		
		return false;
		
	}
	
	public void putStats() {
		//TODO FAZER OS STATS FICAREM GOOD CODE PARA NAO PRECISAR DE METER 50000 LINHAS DE CODIGO SEMPRE QUE ATUALIZO TODOS OS STATS.
	}
}
