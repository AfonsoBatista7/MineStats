package org.rage.pluginstats.commands;


import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.tags.Tags;
import org.rage.pluginstats.utils.Util;

public class TagsCommand implements CommandExecutor {
	
	private ServerManager serverMan;
	private DataBaseManager mongoDB;
	private Server server;
	
	public TagsCommand(Server server, DataBaseManager mongoDB, ServerManager serverMan) {
		this.server = server;
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		String name = sender.getName();
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Just players can performe this command."));
			return false;
		}
		
		if(args.length==0) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - \n&e/tag set <tagName>&7 - Set a tag on your name.\n&e/tag del&7 - delete your current tag\n&e/tag list&7 - list all of your tags"));
			return false;
		}
		
		Tags tag;
		String tagName, color = "", oldTags="",
				fromFile;
		Player player = server.getPlayerExact(name);      
		
		Medal[] medals;
		Document playerDoc = mongoDB.getPlayerByName(name);
		
		if(playerDoc==null) {
			Player[] players = sender.getServer().getOnlinePlayers();
			
			for(Player pl: players) {
				if(sender.getName().equals(pl.getName())) {
					playerDoc = mongoDB.getPlayer(pl.getUniqueId());
					break;
				}
			}
			if(playerDoc==null) {
				sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't exist on DataBase."));
				return false;
			}
		}
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap((UUID) playerDoc.get(Stats.PLAYERID.getQuery()));
	
		if(pp==null) medals = mongoDB.loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
		else medals = pp.getMedals();
		
		String[] fileTags = new String[0]; 
		
		switch(args[0].toLowerCase()) {
			case "set":
				if(args.length==1) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify the tag name! Try &e/tag list&7."));
					return false;
				}
				
				try {
					tagName = "["+args[1].toUpperCase()+"]";
					
					String newName = name, listName = name;					
					boolean haveFileTag = false;
					
					fromFile = mongoDB.getConfig().getString("players."+playerDoc.get(Stats.PLAYERID.getQuery()));
					
					if(fromFile!=null && fromFile.split(">").length==3) {
						oldTags = fromFile.split(">")[2];
						fileTags = oldTags.split(",");
						
						for(String fileTag: fileTags) {
							
							if(removeColorFromTag(fileTag.toUpperCase()).equals(args[1].toUpperCase())) {
								 color = fileTag.subSequence(0, 2).toString();
								 tagName = "["+fileTag.substring(2).toUpperCase()+"]&r";
								 newName = String.format("%s[%s]&r %s",color ,fileTag.substring(2).toUpperCase(), name);
								 listName = color+name;
								 
								 haveFileTag = true;
								 
								break;
							}	
						}
					}
					if(!haveFileTag) {
						tag = Tags.valueOf(args[1].toUpperCase());
						Medal medal = haveTag(tag, medals);
						
						if(medal==null) { 
							sender.sendMessage(Util.chat("&b[MineStats]&7 - You dont have this tag."));
							return false;
						}
						
						if(tag.hasCustomColor() && (medal.getMedalLevel().equals(MLevel.III) || medal.getMedal().equals(Medals.GOD))) color = tag.getColor();
						else if(medal.getMedalLevel().equals(MLevel.GOD)) {
								color = tag.hasCustomColor() ? tag.getColor() : "&3";
								tagName = Util.rainbowText(tagName);
						} else color = medal.getMedalLevel().getLevelColor();
						
						newName = String.format("%s%s&r %s",color, tagName, name);
						listName = medal.getMedalLevel().equals(MLevel.GOD) ? color+"&l"+name : color+name;		
					}
					
					mongoDB.getConfig().set("players."+player.getUniqueId(), newName +">"+ listName +">"+oldTags);
					
					player.setDisplayName(Util.chat(newName));
					player.setPlayerListName(Util.chat(listName));
					
				} catch(ArrayIndexOutOfBoundsException e) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a medal."));
					return false;
				} catch(IllegalArgumentException e) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This Medal doesn't exist yet."));
					e.printStackTrace();
					return false;
				} 
				
				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You set your tag to: %s%s", color, tagName)));
				
				break;
			case "del":
				
				fromFile = mongoDB.getConfig().getString("players."+playerDoc.get(Stats.PLAYERID.getQuery()));
				
				if(fromFile!=null && fromFile.split(">").length==3)
					oldTags = mongoDB.getConfig().getString("players."+playerDoc.get(Stats.PLAYERID.getQuery())).split(">")[2];
				
				mongoDB.getConfig().set("players."+player.getUniqueId(), name +">"+ name +">"+oldTags);
				
				player.setDisplayName(String.format("%s", name));
				player.setPlayerListName(String.format("%s", name));
				
				sender.sendMessage(Util.chat("&b[MineStats]&7 - &aSuccess! &7You deleted your tag!"));
				
				break;
			case "list":
				
				fromFile = mongoDB.getConfig().getString("players."+playerDoc.get(Stats.PLAYERID.getQuery()));
				
				if(fromFile!=null && fromFile.split(">").length==3)
					fileTags = mongoDB.getConfig().getString("players."+playerDoc.get(Stats.PLAYERID.getQuery())).split(">")[2].split(",");
				
				sender.sendMessage(Util.chat("&b[MineStats]&7 - &cYour Tags:"));
				for(Medal medal: medals) {
					if(medal!=null) {						
						
						tag = medal.getMedal().getTag();
						tagName = "["+tag.getTag().toUpperCase()+"]";
						
						if(!tag.equals(Tags.MEMBER)) {
							
							if(tag.hasCustomColor() && (medal.getMedalLevel().equals(MLevel.III) || medal.getMedal().equals(Medals.GOD))) color = tag.getColor();
							else if(medal.getMedalLevel().equals(MLevel.GOD)) {
									color = tag.hasCustomColor() ? tag.getColor() : "&3";
									tagName = Util.rainbowText(tagName);
							} else color = medal.getMedalLevel().getLevelColor();
														
							sender.sendMessage(Util.chat(String.format("    %s%s", color, tagName)));
						}
					}
				}
				
				for(String fileTag: fileTags) {
					color = fileTag.subSequence(0, 2).toString();
					tagName = color+"["+fileTag.substring(2).toUpperCase()+"]";
					sender.sendMessage(Util.chat(String.format("    %s", tagName)));
				}
				
				break;
			case "give":
				
				
				
				if(!sender.hasPermission(PermissionDefault.OP.name())) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't have permission to do that :(("));
					return false;
				}
				
				if(args.length==1 || args.length==2) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You forgot to specify some arguments try: /tag give <playerName> <&Color|TagName>"));
					return false;
				}
				
				tagName = args[2];
				color = tagName.subSequence(0, 2).toString();
				
				String playerName = args[1],
					   newName = String.format("%s[%s]&r %s", color, tagName.substring(2).toUpperCase(), playerName),
					   listName = args[2].subSequence(0, 2)+playerName;
				
				Document givePlayerDoc = mongoDB.getPlayerByName(playerName);
				
				if(givePlayerDoc==null) {
					Player[] players = sender.getServer().getOnlinePlayers();
					
					for(Player pl: players) {
						if(sender.getName().equals(pl.getName())) {
							givePlayerDoc = mongoDB.getPlayer(pl.getUniqueId());
							break;
						}
					}
					if(givePlayerDoc==null) {
						sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't exist on DataBase."));
						return false;
					}
				}
				
				Player playerGive = server.getPlayerExact(playerName);
				
				UUID playerId = (UUID) givePlayerDoc.get(Stats.PLAYERID.getQuery());
				
				fromFile = mongoDB.getConfig().getString("players."+playerId);
				
				if(fromFile!=null && fromFile.split(">").length==3) {
					oldTags = ","+mongoDB.getConfig().getString("players."+playerId).split(">")[2];
				}
				
				mongoDB.getConfig().set("players."+playerId, newName +">"+ listName +">"+ tagName+oldTags);
				
				playerGive.setDisplayName(Util.chat(newName));
				playerGive.setPlayerListName(Util.chat(listName));
				
				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You gave the %s&7 tag to %s", tagName, playerName)));
				
				Bukkit.broadcastMessage(Util.chat(String.format("&b[MineStats]&7 - %s received the %s&7 tag! :D",playerName, tagName)));
				
				break;
			default:
				sender.sendMessage(Util.chat("&b[MineStats]&7 - &e/tag set <tagName> -&7 Set a tag on your name.\n&e/tag del&7 - delete your current tag\n&e/tag list&7 - list all of your tags"));
				return false;
		}
		
		return true;
	}
	
	private Medal haveTag(Tags tag, Medal[] medals) {
		for(Medal medal: medals) {
			if(medal!=null)
				if(medal.getMedal().getTag().equals(tag)) return medal;
		}
			
		return null;
	}
	
	private String removeColorFromTag(String tag) {
		
		String done="";
		
		for(String a: tag.split("&")) {
			done = done.concat(a.replaceFirst("[A-Z1-9]", ""));
		}
		
		return done;
	}
}
