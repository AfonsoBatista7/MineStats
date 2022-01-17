package org.rage.pluginstats.mongoDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.BlockStats;
import org.rage.pluginstats.stats.MobStats;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021
 */
public class DataBaseManager {
	
	private Logger log;
	private DataBase mongoDB;
	private ServerManager serverManager;
	
	public DataBaseManager(DataBase mongoDB, Logger log, ServerManager serverManager) {
		this.log = log;
		this.mongoDB = mongoDB;
		this.serverManager = serverManager;
	}
	
	public Document newPlayer(Player player) {
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		Document playerDoc = new Document(Stats.PLAYERID.getQuery(), player.getUniqueId())
			     				.append(Stats.NAME.getQuery(), player.getName())
			     				.append(Stats.NAMES.getQuery(), Arrays.asList(player.getName()))
			     				.append(Stats.BLOCKSDEST.getQuery(), 0L)
			     				.append(Stats.BLOCKSPLA.getQuery(), 0L)
			     				.append(Stats.KILLS.getQuery(), 0L)
			     				.append(Stats.MOBKILLS.getQuery(), 0L)
			     				.append(Stats.TRAVELLED.getQuery(), 0L)
			     				.append(Stats.DEATHS.getQuery(), 0L)
			     				.append(Stats.TIMESLOGIN.getQuery(), 0L)
			     				.append(Stats.REDSTONEUSED.getQuery(), 0L)
			     				.append(Stats.ENDERDRAGONKILLS.getQuery(), 0L)
			     				.append(Stats.WITHERKILLS.getQuery(), 0L)
			     				.append(Stats.FISHCAUGHT.getQuery(), 0L)
			     				.append(Stats.LASTLOGIN.getQuery(), formatter.format(new Date()))
			     				.append(Stats.PLAYERSINCE.getQuery(), formatter.format(new Date()))
			     				.append(Stats.TIMEPLAYED.getQuery(), "0 Hr 0 Min")
			     				.append(Stats.VERSIONS.getQuery(), Arrays.asList(serverManager.getCurrentServerVersion()))
			     				.append(Stats.MEDALS.getQuery(), Arrays.asList(new Medal(Medals.NOSTALGIAPLAYER).createMedalDoc()))
								.append(Stats.ONLINE.getQuery(), player.isOnline());
		
		mongoDB.newDoc(playerDoc);
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Heyyy &a<player>&7! Bem vindo ao Minecraft Nostalgia :D.".replace("<player>", player.getName())));
						
		
		return playerDoc;
	}
	
	public ServerPlayer getPlayerStats(Player player) {
		
		if(!serverManager.playerAlreadyInServer(player.getUniqueId())) {
			
			ServerPlayer pp = new ServerPlayer(player.getUniqueId(), this);
			
			Document playerDoc = mongoDB.getPlayer(player.getUniqueId());
			
			if(playerDoc==null) playerDoc = newPlayer(player);
			
			try {
				downloadFromDataBase(pp, playerDoc);
			} catch (ParseException e) {
				log.log(Level.SEVERE, "[MineStats] - An error has occurred:", e.fillInStackTrace());
			}
		}
		
		return serverManager.getPlayerStats(player.getUniqueId());
	}
	
