package org.rage.pluginstats.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021
 */
public class UploadCommand implements CommandExecutor{
	
	private ListenersController controller;
	
	public UploadCommand(ListenersController controller) {
		this.controller = controller;
		
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
		
		PlayerProfile ps = controller.getPlayerStats(player);
		
		ps.flushSessionPlaytime();
		ps.stopPersisting();
		controller.uploadToDataBase(ps);
		ps.startPersisting();
		
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - All of &a<player>&7 stats are up to date on the cloud :DD.".replace("<player>", player.getName())));
		
		return true;
	}
}
