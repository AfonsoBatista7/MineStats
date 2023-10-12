package org.rage.pluginstats.commands;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.discord.LinkManager;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class DiscordUnLinkCommand implements CommandExecutor {

	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private LinkManager linkMan;
	
	public DiscordUnLinkCommand(DataBaseManager mongoDB, ServerManager serverMan, LinkManager linkMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
		this.linkMan = linkMan;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String name = sender.getName();
		
		if(args.length==0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
				return false;
			}
		} else name = args[0];
		
		Document playerDoc = mongoDB.getPlayerByName(name);
		
		if(playerDoc==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
			return false;
		}
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));
				
		if(mongoDB.getDiscordUserByPlayer(pp.getPlayerID())==null) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - &d&l"+name+"&7 is not linked yet. Try /link first."));
			return false;
		}
		
		linkMan.unlink(pp.getPlayerID());
		sender.sendMessage(Util.chat("&b[MineStats]&7 - &d&l"+name+"&7 is now unlinked!"));
		
		return true;
	}

}
