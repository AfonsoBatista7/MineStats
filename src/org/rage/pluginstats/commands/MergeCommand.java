package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * If a player have changed is name, you can execute this command to merge two player profiles. 
 * @author Afonso Batista
 * 2021 - 2022
 */
public class MergeCommand implements CommandExecutor{

	private DataBaseManager mongoDB;
	private ServerManager serverMan;

	public MergeCommand(DataBaseManager mongoDB, ServerManager serverMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
			
		Document playerDoc1, playerDoc2;
		
		
		switch(args.length) {
			case 1:
				
				if(mongoDB.getPlayerByName(args[0])==null) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
					return false;
				}
				
				MongoCursor<Document> iterator = mongoDB.getAllPlayersByName(args[0]);
				
				playerDoc1 = iterator.next();
				sender.sendMessage(String.format(Util.chat("&b[MineStats]&7 - ID 1 -> %s"), playerDoc1.get(Stats.PLAYERID.getQuery())));
				
				
				if(!iterator.hasNext()) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't have duplicate Documents."));
					return false;
				}
					
				playerDoc2 = iterator.next();
				sender.sendMessage(String.format(Util.chat("&b[MineStats]&7 - ID 2 -> %s"), playerDoc2.get(Stats.PLAYERID.getQuery())));
				
				
				break;
			case 2:
				
				playerDoc1 = mongoDB.getPlayerByName(args[0]);
				
				playerDoc2 = mongoDB.getPlayerByName(args[1]);
				
