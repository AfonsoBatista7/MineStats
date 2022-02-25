package org.rage.pluginstats.commands;


import org.bson.Document;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
		String tagName, color;
		Player player = server.getPlayerExact(name);
		
		ServerPlayer pp = serverMan.getPlayerFromHashMap(player.getUniqueId());       
		
		Medal[] medals;
		Document playerDoc = mongoDB.getPlayerByName(name);
		
		if(pp==null) medals = mongoDB.loadMedals(playerDoc.getList(Stats.MEDALS.getQuery(), Document.class));
		else medals = pp.getMedals();
		
		switch(args[0].toLowerCase()) {
			case "set":
				if(args.length==1) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify the tag name! Try &e/tag list&7."));
					return false;
				}
				
				try {
					tag = Tags.valueOf(args[1].toUpperCase());
				
					tagName = "["+args[1].toUpperCase()+"]";
					
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
					
					String newName = String.format("%s%s&r %s",color, tagName, name),
						   listName = medal.getMedalLevel().equals(MLevel.GOD) ? color+"&l"+name : color+name;

					mongoDB.getConfig().set("players."+player.getUniqueId(), newName +">"+ listName );
					
					player.setDisplayName(Util.chat(newName));
					player.setPlayerListName(Util.chat(listName));
					
				} catch(ArrayIndexOutOfBoundsException e) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a medal."));
					return false;
				} catch(IllegalArgumentException e) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This Medal doesn't exist yet."));
					return false;
				} 
				
				
				
				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You set your tag to: %s%s", color, tagName)));
				
				break;
			case "del":
				
				mongoDB.getConfig().set("players."+player.getUniqueId(), name);
				
				player.setDisplayName(String.format("%s", name));
				player.setPlayerListName(String.format("%s", name));
				
				sender.sendMessage(Util.chat("&b[MineStats]&7 - &aSuccess! &7You deleted your tag!"));
				
				break;
			case "list":
				
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
}
