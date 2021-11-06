package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021
 */
public class DownloadCommand implements CommandExecutor{

	private ListenersController controller;
	private DataBase mongoDB;

	public DownloadCommand(ListenersController controller, DataBase mongoDB) {
		this.controller = controller;
		this.mongoDB = mongoDB;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		Document playerDoc = null;
		PlayerProfile ps = null;
		
		if(!(sender instanceof Player)) {
			
			if(args.length==1) {
				
				try {
					playerDoc = mongoDB.getPlayerByName(args[0]);
				} catch(ArrayIndexOutOfBoundsException e) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
					return false;
				}
				
				if(playerDoc==null) { 
					sender.sendMessage(Util.chat("&b[MineStats]&7 - The player: &a<player>&7 doesn't exist on the database.").replace("<player>", args[0]));
					return false;
				}
				
				UUID playerId = (UUID) playerDoc.get(Stats.PLAYERID.getQuery());
				
				ps = controller.getPlayerFromHashMap(playerId);
				
				if(ps==null) ps = new PlayerProfile(playerId, controller, mongoDB.getConfig());
				
				ps.stopPersisting();
				
				try {
					controller.downloadFromDataBase(ps, playerDoc);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				ps.startPersisting();
				
				Bukkit.broadcastMessage(
						Util.chat("&b[MineStats]&7 - SUCCESS!! All of &a<player>&7 stats were downloaded from the cloud.".replace("<player>", args[0])));
				
				return true;
				
			}
			
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You have to cast the command to a player!"));
			return false;
		
		}
			
		sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do that command..."));
		return false;
	}

}
