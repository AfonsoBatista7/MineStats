package org.rage.pluginstats.commands;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021
 */
public class PlayerMedalsCommand implements CommandExecutor {

	private DataBase mongoDB;
	private ListenersController controller;

	public PlayerMedalsCommand(ListenersController controller, DataBase mongoDB) {
		this.controller = controller;
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
		
		PlayerProfile pp = controller.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));       
		
		Medal[] medals;
		
		if(pp==null) medals = controller.loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
		else medals = pp.getMedals();
		
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &c<player>&7 Medals:").replace("<player>", name));
		for(Medal medal: medals) {
			if(medal!=null)
				sender.sendMessage(Util.chat("    &a<medal> &6<level>")
						.replace("<medal>", medal.getMedal().toString())
						.replace("<level>", medal.getMedalLevel().toString()));
		}
			
		return true;
	}

}
