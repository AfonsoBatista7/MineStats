package org.rage.pluginstats.listeners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021
 */
public class ListenersController {
	
	private HashMap<UUID, PlayerProfile> stats;
	private DataBase mongoDB;
	private Logger log;
	private String currentVertion;
	
	public ListenersController(HashMap<UUID, PlayerProfile> stats, DataBase mongoDB, Logger log) {
		this.stats = stats;
		this.mongoDB = mongoDB;
		this.log = log;
		this.currentVertion = getServerVersion();
	}
	
	public String getServerVersion() {
		String version = Main.currentServer.getVersion();
		int start = version.indexOf("MC: ") + 4;
		int end = version.length() - 1;
		return version.substring(start, end);
	}
	
	public Document createMedalDoc(Medal medal) {
		return new Document(Stats.MEDALNAME.getQuery(), medal.getMedal().toString())
							.append(Stats.MEDALLEVEL.getQuery(), medal.getMedalLevel().toString());
	}
	
	public void newMedal(Medals medal, Player player) {
		PlayerProfile pp = getPlayerStats(player);
		MongoCollection<Document> collection = mongoDB.getCollection();
			
		if(!pp.haveMedal(medal)) {
			Medal newMedal = new Medal(medal);
			pp.newMedal(newMedal);
			Document doc = createMedalDoc(newMedal);
			collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), doc));
			
			newMedalEffect(player, pp.getMedal(medal).getMedalLevel());
			Bukkit.broadcastMessage(
					Util.chat("&b[MineStats]&7 - &a<player>&7, received the &c<medalName> &6<level>&7 Medal!!! :D.".replace("<player>", player.getName())
														   													   	   .replace("<medalName>", medal.toString())
														   													   	   .replace("<level>", medal.getMedalLevel().toString())));
		}
	}
	
	public void newMedalEffect(Player player, MLevel level) {
		Location location = player.getLocation();
		player.playSound(location, Sound.LEVEL_UP, level.getSoudLevel(), 2);
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 2020);
		
	}
	
	public void medalCheck(Medals medal, long stat, Player player, PlayerProfile pp) {		
			MongoCollection<Document> collection = mongoDB.getCollection();
			boolean haveTrasition = false;
			
			if(!pp.haveMedal(medal)) {
				if(stat >= medal.getTransition()) {
					Medal newMedal = new Medal(medal);
					pp.newMedal(newMedal);
					
					Document doc = createMedalDoc(newMedal);
					collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), doc));
					
					haveTrasition = pp.getMedal(medal).checkLevelTransition(stat);
					
				} else return;
			} else {
				haveTrasition = pp.getMedal(medal).checkLevelTransition(stat);
				if(!haveTrasition) return;
			}
			
			if(haveTrasition) {
				
				Document playerDoc = mongoDB.getPlayer(pp.getPlayerID());
				Object[] list = playerDoc.getList(Stats.MEDALS.getQuery(), Document.class).toArray();
				List<Document> finalList = new ArrayList<Document>(list.length);
				for(int i=0; i<list.length; i++) {
					Document document = (Document) list[i];
					finalList.add(i, document);
					if(document.getString(Stats.MEDALNAME.getQuery()).equals(medal.toString())) {
						finalList.remove(i); finalList.add(i,createMedalDoc(new Medal(medal, pp.getMedal(medal).getMedalLevel()))); 
					}
				}
				
				collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.set(Stats.MEDALS.getQuery(), finalList));
				
				/*collection.updateOne(Filters.and(Filters.eq(Main.PLAYERID, pp.getPlayerID()),
												 Filters.eq(Main.MEDALS+"."+Main.MEDALNAME, medal.toString())),
												 Updates.set(Main.MEDALLEVEL, pp.getMedal(medal).getMedalLevel().toString())); */
				
				Bukkit.broadcastMessage(
						Util.chat("&b[&a<player>&b]&7 - &aLEVEL UP!").replace("<player>", pp.getName()));
			}
			
			if(player!=null) newMedalEffect(player, pp.getMedal(medal).getMedalLevel());
				
			
			Bukkit.broadcastMessage(
					Util.chat("&b[MineStats]&7 - &a<player>&7 achieved <statCounter> <statName> and received the &c<medalName> &6<level>&7 Medal!!! :D.".replace("<player>", pp.getName())
																																					    .replace("<statCounter>", String.valueOf(stat))
																																						.replace("<statName>",medal.getStatName())
																																					    .replace("<medalName>", medal.toString())
																																						.replace("<level>", pp.getMedal(medal).getMedalLevel().toString())));
			if(pp.haveAllMedalsGod()) newMedal(Medals.GOD, player);
		
	}
	
	@SuppressWarnings("deprecation")
	public void playerJoin(Player player) {
		PlayerProfile pp = getPlayerStats(player);
		pp.sessionMarkTime = new Date();
		pp.lastLogIn = new Date();
		pp.online = true;
		mongoDB.getCollection().updateOne(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.set(Stats.ONLINE.getQuery(), true));
		pp.startPersisting();
		
		if(pp.addNewVersion(currentVertion)) medalCheck(Medals.TIMETRAVELLER, pp.versionsPlayed.size(), player, pp);
		medalCheck(Medals.VETERAN, pp.lastLogIn.getYear() - pp.playerSince.getYear(), player, pp);
		medalCheck(Medals.LOGINNER, pp.timesLogin++, player, pp);
			
	}
	
	public void playerQuit(Player player) {
		PlayerProfile pp = getPlayerStats(player);
		pp.flushSessionPlaytime();
		pp.sessionMarkTime = null;
		pp.stopPersisting();
		pp.online = false;
		uploadToDataBase(pp);
		
	}
	
	public void logInOnlinePlayers() {
		for(Player player : Main.currentServer.getOnlinePlayers()) playerJoin(player);
	}
	
	public void logOutAllPlayers() {
		synchronized(stats) {
			for(PlayerProfile pp : stats.values()) {
				pp.flushSessionPlaytime();
				pp.sessionMarkTime = null;
				pp.online = false;
			}
		}
		uploadAll();
	}
	
	public void playerMove(Player player) {
		PlayerProfile pp = getPlayerStats(player);
		pp.kilometer++;
		pp.metersTraveled++;
		if(pp.kilometer==1000) {
			pp.kilometer=0;
			medalCheck(Medals.WORLDTRAVELLER, pp.metersTraveled, player, pp);
		}
		
	}
	
	public void playerKick(Player player) {
		playerQuit(player);
	}
	
	public void placeBlock(Player player, Block block) {
		if(block.getType().getId() > 0) {
			PlayerProfile pp = getPlayerStats(player);
			medalCheck(Medals.BUILDER, pp.blocksPlaced++, player, pp);
			
			switch(block.getType()) {
			case REDSTONE:
			case REDSTONE_BLOCK:
			case REDSTONE_COMPARATOR:
			case REDSTONE_WIRE:
			case REDSTONE_ORE:
			case REDSTONE_LAMP_OFF:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
				medalCheck(Medals.REDSTONENGINEER, pp.redstoneUsed++, player, pp);
				break;
			default:
			}
		}
	}
	
	public void brakeBlock(Player player, Block block) {
		if(block.getType().getId() > 0) {
			PlayerProfile pp = getPlayerStats(player);
			medalCheck(Medals.DESTROYER, pp.blocksDestroyed++, player, pp);
		}
	}
	
	public void playerFishCaught(Player player, Entity caught) {
		PlayerProfile pp = getPlayerStats(player);
		medalCheck(Medals.FISHERMAN, pp.fishcaught++, player, pp);
	}
	
	public void die(Player player) {
		PlayerProfile pp = getPlayerStats(player);
		medalCheck(Medals.ZOMBIE, pp.deaths++, player, pp);
	}
	
	public void kill(Player player, Entity entity) {
		PlayerProfile pp = getPlayerStats(player);
		if(entity instanceof Player)
			medalCheck(Medals.PVPMASTER, pp.playersKilled++, player, pp);
		else {
			medalCheck(Medals.MOBSLAYER, pp.mobsKilled++, player, pp);
			
			switch(entity.getType()) {
			case ENDER_DRAGON:
				medalCheck(Medals.DRAGONSLAYER, pp.enderDragonKills++, player, pp);
				break;
			case WITHER:
				medalCheck(Medals.WITHERSLAYER, pp.witherKills++, player, pp);
				break;
			default:
			}
		}
	}
	
	public PlayerProfile getPlayerStats(Player player) {
		
		if(!stats.containsKey(player.getUniqueId())) {
			
			PlayerProfile pp = new PlayerProfile(player.getUniqueId(), this, mongoDB.getConfig());
			
			Document playerDoc = mongoDB.getPlayer(player.getUniqueId());
			
			if(playerDoc==null) playerDoc = newPlayer(player);
			
			try {
				downloadFromDataBase(pp, playerDoc);
			} catch (ParseException e) {
				log.log(Level.INFO, "[MineStats] - Se isto aparecer o Moisés é gay:", e);
			}
		}
		
		return stats.get(player.getUniqueId());	
	}
	
	public PlayerProfile getOfflinePlayerStats(UUID playerId) {
		return stats.get(playerId);
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
			     				.append(Stats.MEDALS.getQuery(), Arrays.asList(createMedalDoc(new Medal(Medals.NOSTALGIAPLAYER))))
								.append(Stats.ONLINE.getQuery(), player.isOnline());
		
		mongoDB.newDoc(playerDoc);
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Heyyy &a<player>&7! Bem vindo ao Minecraft Nostalgia :D.".replace("<player>", player.getName())));
						
		
		return playerDoc;
	}
	
	public void downloadFromDataBase(PlayerProfile pp, Document playerDoc) throws ParseException {
		synchronized (stats) {
			
			String[] time = playerDoc.getString(Stats.TIMEPLAYED.getQuery()).split(" ");
			int min = 0;
			if(time.length>2) min = Integer.parseInt(time[2]);
			
			pp.name = (String) playerDoc.getString(Stats.NAME.getQuery());
			pp.names = playerDoc.getList(Stats.NAMES.getQuery(), String.class);
			pp.versionsPlayed = playerDoc.getList(Stats.VERSIONS.getQuery(), String.class);
			pp.blocksDestroyed = playerDoc.getLong(Stats.BLOCKSDEST.getQuery()); 
			pp.blocksPlaced = playerDoc.getLong(Stats.BLOCKSPLA.getQuery());
			pp.playersKilled = playerDoc.getLong(Stats.KILLS.getQuery());
			pp.mobsKilled = playerDoc.getLong(Stats.MOBKILLS.getQuery());
			pp.metersTraveled = playerDoc.getLong(Stats.TRAVELLED.getQuery());
			pp.enderDragonKills = playerDoc.getLong(Stats.ENDERDRAGONKILLS.getQuery());
			pp.witherKills = playerDoc.getLong(Stats.WITHERKILLS.getQuery());
			pp.redstoneUsed = playerDoc.getLong(Stats.REDSTONEUSED.getQuery());
			pp.fishcaught = playerDoc.getLong(Stats.FISHCAUGHT.getQuery());
			pp.lastLogIn = new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.LASTLOGIN.getQuery()));
			pp.playerSince = new SimpleDateFormat("dd/MM/yyyy").parse(playerDoc.getString(Stats.PLAYERSINCE.getQuery()));
			pp.timePlayed = Long.parseLong(time[0])*3600+min*60;
			pp.deaths = playerDoc.getLong(Stats.DEATHS.getQuery());
			pp.timesLogin = playerDoc.getLong(Stats.TIMESLOGIN.getQuery());
			pp.sessionMarkTime = null;
			
			pp.medals = loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
			
			stats.put(pp.getPlayerID(), pp);
		}
	}
	
	public synchronized void uploadToDataBase(PlayerProfile ps) {
		MongoCollection<Document> collection = mongoDB.getCollection();
		
		collection.updateMany(Filters.eq(Stats.PLAYERID.getQuery(), ps.getPlayerID()),
			Updates.combine(
					Updates.set(Stats.VERSIONS.getQuery(), ps.getVersions()),
					Updates.set(Stats.ONLINE.getQuery(), ps.isOnline()),
					Updates.set(Stats.BLOCKSDEST.getQuery(), ps.getBlocksDestroyed()),
					Updates.set(Stats.BLOCKSPLA.getQuery(), ps.getBlocksPlaced()),
					Updates.set(Stats.KILLS.getQuery(), ps.getPlayersKilled()),
					Updates.set(Stats.MOBKILLS.getQuery(), ps.getMobsKilled()),
					Updates.set(Stats.TRAVELLED.getQuery(), ps.getMetersTraveled()),
					Updates.set(Stats.DEATHS.getQuery(), ps.getDeaths()),
					Updates.set(Stats.REDSTONEUSED.getQuery(), ps.getRedtoneUsed()),
					Updates.set(Stats.ENDERDRAGONKILLS.getQuery(), ps.getEnderDragonKills()),
					Updates.set(Stats.WITHERKILLS.getQuery(), ps.getWhitherKills()),
					Updates.set(Stats.FISHCAUGHT.getQuery(), ps.getFishCaught()),
					Updates.set(Stats.LASTLOGIN.getQuery(), ps.getLastLogin()),
					Updates.set(Stats.TIMEPLAYED.getQuery(), ps.getTotalPlaytime()),
					Updates.set(Stats.TIMESLOGIN.getQuery(), ps.getTimesLogin())
			)
		);
	}
	
	public void uploadAll() {
		for(PlayerProfile ps : stats.values()) {
			ps.flushSessionPlaytime();
			ps.stopPersisting();
			uploadToDataBase(ps);
			ps.startPersisting();
		}
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
	
	public PlayerProfile getPlayerFromHashMap(UUID player) {
		return stats.get(player);
	}
	
	public PlayerProfile deleteFromHashMap(UUID player) {
		return stats.remove(player);
	}
	
	public boolean alreadyHadMedal(Medals medal, Document doc) {
		List<Document> medalDoc = doc.getList(Stats.MEDALS.getQuery(), Document.class);
		for(Document medals: medalDoc)
			if(medals.getString(Stats.MEDALNAME.getQuery()).equals(medal.toString())) return true;
		return false;
		
	}
	
}
