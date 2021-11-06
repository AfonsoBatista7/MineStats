package org.rage.pluginstats.mongoDB;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.stats.Stat;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author Afonso Batista
 * 2021
 */
public class PlayerProfile {
	
	private UUID playerID;
	public String name;
	
	private final long TIME_BETWEEN_SAVES;

	public List<String> names, versionsPlayed;
	
	public Medal[] medals;
	public Stat[] stats;
	
	public boolean online;
	
	public long blocksDestroyed,
				blocksPlaced,
				redstoneUsed,
				metersTraveled,
				kilometer,
				timePlayed,
				playersKilled,
				mobsKilled,
				deaths,
				timesLogin,
				enderDragonKills,
				witherKills,
				fishcaught;
	
	public Date lastLogIn,
				playerSince,
				sessionMarkTime;
	
	private Timer t;
	
	private ListenersController controller;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
	public PlayerProfile(UUID playerID, ListenersController controller, FileConfiguration config) {
		
		TIME_BETWEEN_SAVES = config.getInt("timeBetweenSaves");
		
		names = new ArrayList<String>();
		versionsPlayed = new ArrayList<String>();
		
		medals = new Medal[Medals.values().length];
		stats = new Stat[Stats.values().length];
		
		this.controller = controller;
		this.playerID = playerID;
		t = new Timer();
	}
	
	public void flushSessionPlaytime() {
		if(sessionMarkTime != null)
		{
			Date now = new Date();
			long dif = (now.getTime() - sessionMarkTime.getTime()) / 1000;
			timePlayed += dif;
			sessionMarkTime = now;
		}
	}
	
	public long getFishCaught() {
		return fishcaught;
	}
	
	public List<String> getVersions() {
		return versionsPlayed;
	}
	
	public long getRedtoneUsed() {
		return redstoneUsed;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public UUID getPlayerID() {
		return playerID;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getAllNames() {
		return names;
	}
	
	public Medal[] getMedals() {
		return medals;
	}
	
	public Stat[] getStatsArray() {
		return stats;
	}
	
	public Stat getStat(Stats stat) {
		return stats[stat.getIndex()];
	}
	
	public boolean haveMedal(Medals medal) {
		return medals[medal.getIndex()]!=null;
	}
	
	public Medal getMedal(Medals medal) {
		return medals[medal.getIndex()];
	}
	
	public void newMedal(Medal medal) {
		medals[medal.getMedal().getIndex()] = medal; 
	}
	
	public long getEnderDragonKills() {
		return enderDragonKills;
	}
	
	public long getWhitherKills() {
		return witherKills;
	}
	
	public long getTimesLogin() {
		return timesLogin;
	}
	
	public long getBlocksDestroyed() {
		return blocksDestroyed;
	}
	
	public long getBlocksPlaced() {
		return blocksPlaced;
	}
	
	public long getPlayersKilled() {
		return playersKilled;
	}
	
	public long getMobsKilled() {
		return mobsKilled;
	}
	
	public long getMetersTraveled() {
		return metersTraveled;
	}
	
	public long getDeaths() {
		return deaths;
	}
	
	public long getTimePlayed() {
		return timePlayed;
	}
	
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
		return lastLogIn != null ? formatter.format(lastLogIn) : formatter.format(new Date());
	}

	public String getTotalPlaytime() {
		return Util.secondsToTimestamp(timePlayed);
	}
	
	public String getTotalPlaytimeSeconds() {
		return Long.toString(timePlayed);
	}
	
	public String getSessionPlaytime() {
		if(isOnline()) {
			long seccondsInSession = (new Date().getTime() - lastLogIn.getTime()) / 1000;
			return Util.secondsToTimestamp(seccondsInSession);
		} else return "-1";
	}
	
	public String getSessionPlaytimeSeconds() {
		if(isOnline()) {
			long secondsInSession = (new Date().getTime() - lastLogIn.getTime()) / 1000;
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
	
	// Utility methods
	public void startPersisting() {
		t.scheduleAtFixedRate(new StatsTimerTask(), TIME_BETWEEN_SAVES, TIME_BETWEEN_SAVES);
	}
	
	public void stopPersisting() {
		t.cancel();
		t = new Timer();
	}
	
	public void uploadToDataBase() {
		controller.uploadToDataBase(this);
	}
	
	public void playerMedalCheck(Medals medal, long stat, Player player) {
		controller.medalCheck(medal, stat, player, this);
	}
	
	private class StatsTimerTask extends TimerTask {
		
		@Override
		public void run() {
			flushSessionPlaytime();
			playerMedalCheck(Medals.TIMEWALKER, timePlayed/3600, Main.currentServer.getPlayer(name));
			uploadToDataBase();
		}
	}
}
