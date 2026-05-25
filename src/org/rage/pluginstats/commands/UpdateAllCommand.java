package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.mongoDB.DBFields;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.MongoCursor;

/**
 * Update all player medals (make all players get medals if they can)
 * @author Afonso Batista
 * 2021 - 2023
 */
public class UpdateAllCommand implements CommandExecutor{
	private DataBaseManager mongoDB;
	private ServerManager serverMan;

	public UpdateAllCommand(DataBaseManager mongoDB, ServerManager serverMan) {
		this.serverMan = serverMan;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command...")); 
			return false;
		}
		
		
		MongoCursor<Document> it = mongoDB.getCollectionIterator();
		ServerPlayer sp; Document doc;
		while(it.hasNext()) {
			doc = it.next();
			
			sp = serverMan.getPlayerFromHashMap((UUID) doc.get(DBFields.PLAYER_ID));

			if(sp==null) {
				try {
					sp = new ServerPlayer((UUID) doc.get(DBFields.PLAYER_ID), mongoDB);
					mongoDB.downloadFromDataBase(sp, doc);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			Player player = Main.currentServer.getPlayer(doc.getString(DBFields.NAME));
			
			for(Medals medal: Medals.values()) {
				long variable = Util.getMedalVariable(sp, medal);
				if(variable!=0)
					sp.medalCheck(medal, variable, player);
			}
		}
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - SUCCESS!!!!"));
	
		
		return true;
	}
}
