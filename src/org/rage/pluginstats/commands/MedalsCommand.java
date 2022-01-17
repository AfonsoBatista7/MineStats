package org.rage.pluginstats.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.utils.Util;

/**
 * Prints all current existing medals
 * @author Afonso Batista
 * 2021 - 2022
 */
public class MedalsCommand implements CommandExecutor {

	public MedalsCommand() {}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		sender.sendMessage(
				Util.chat("&b[MineStats]&7 - &aNostalgia Medals :D"));
		for(Medals medal: Medals.values()) {
			sender.sendMessage(
					Util.chat("    &c<medal>").replace("<medal>", medal.toString()));
		}
				
		return true;
	}

}
