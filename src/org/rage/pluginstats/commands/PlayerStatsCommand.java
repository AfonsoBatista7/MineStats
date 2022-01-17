package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.Util;
import org.rage.pluginstats.stats.Stats;

/**
 * Prints all current specified player stats
 * @author Afonso Batista
 * 2021 - 2022
 */
public class PlayerStatsCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;

	public PlayerStatsCommand( DataBaseManager mongoDB, ServerManager serverMan) {
		this.serverMan = serverMan;
		this.mongoDB = mongoDB;
	}
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Document playerDoc;
		String name = sender.getName();
		
		if(args.length==0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
				return false;
			}
		} else name = args[0];
		
		playerDoc = mongoDB.getPlayerByName(name);
		
		if(playerDoc==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
			return false;
		}
		
		UUID playerId = (UUID) playerDoc.get(Stats.PLAYERID.getQuery());
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap(playerId);       
		if(pp==null) {
			pp = new ServerPlayer(playerId, mongoDB);
			try {
				mongoDB.downloadFromDataBase(pp, playerDoc);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &c<player>&7 Stats:").replace("<player>", name));
		for(Stats stat: Stats.values()) {
			if(stat.toPrint())
				sender.sendMessage(Util.chat("    &e<stat>&7: &b<variable>").replace("<stat>", stat.getText())
															  	  .replace("<variable>", String.valueOf(Util.getStatVariable(pp, stat))));
		}
						
		
		return true;
	}
}
