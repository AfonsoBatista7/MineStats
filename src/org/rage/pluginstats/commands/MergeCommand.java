package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.medals.MLevel;
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
 * 2021 - 2023
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
			
		Document recentPlayer = getRecentPlayer(playerDoc1 ,playerDoc2);
		
		try {
		
			mergePlayerDocs(recentPlayer, recentPlayer.equals(playerDoc1) ? playerDoc2 : playerDoc1 , mongoDB);
			
		} catch(Exception e) {
			
			Bukkit.broadcastMessage(Util.chat("&b[MineStats]&7 - An ERROR occurred while merging..."));
			
			e.printStackTrace();
			return false;
		}
		
		serverMan.deleteFromHashMap((UUID) playerDoc2.get(Stats.PLAYERID.getQuery()));
		serverMan.deleteFromHashMap((UUID) playerDoc1.get(Stats.PLAYERID.getQuery()));
		
		UUID playerId = (UUID) recentPlayer.get(Stats.PLAYERID.getQuery());
		
		playerDoc1 = mongoDB.getPlayer(playerId);
				
		try {
			mongoDB.downloadFromDataBase(new ServerPlayer(playerId, mongoDB), playerDoc1);
		} catch (ParseException e) {
			Bukkit.broadcastMessage(Util.chat("&b[MineStats]&7 - An ERROR occurred while merging..."));

			e.printStackTrace();
			
			return false;
		}
		
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Player &a<player1>&7 and &a<player2>&7 now are one B)."
						.replace("<player1>", playerDoc1.getString(Stats.NAME.getQuery()))
						.replace("<player2>", playerDoc2.getString(Stats.NAME.getQuery()))));
		
		//Player player = Main.currentServer.getPlayer(playerDoc1.getString(Stats.NAME.getQuery()));
		
		//serverMan.getPlayerStats(playerId).medalCheck(Medals.NAMEHOLDER, playerDoc1.getList(Stats.NAMES.getQuery(), String.class).size(), player);
		return true;
	}
	
	private void mergePlayerDocs(Document recentPlayer, Document oldPlayer, DataBaseManager mongoDB) {
			
			//Merge all duplicate data, incrising the stats
			mongoDB.updateMultStats(Filters.eq(Stats.PLAYERID.getQuery(), recentPlayer.get(Stats.PLAYERID.getQuery())),
					Updates.combine(
							Updates.set(Stats.BLOCKS.getQuery(), mergeBlockData(recentPlayer.getList(Stats.BLOCKS.getQuery(), Document.class),
									oldPlayer.getList(Stats.BLOCKS.getQuery(), Document.class))),
							Updates.set(Stats.MOBSKILLED.getQuery(), mergeMobData(recentPlayer.getList(Stats.MOBSKILLED.getQuery(), Document.class),
									oldPlayer.getList(Stats.MOBSKILLED.getQuery(), Document.class))),
							Updates.set(Stats.MEDALS.getQuery(), mergeMedalData(recentPlayer.getList(Stats.MEDALS.getQuery(), Document.class),
									oldPlayer.getList(Stats.MEDALS.getQuery(), Document.class)))
					));
			
			mongoDB.updateMultStats(Filters.eq(Stats.PLAYERID.getQuery(), recentPlayer.get(Stats.PLAYERID.getQuery())),
					Updates.combine(
							Updates.set(Stats.PLAYERID.getQuery(), recentPlayer.get(Stats.PLAYERID.getQuery())),
							Updates.set(Stats.LINK.getQuery(),  recentPlayer.getString(Stats.LINK.getQuery())!=null ?
									recentPlayer.getString(Stats.LINK.getQuery()) :
									oldPlayer.getString(Stats.LINK.getQuery())),
							Updates.set(Stats.ONLINE.getQuery(), recentPlayer.getBoolean(Stats.ONLINE.getQuery()) || oldPlayer.getBoolean(Stats.ONLINE.getQuery())),
							Updates.inc(Stats.BLOCKSDEST.getQuery(), oldPlayer.getLong(Stats.BLOCKSDEST.getQuery())),
							Updates.inc(Stats.BLOCKSPLA.getQuery(), oldPlayer.getLong(Stats.BLOCKSPLA.getQuery())),
							Updates.inc(Stats.BLOCKSMINED.getQuery(), oldPlayer.getLong(Stats.BLOCKSMINED.getQuery())),
							Updates.inc(Stats.KILLS.getQuery(), oldPlayer.getLong(Stats.KILLS.getQuery())),
							Updates.inc(Stats.MOBKILLS.getQuery(), oldPlayer.getLong(Stats.MOBKILLS.getQuery())),
							Updates.inc(Stats.TRAVELLED.getQuery(), oldPlayer.getLong(Stats.TRAVELLED.getQuery())),
							Updates.inc(Stats.DEATHS.getQuery(), oldPlayer.getLong(Stats.DEATHS.getQuery())),
							Updates.inc(Stats.TIMESLOGIN.getQuery(), oldPlayer.getLong(Stats.TIMESLOGIN.getQuery())),
							Updates.inc(Stats.FISHCAUGHT.getQuery(), oldPlayer.getLong(Stats.FISHCAUGHT.getQuery())),
							Updates.inc(Stats.REDSTONEUSED.getQuery(), oldPlayer.getLong(Stats.REDSTONEUSED.getQuery())),
							Updates.min(Stats.PLAYERSINCE.getQuery(), oldPlayer.getString(Stats.PLAYERSINCE.getQuery())),
							Updates.set(Stats.TIMEPLAYED.getQuery(), mergeTimePlayed(recentPlayer.getString(Stats.TIMEPLAYED.getQuery()), oldPlayer.getString(Stats.TIMEPLAYED.getQuery()))),
							Updates.addEachToSet(Stats.MEDALS.getQuery(), oldPlayer.getList(Stats.MEDALS.getQuery(), Document.class)),
							Updates.addEachToSet(Stats.VERSIONS.getQuery(),oldPlayer.getList(Stats.VERSIONS.getQuery(), String.class))

				)
			);
			
			mongoDB.deleteDoc(Filters.eq(Stats.PLAYERID.getQuery(), oldPlayer.get(Stats.PLAYERID.getQuery())));
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
	
	private List<Document> mergeBlockData(List<Document> rpBlocks, List<Document> opBlocks) {
		
		int toRemove = -1;
		
		for(Document rDoc : rpBlocks) {
			for(Document oDoc : opBlocks) {
				if(rDoc.getString("bName").equals(oDoc.getString("bName"))) {
					long placed = rDoc.getLong("bNumPlaced") + oDoc.getLong("bNumPlaced");
					long destroyed = rDoc.getLong("bNumDestroyed") + oDoc.getLong("bNumDestroyed");
					
					rDoc.put("bNumPlaced", placed);
					rDoc.put("bNumDestroyed", destroyed);
					
					toRemove = opBlocks.indexOf(oDoc);
					
					continue;
				}
			}
			
			if(toRemove!=-1) {
				opBlocks.remove(toRemove);
				toRemove = -1;
			}
		}
		
		rpBlocks.addAll(opBlocks);
		
		return rpBlocks;
	}
	
	private List<Document> mergeMobData(List<Document> rpMobs, List<Document> opMobs) {
		
		int toRemove = -1;
		
		for(Document rDoc : rpMobs) {
			for(Document oDoc : opMobs) {
				if(rDoc.getString("mName").equals(oDoc.getString("mName"))) {
					long killed = rDoc.getLong("mNumKilled") + oDoc.getLong("mNumKilled");
					
					rDoc.put("mNumKilled", killed);
					
					toRemove = opMobs.indexOf(oDoc);
					
					continue;
				}
			}
			
			if(toRemove!=-1) {
				opMobs.remove(toRemove);
				toRemove = -1;
			}
			

		}
				
		rpMobs.addAll(opMobs);
		
		return rpMobs;
	}
	
	private List<Document> mergeMedalData(List<Document> recentBadMedals, List<Document> oldBadMedals) {
		
		String medalName, medalName2, level, level2;
		List<Document> newList = new ArrayList<>(recentBadMedals);
		
		for(Document badDoc: recentBadMedals) {
			for(Document badDoc2: oldBadMedals) {
				medalName = badDoc.getString("medalName");
				medalName2 = badDoc2.getString("medalName");
				
				if(medalName.equals(medalName2)) {
					
					level = badDoc.getString("medalLevel");
					level2 = badDoc2.getString("medalLevel");
					
					if(MLevel.valueOf(level).getNumber() < MLevel.valueOf(level2).getNumber()) {
						newList.remove(badDoc);
						badDoc.put("medalLevel", level2);
						newList.add(badDoc);
					}
				}
			}
		}
		
		return newList;
	}
	
	private Document getRecentPlayer(Document playerDoc1, Document playerDoc2) {
		
		try { 
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy h:mm a"); 
			return formatter.parse(playerDoc1.getString(Stats.LASTLOGIN.getQuery())).
					compareTo(formatter.parse(playerDoc2.getString(Stats.LASTLOGIN.getQuery()))) > 1 ?
				playerDoc1 :
				playerDoc2; 
		} catch(ParseException e) {
				System.out.println("[MineStats] - An error occurred parsing.");	
		}
		return null;
	}

}
