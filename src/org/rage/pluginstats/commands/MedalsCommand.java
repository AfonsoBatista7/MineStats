package org.rage.pluginstats.commands;

import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

/**
 * Prints all current existing medals
 * @author Afonso Batista
 * 2021 - 2023
 */
public class MedalsCommand implements CommandExecutor {

	private ServerManager serverMan;
	private DataBaseManager mongoDB;

	public MedalsCommand(DataBaseManager mongoDB, ServerManager serverMan) {
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Document playerDoc;
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
			return false;
		}
		
		String name = sender.getName();
		
		playerDoc = mongoDB.getPlayerByName(name);
		
		if(playerDoc==null) {
			Collection<? extends Player> players = sender.getServer().getOnlinePlayers();

                        Iterator<? extends Player> iterator = players.iterator();
			
                        while(iterator.hasNext()) {
                            Player player = iterator.next();
                            if(sender.getName().equals(player.getName())) {
                                    playerDoc = mongoDB.getPlayer(player.getUniqueId());
                                    break;
                            }
                        }

			if(playerDoc==null) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't exist on DataBase."));
				return false;
			}
		}
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));       
		
		Medal[] medals;
		
		if(pp==null) medals = mongoDB.loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
		else medals = pp.getMedals();
		
		sender.sendMessage(
				Util.chat("&b[MineStats]&7 - &aNostalgia Medals :D"));
		for(Medals medal: Medals.values()) {
			if(contains(medal, medals))
				sender.sendMessage(
						Util.chat("    &a<medal> &7- <description>").replace("<medal>", medal.toString())
																	.replace("<description>", medal.getHowToGet()));
			else
				sender.sendMessage(
						Util.chat("    &c<medal> &7- <description>").replace("<medal>", medal.toString())
																	.replace("<description>", medal.getHowToGet()));
		}
				
		return true;
	}
	
	private boolean contains(Medals medal, Medal[] medals) {
		
		int i = 0;
		
		while(i<medals.length) {
			if(medals[i]!=null)
				if(medals[i].getMedal().equals(medal)) return true;
			i++;
		}
		return false;
	}

}
