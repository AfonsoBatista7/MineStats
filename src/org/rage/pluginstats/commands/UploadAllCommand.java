package org.rage.pluginstats.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.Util;

/**
 * Uploads to database all the current player stats to update it.
 * @author Afonso Batista
 * 2021 - 2022
 */
public class UploadAllCommand implements CommandExecutor{
	private ServerManager serverMan;
	
	public UploadAllCommand(ServerManager serverMan) {
		this.serverMan = serverMan;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
	
		serverMan.uploadAll();

		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - All the players stats are up to date on the cloud :DD."));
		
		return true;
	}
}
