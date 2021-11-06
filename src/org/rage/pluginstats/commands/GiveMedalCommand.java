package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.listeners.ListenersController;
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
public class GiveMedalCommand implements CommandExecutor {

	private DataBase mongoDB;
	private ListenersController controller;

	public GiveMedalCommand(ListenersController controller, DataBase mongoDB) {
		this.controller = controller;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
		
		Document playerDoc;
		try {
			playerDoc = mongoDB.getPlayerByName(args[0]);
		} catch(ArrayIndexOutOfBoundsException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
			return false;
		}
		
		if(playerDoc==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
			return false;
		}
		
		
		Medals medal;
		try {
			medal = Medals.valueOf(args[1].toUpperCase());
		} catch(IllegalArgumentException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This Medal doesn't exist yet."));
			return false;
		} catch(ArrayIndexOutOfBoundsException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a medal."));
			return false;
		}
		
		MLevel level;
		try {
			level = MLevel.valueOf(args[2].toUpperCase());
		} catch(IllegalArgumentException e) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This Level doesn't exist yet."));
			return false;
		} catch(ArrayIndexOutOfBoundsException e) {
			level = MLevel.GOD;
		}
		
		MongoCollection<Document> collection = mongoDB.getCollection();
		PlayerProfile pp = controller.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));       
		
		if(pp==null) {
			try {
				pp = new PlayerProfile((UUID) playerDoc.get(Stats.PLAYERID.getQuery()), controller, mongoDB.getConfig());
				controller.downloadFromDataBase(pp, playerDoc);															//CASO O PLAYER NAO ESTEJA ONLINE
			} catch (ParseException e) {	
				e.printStackTrace();
			}
		}
		
		Medal newMedal = new Medal(medal, level);
		Document newMedalDoc = controller.createMedalDoc(newMedal);
		                                                                         
		if(pp.haveMedal(medal)) {
			if(pp.getMedal(medal).getMedalLevel().equals(level)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - This player already have this medal."));
				return false;
			} else {
				Object[] list = playerDoc.getList(Stats.MEDALS.getQuery(), Document.class).toArray();
				List<Document> finalList = new ArrayList<Document>(list.length);
				for(int i=0; i<list.length; i++) {
					Document document = (Document) list[i];
					finalList.add(i, document);
					if(document.getString(Stats.MEDALNAME.getQuery()).equals(medal.toString()))
						finalList.remove(i); finalList.add(i,newMedalDoc); 
				}
			}
		} else {
			pp.newMedal(newMedal);
			collection.updateOne(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.addToSet(Stats.MEDALS.getQuery(), newMedalDoc));
		}
		
		Player player = Main.currentServer.getPlayer(playerDoc.getString(Stats.NAME.getQuery()));
		if(player!=null) controller.newMedalEffect(player, newMedal.getMedalLevel());
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - Now, &a<player1>&7 have the &c<medal>&7 &6<level>&7 level Medal!."
						.replace("<player1>", playerDoc.getString(Stats.NAME.getQuery()))
						.replace("<medal>", newMedal.getMedal().toString())
						.replace("<level>", newMedal.getMedalLevel().toString())));
	
		
		return true;
		
	}

}
