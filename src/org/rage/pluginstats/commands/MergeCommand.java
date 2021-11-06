package org.rage.pluginstats.commands;

import java.text.ParseException;
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
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021
 */
public class MergeCommand implements CommandExecutor{

	private DataBase mongoDB;
	private ListenersController controller;

	public MergeCommand(ListenersController controller, DataBase mongoDB) {
		this.controller = controller;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
		
		if(args.length<2) {
		
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need two players to merge their stats."));
			return false;
		
		}
			
		Document playerDoc1, playerDoc2;
		
		try {
			playerDoc1 = mongoDB.getPlayerByName(args[0]);
			playerDoc2 = mongoDB.getPlayerByName(args[1]);
		} catch(ArrayIndexOutOfBoundsException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify two players."));
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
	
		
		MongoCollection<Document> collection = mongoDB.getCollection();
		
		UUID playerId = getUUIDRecentPlayer(playerDoc1 ,playerDoc2);
		
		collection.updateMany(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc1.get(Stats.PLAYERID.getQuery())),
				Updates.combine(
						Updates.set(Stats.PLAYERID.getQuery(), playerId),
						Updates.set(Stats.ONLINE.getQuery(), playerDoc1.getBoolean(Stats.ONLINE.getQuery()) || playerDoc2.getBoolean(Stats.ONLINE.getQuery())),
						Updates.inc(Stats.BLOCKSDEST.getQuery(), playerDoc2.getLong(Stats.BLOCKSDEST.getQuery())),
						Updates.inc(Stats.BLOCKSPLA.getQuery(), playerDoc2.getLong(Stats.BLOCKSPLA.getQuery())),
						Updates.inc(Stats.KILLS.getQuery(), playerDoc2.getLong(Stats.KILLS.getQuery())),
						Updates.inc(Stats.MOBKILLS.getQuery(), playerDoc2.getLong(Stats.MOBKILLS.getQuery())),
						Updates.inc(Stats.TRAVELLED.getQuery(), playerDoc2.getLong(Stats.TRAVELLED.getQuery())),
						Updates.inc(Stats.DEATHS.getQuery(), playerDoc2.getLong(Stats.DEATHS.getQuery())),
						Updates.inc(Stats.TIMESLOGIN.getQuery(), playerDoc2.getLong(Stats.TIMESLOGIN.getQuery())),
						Updates.max(Stats.LASTLOGIN.getQuery(), playerDoc2.getString(Stats.LASTLOGIN.getQuery())),
						Updates.min(Stats.PLAYERSINCE.getQuery(), playerDoc2.getString(Stats.PLAYERSINCE.getQuery())),
						Updates.set(Stats.TIMEPLAYED.getQuery(), mergeTimePlayed(playerDoc1.getString(Stats.TIMEPLAYED.getQuery()), playerDoc2.getString(Stats.TIMEPLAYED.getQuery()))),
						Updates.addEachToSet(Stats.NAMES.getQuery(), playerDoc2.getList(Stats.NAMES.getQuery(), String.class)),
						Updates.addEachToSet(Stats.MEDALS.getQuery(), playerDoc2.getList(Stats.MEDALS.getQuery(), Document.class)),
						Updates.addEachToSet(Stats.VERSIONS.getQuery(),playerDoc2.getList(Stats.VERSIONS.getQuery(), String.class))
			)
		);

		collection.deleteOne(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc2.get(Stats.PLAYERID.getQuery())));
		
		controller.deleteFromHashMap((UUID) playerDoc2.get(Stats.PLAYERID.getQuery()));
		controller.deleteFromHashMap((UUID) playerDoc1.get(Stats.PLAYERID.getQuery()));
		
		playerDoc1 = mongoDB.getPlayer(playerId);
		
		collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), playerDoc1.get(Stats.PLAYERID.getQuery())),
				Updates.set(Stats.MEDALS.getQuery(), getMedalList(playerDoc1.getList(Stats.MEDALS.getQuery(), Document.class))));
		
		try {
			controller.downloadFromDataBase(new PlayerProfile(playerId, controller, mongoDB.getConfig()), playerDoc1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Player &a<player1>&7 and &a<player2>&7 now are one B)."
						.replace("<player1>", playerDoc1.getString(Stats.NAME.getQuery()))
						.replace("<player2>", playerDoc2.getString(Stats.NAME.getQuery()))));
		
		Player player = Main.currentServer.getPlayer(playerDoc1.getString(Stats.NAME.getQuery()));
		
		controller.medalCheck(Medals.NAMEHOLDER, playerDoc1.getList(Stats.NAMES.getQuery(), String.class).size(), player, controller.getOfflinePlayerStats(playerId));
		
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
			medalName = badDoc.getString(Stats.MEDALNAME.getQuery());
			level = badDoc.getString(Stats.MEDALLEVEL.getQuery());
			newList = findAndRemoveMedal(medalName, level, badDoc, newList);
		}
		
		return newList;
	}
	
	private List<Document> findAndRemoveMedal(String medalName, String level, Document badDoc, List<Document> badMedals) {
		
		String medalName2;
		List<Document> newList = badMedals;
		
		for(Document badDoc2: badMedals) {
			medalName2 = badDoc.getString(Stats.MEDALNAME.getQuery());
			
			if(medalName.equals(medalName2) && !badDoc.equals(badDoc2)) {
				if(MLevel.valueOf(level).getNumber() > MLevel.valueOf(badDoc2.getString(medalName2)).getNumber()) {
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
		return playerDoc1.getString(Stats.LASTLOGIN).compareTo(playerDoc2.getString(Stats.LASTLOGIN)) > 1 ? (UUID) playerDoc1.get(Stats.PLAYERID.getQuery()) : (UUID) playerDoc2.get(Stats.PLAYERID.getQuery()); 
	}

}
