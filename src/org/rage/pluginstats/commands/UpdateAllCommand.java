package org.rage.pluginstats.commands;

import java.text.ParseException;
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
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

/**
 * @author Afonso Batista
 * 2021
 */
public class UpdateAllCommand implements CommandExecutor{
	private DataBase mongoDB;
	private ListenersController controller;

	public UpdateAllCommand(ListenersController controller, DataBase mongoDB) {
		this.controller = controller;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
		
		
		MongoCollection<Document> collection = mongoDB.getCollection();
		MongoCursor<Document> it = collection.find().iterator();
		PlayerProfile pp; Document doc;
		while(it.hasNext()) {
			doc = it.next();
			
			pp = controller.getPlayerFromHashMap((UUID) doc.get(Stats.PLAYERID.getQuery())); 
			
			/*doc.append(Main.REDSTONEUSED, 0L)
			   .append(Main.ENDERDRAGONKILLS, 0L)
			   .append(Main.WHITHERKILLS, 0L)
			   .append(Main.FISHCAUGHT, 0L)
			   .append(Main.VERSIONS, Arrays.asList(controller.getServerVersion()))
			   .append(Main.MEDALS, Arrays.asList(controller.createMedalDoc(new Medal(Medals.NOSTALGIAPLAYER))))
			   .append(Main.ONLINE, pp!=null ? true : false);*/
			
			if(pp==null) {
				try {
					pp = new PlayerProfile((UUID) doc.get(Stats.PLAYERID.getQuery()), controller, mongoDB.getConfig());
					controller.downloadFromDataBase(pp, doc);															//CASO O PLAYER NAO ESTEJA ONLINE
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			/*
			collection.deleteOne(Filters.eq(Main.PLAYERID, pp.getPlayerID()));
			collection.insertOne(doc);*/
			
			//Check for medals
			Player player = Main.currentServer.getPlayer(doc.getString(Stats.NAME.getQuery()));
			
			for(Medals medal: Medals.values()) {
				long variable = Util.getMedalVariable(pp, medal);
				if(variable!=0)
					controller.medalCheck(medal, variable, player, pp);
			}
		}
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - SUCCESS!!!!"));
	
		
		return true;
	}
}
