package org.rage.pluginstats.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021
 */
public class UploadAllCommand implements CommandExecutor{
	private ListenersController controller;
	
	public UploadAllCommand(ListenersController controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
	
		controller.uploadAll();

		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - All the players stats are up to date on the cloud :DD."));
		
		return true;
	}
}
