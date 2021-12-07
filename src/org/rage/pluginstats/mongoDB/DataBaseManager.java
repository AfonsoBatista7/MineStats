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
import org.rage.pluginstats.Main;
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
			     				.append(Stats.VERSIONS.getQuery(), Arrays.asList(Main.currentServer.getVersion()))
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
				log.log(Level.INFO, "[MineStats] - Se isto aparecer o Moisés é gay:", e);
			}
		}
		
		return serverManager.getPlayerStats(player.getUniqueId());
	}
	
	public void downloadFromDataBase(ServerPlayer pp, Document playerDoc) throws ParseException {
		synchronized (serverManager) {
			String[] time = playerDoc.getString(Stats.TIMEPLAYED.getQuery()).split(" ");
			int min = 0;
			if(time.length>2) min = Integer.parseInt(time[2]);
			
			pp.setName((String) playerDoc.getString(Stats.NAME.getQuery()));
			pp.setNameList(playerDoc.getList(Stats.NAMES.getQuery(), String.class));
			pp.setVersionList(playerDoc.getList(Stats.VERSIONS.getQuery(), String.class));
			
			pp.setBlockStats(new BlockStats(playerDoc.getLong(Stats.BLOCKSDEST.getQuery()), 
					playerDoc.getLong(Stats.BLOCKSPLA.getQuery()), 
					playerDoc.getLong(Stats.REDSTONEUSED.getQuery())));
			
			pp.setMobStats(new MobStats(playerDoc.getLong(Stats.KILLS.getQuery()),
					playerDoc.getLong(Stats.MOBKILLS.getQuery()), playerDoc.getLong(Stats.ENDERDRAGONKILLS.getQuery()),
					playerDoc.getLong(Stats.WITHERKILLS.getQuery()), playerDoc.getLong(Stats.FISHCAUGHT.getQuery())));
			
			
			pp.setMetersTraveled(playerDoc.getLong(Stats.TRAVELLED.getQuery()));
			pp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.LASTLOGIN.getQuery())));
			pp.setPlayerSince(new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.PLAYERSINCE.getQuery())));
			pp.setTimePlayed(Long.parseLong(time[0])*3600+min*60);
			pp.setDeaths(playerDoc.getLong(Stats.DEATHS.getQuery()));
			pp.setTimesLogin(playerDoc.getLong(Stats.TIMESLOGIN.getQuery()));
			pp.setSessionMarkTime(null);
			
			pp.setMedals(loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class)));
			
			serverManager.newPlayerOnServer(pp);
		}
	}
	
	public synchronized void uploadToDataBase(ServerPlayer ps) {
		MongoCollection<Document> collection = mongoDB.getCollection();
		
		collection.updateMany(Filters.eq(Stats.PLAYERID.getQuery(), ps.getPlayerID()),
			Updates.combine(
					Updates.set(Stats.VERSIONS.getQuery(), ps.getVersions()),
					Updates.set(Stats.ONLINE.getQuery(), ps.isOnline()),
					Updates.set(Stats.BLOCKSDEST.getQuery(), ps.getBlockStats().getBlocksDestroyed()),
					Updates.set(Stats.BLOCKSPLA.getQuery(), ps.getBlockStats().getBlocksPlaced()),
					Updates.set(Stats.KILLS.getQuery(), ps.getMobStats().getPlayersKilled()),
					Updates.set(Stats.MOBKILLS.getQuery(), ps.getMobStats().getMobsKilled()),
					Updates.set(Stats.TRAVELLED.getQuery(), ps.getMetersTraveled()),
					Updates.set(Stats.DEATHS.getQuery(), ps.getDeaths()),
					Updates.set(Stats.REDSTONEUSED.getQuery(), ps.getBlockStats().getRedstoneUsed()),
					Updates.set(Stats.ENDERDRAGONKILLS.getQuery(), ps.getMobStats().getEnderDragonKills()),
					Updates.set(Stats.WITHERKILLS.getQuery(), ps.getMobStats().getWitherKills()),
					Updates.set(Stats.FISHCAUGHT.getQuery(), ps.getMobStats().getFishCaught()),
					Updates.set(Stats.LASTLOGIN.getQuery(), ps.getLastLogin()),
					Updates.set(Stats.TIMEPLAYED.getQuery(), ps.getTotalPlaytime()),
					Updates.set(Stats.TIMESLOGIN.getQuery(), ps.getTimesLogin())
			)
		);
	}
	
	public void newMedalOnDataBase(Medal newMedal, Player player) {	
		Document doc = newMedal.createMedalDoc();								//NEED TO TEST IF PLAYER IDs CHANGE WHEN NAME CHANGE
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), player.getUniqueId()), Updates.addToSet(Stats.MEDALS.getQuery(), doc));
	}
	
	public void newMedalOnDataBase(Document medalDoc, ServerPlayer sp) {	
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), sp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), medalDoc));
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