				break;
			default:
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify two or one player with two dublicate documents with the same name."));
				return false;
		}
		
			
		if(playerDoc1==null || playerDoc2==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - One or more players doesn't exist on DataBase."));
			return false;
		}
		
		if(playerDoc1.equals(playerDoc2)) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - The two players are the same :/ ."));
			return false;
		}
			
		UUID playerId = getUUIDRecentPlayer(playerDoc1 ,playerDoc2);
		try {
			mongoDB.deleteDoc(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc2.get(Stats.PLAYERID.getQuery())));
			
			mongoDB.updateMultStats(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc1.get(Stats.PLAYERID.getQuery())),
					Updates.combine(
							Updates.set(Stats.PLAYERID.getQuery(), playerId),
							Updates.set(Stats.ONLINE.getQuery(), playerDoc1.getBoolean(Stats.ONLINE.getQuery()) || playerDoc2.getBoolean(Stats.ONLINE.getQuery())),
							Updates.inc(Stats.BLOCKSDEST.getQuery(), playerDoc2.getLong(Stats.BLOCKSDEST.getQuery())),
							Updates.inc(Stats.BLOCKSPLA.getQuery(), playerDoc2.getLong(Stats.BLOCKSPLA.getQuery())),
							Updates.inc(Stats.BLOCKSMINED.getQuery(), playerDoc2.getLong(Stats.BLOCKSMINED.getQuery())),
							Updates.inc(Stats.KILLS.getQuery(), playerDoc2.getLong(Stats.KILLS.getQuery())),
							Updates.inc(Stats.MOBKILLS.getQuery(), playerDoc2.getLong(Stats.MOBKILLS.getQuery())),
							Updates.inc(Stats.TRAVELLED.getQuery(), playerDoc2.getLong(Stats.TRAVELLED.getQuery())),
							Updates.inc(Stats.DEATHS.getQuery(), playerDoc2.getLong(Stats.DEATHS.getQuery())),
							Updates.inc(Stats.TIMESLOGIN.getQuery(), playerDoc2.getLong(Stats.TIMESLOGIN.getQuery())),
							Updates.inc(Stats.FISHCAUGHT.getQuery(), playerDoc2.getLong(Stats.FISHCAUGHT.getQuery())),
							Updates.inc(Stats.REDSTONEUSED.getQuery(), playerDoc2.getLong(Stats.REDSTONEUSED.getQuery())),
							Updates.max(Stats.LASTLOGIN.getQuery(), playerDoc2.getString(Stats.LASTLOGIN.getQuery())),
							Updates.min(Stats.PLAYERSINCE.getQuery(), playerDoc2.getString(Stats.PLAYERSINCE.getQuery())),
							Updates.set(Stats.TIMEPLAYED.getQuery(), mergeTimePlayed(playerDoc1.getString(Stats.TIMEPLAYED.getQuery()), playerDoc2.getString(Stats.TIMEPLAYED.getQuery()))),
							Updates.addEachToSet(Stats.MEDALS.getQuery(), playerDoc2.getList(Stats.MEDALS.getQuery(), Document.class)),
							Updates.addEachToSet(Stats.VERSIONS.getQuery(),playerDoc2.getList(Stats.VERSIONS.getQuery(), String.class))
				)
			);
			
			mongoDB.updateStat(Filters.eq(Stats.PLAYERID.getQuery(), playerId),
					Updates.set(Stats.MEDALS.getQuery(), getMedalList(playerDoc1.getList(Stats.MEDALS.getQuery(), Document.class))));
			
		} catch(Exception e) {
			
			Bukkit.broadcastMessage(Util.chat("&b[MineStats]&7 - An ERROR occurred while merging..."));
			
			e.printStackTrace();
			return false;
		}
		
		serverMan.deleteFromHashMap((UUID) playerDoc2.get(Stats.PLAYERID.getQuery()));
		serverMan.deleteFromHashMap((UUID) playerDoc1.get(Stats.PLAYERID.getQuery()));
		
		playerDoc1 = mongoDB.getPlayer(playerId);
		
		try {
			mongoDB.downloadFromDataBase(new ServerPlayer(playerId, mongoDB), playerDoc1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Player &a<player1>&7 and &a<player2>&7 now are one B)."
						.replace("<player1>", playerDoc1.getString(Stats.NAME.getQuery()))
						.replace("<player2>", playerDoc2.getString(Stats.NAME.getQuery()))));
		
		//Player player = Main.currentServer.getPlayer(playerDoc1.getString(Stats.NAME.getQuery()));
		
		//serverMan.getPlayerStats(playerId).medalCheck(Medals.NAMEHOLDER, playerDoc1.getList(Stats.NAMES.getQuery(), String.class).size(), player);
		return true;
	}
	
	private String mergeTimePlayed(String timePlayed1, String timePlayed2) {
		String[] time1 = timePlayed1.split(" "),
				 time2 = timePlayed2.split(" ");
		
		int min1 = 0, min2 = 0;
		
		if(time1.length>2) min1 = Integer.parseInt(time1[2]);
		if(time2.length>2) min2 = Integer.parseInt(time2[2]);
		
		long seconds = (Long.parseLong(time1[0])*3600+min1*60) +
					   (Long.parseLong(time2[0])*3600+min2*60);
		
		return Util.secondsToTimestamp(seconds);
	}
	
	private List<Document> getMedalList(List<Document> badMedals) {
		
		String medalName, level;
		List<Document> newList = badMedals;
		
		for(Document badDoc: badMedals) {
			medalName = badDoc.getString("medalName");
			level = badDoc.getString("medalLevel");
			newList = findAndRemoveMedal(medalName, level, badDoc, newList);
		}
		
		return newList;
	}
	
	private List<Document> findAndRemoveMedal(String medalName, String level, Document badDoc, List<Document> badMedals) {
		
		String medalName2, medalLevel2;
		List<Document> newList = badMedals;
		
		for(Document badDoc2: badMedals) {
			medalName2 = badDoc.getString("medalName");
			medalLevel2 = badDoc.getString("medalLevel");
			
			if(medalName.equals(medalName2) && !badDoc.equals(badDoc2)) {
				if(MLevel.valueOf(level).getNumber() > MLevel.valueOf(badDoc2.getString(medalLevel2)).getNumber()) {
					newList.remove(badDoc2);
					return newList;
				} else {
					newList.remove(badDoc);
					return newList;
				}
			}
		}
		return newList;
	}
	
	private UUID getUUIDRecentPlayer(Document playerDoc1, Document playerDoc2) {
		
		try { 
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy h:mm a"); 
			return formatter.parse(playerDoc1.getString(Stats.LASTLOGIN.getQuery())).compareTo(formatter.parse(playerDoc2.getString(Stats.LASTLOGIN.getQuery()))) > 1 ?
				(UUID) playerDoc1.get(Stats.PLAYERID.getQuery()) :
				(UUID) playerDoc2.get(Stats.PLAYERID.getQuery()); 
		} catch(ParseException e) {
				System.out.println("[MineStats] - An error occurred parsing.");	
		}
		return null;
	}

}
