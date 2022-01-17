package org.rage.pluginstats.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.utils.Util;

/**
 * Upload a specified player stats to database
 * @author Afonso Batista
 * 2021 - 2022
 */
public class UploadCommand implements CommandExecutor{
	
	private DataBaseManager mongoDB;
	
	public UploadCommand(DataBaseManager mongoDB) {
		this.mongoDB = mongoDB;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player player = null;
		
		if(args.length==0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - Only players can do this command without a player to cast!")); 
				return false;
			}
		
			player = (Player) sender;
		
		} else {
			
			player = Bukkit.getPlayerExact(args[0]);
			if(player==null) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - The player &a<player>&7 isn't online now...").replace("<player>", args[0]));
				return false;
			}
		}
		
		ServerPlayer sp = mongoDB.getPlayerStats(player);
		
		sp.flushSessionPlaytime();
		sp.stopPersisting();
		mongoDB.uploadToDataBase(sp);
		sp.startPersisting();
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - All of &a<player>&7 stats are up to date on the cloud :DD.".replace("<player>", player.getName())));
		
		return true;
	}
}