	/**
	 * Downloads all stats from <playerDoc> in database to <sp>
	 * 
	 * @param sp - Local Player
	 * @param playerDoc - Document with all player stats 
	 */
	public void downloadFromDataBase(ServerPlayer sp, Document playerDoc) throws ParseException {
		synchronized (serverManager) {
			String[] time = playerDoc.getString(Stats.TIMEPLAYED.getQuery()).split(" ");
			int min = 0;
			if(time.length>2) min = Integer.parseInt(time[2]);
			
			sp.setName((String) playerDoc.getString(Stats.NAME.getQuery()));
			sp.setNameList(playerDoc.getList(Stats.NAMES.getQuery(), String.class));
			sp.setVersionList(playerDoc.getList(Stats.VERSIONS.getQuery(), String.class));
			
			sp.setBlockStats(new BlockStats(playerDoc.getLong(Stats.BLOCKSDEST.getQuery()), 
					playerDoc.getLong(Stats.BLOCKSPLA.getQuery()), 
					playerDoc.getLong(Stats.REDSTONEUSED.getQuery())));
			
			sp.setMobStats(new MobStats(playerDoc.getLong(Stats.KILLS.getQuery()),
					playerDoc.getLong(Stats.MOBKILLS.getQuery()), playerDoc.getLong(Stats.ENDERDRAGONKILLS.getQuery()),
					playerDoc.getLong(Stats.WITHERKILLS.getQuery()), playerDoc.getLong(Stats.FISHCAUGHT.getQuery())));
			
			
			sp.setMetersTraveled(playerDoc.getLong(Stats.TRAVELLED.getQuery()));
			sp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.LASTLOGIN.getQuery())));
			sp.setPlayerSince(new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.PLAYERSINCE.getQuery())));
			sp.setTimePlayed(Long.parseLong(time[0])*3600+min*60);
			sp.setDeaths(playerDoc.getLong(Stats.DEATHS.getQuery()));
			sp.setTimesLogin(playerDoc.getLong(Stats.TIMESLOGIN.getQuery()));
			sp.setSessionMarkTime(null);
			
			sp.setMedals(loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class)));
			
			serverManager.newPlayerOnServer(sp);
		}
	}
	
	/**
	 * Uploads all <sp> stats to the database, updating it.
	 * 
	 * @param sp - Local Player
	 */
	public synchronized void uploadToDataBase(ServerPlayer sp) {
		MongoCollection<Document> collection = mongoDB.getCollection();
		
		collection.updateMany(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()),
			Updates.combine(
					Updates.set(Stats.VERSIONS.getQuery(), sp.getVersions()),
					Updates.set(Stats.ONLINE.getQuery(), sp.isOnline()),
					Updates.set(Stats.BLOCKSDEST.getQuery(), sp.getBlockStats().getBlocksDestroyed()),
					Updates.set(Stats.BLOCKSPLA.getQuery(), sp.getBlockStats().getBlocksPlaced()),
					Updates.set(Stats.KILLS.getQuery(), sp.getMobStats().getPlayersKilled()),
					Updates.set(Stats.MOBKILLS.getQuery(), sp.getMobStats().getMobsKilled()),
					Updates.set(Stats.TRAVELLED.getQuery(), sp.getMetersTraveled()),
					Updates.set(Stats.DEATHS.getQuery(), sp.getDeaths()),
					Updates.set(Stats.REDSTONEUSED.getQuery(), sp.getBlockStats().getRedstoneUsed()),
					Updates.set(Stats.ENDERDRAGONKILLS.getQuery(), sp.getMobStats().getEnderDragonKills()),
					Updates.set(Stats.WITHERKILLS.getQuery(), sp.getMobStats().getWitherKills()),
					Updates.set(Stats.FISHCAUGHT.getQuery(), sp.getMobStats().getFishCaught()),
					Updates.set(Stats.LASTLOGIN.getQuery(), sp.getLastLogin()),
					Updates.set(Stats.TIMEPLAYED.getQuery(), sp.getTotalPlaytime()),
					Updates.set(Stats.TIMESLOGIN.getQuery(), sp.getTimesLogin())
			)
		);
	}
	
	public void levelUpMedal(Player player, Medal medal) {
		Document playerDoc = mongoDB.getPlayer(player.getUniqueId());
		Object[] list = playerDoc.getList(Stats.MEDALS.getQuery(), Document.class).toArray();
		List<Document> finalList = new ArrayList<Document>(list.length);
		
		for(int i=0; i<list.length; i++) {
			Document document = (Document) list[i];
			finalList.add(i, document);
			if(document.getString(Stats.MEDALNAME.getQuery()).equals(medal.getMedal().toString())) {
				finalList.remove(i); finalList.add(i, medal.createMedalDoc()); 
			}
		}
		
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), player.getUniqueId()), Updates.set(Stats.MEDALS.getQuery(), finalList));
	}
	
	/**
	 * Loads all player <medals> from data base and convert them to an array of Medals 
	 * 
	 * @param medals - All player medals on data base.
	 * @return the converted list of medals to save locally.
	 */
	public Medal[] loadMedals(List<Document> medals) {
		
		Medal[] newList = new Medal[Medals.values().length];
		
		Medals medal; MLevel level;
		for(Document doc: medals) {
			
			medal = Medals.valueOf(doc.getString(Stats.MEDALNAME.getQuery()));
			level = MLevel.valueOf(doc.getString(Stats.MEDALLEVEL.getQuery()));
			
			newList[medal.getIndex()] = new Medal(medal, level) ;
		}
		
		return newList;
	}
	
	public boolean alreadyHadMedal(Medals medal, Document doc) {
		List<Document> medalDoc = doc.getList(Stats.MEDALS.getQuery(), Document.class);
		for(Document medals: medalDoc)
			if(medals.getString(Stats.MEDALNAME.getQuery()).equals(medal.toString())) return true;
		return false;
		
	}
	
	public void newMedalOnDataBase(Medal newMedal, Player player) {	
		Document doc = newMedal.createMedalDoc();								//NEED TO TEST IF PLAYER IDs CHANGE WHEN NAME CHANGED
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), player.getUniqueId()), Updates.addToSet(Stats.MEDALS.getQuery(), doc));
	}
	
	public void newMedalOnDataBase(Document medalDoc, ServerPlayer sp) {	
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), medalDoc));
	}
	
	public void updateStat(Bson filter, Bson update) {
		mongoDB.getCollection().updateOne(filter, update);
	}
	
	public void updateMultStats(Bson filter, Bson update) {
		mongoDB.getCollection().updateMany(filter, update);
	}
	
	public void deleteDoc(Bson filter) {
		mongoDB.getCollection().deleteOne(filter);
	}
	
	public Document getPlayerByName(String name) {
		return mongoDB.getPlayerByName(name);
	}
	
	public Document getPlayer(UUID playerId) {
		return mongoDB.getPlayer(playerId);
	}
	
	public FileConfiguration getConfig() {
		return mongoDB.getConfig();
	}
	
	public MongoCursor<Document> getCollectionIterator() {
		return mongoDB.getCollection().find().iterator();
	}
	
}
