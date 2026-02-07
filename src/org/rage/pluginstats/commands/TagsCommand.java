package org.rage.pluginstats.commands;

import java.util.List;
import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

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

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
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
			sender.sendMessage(Util.chat("&b[MineStats]&7 - Only players can performe this command."));
			return false;
		}

		if(args.length==0) {
			sender.sendMessage(Util.chat("&b[MineStats]&7 - \n&e/tag set <tagName>&7 - Set a tag on your name.\n&e/tag del&7 - delete your current tag\n&e/tag list&7 - list all of your tags\n&e/tag give <playerName> <&ColorTagName>&7 - (OP) give a custom tag\n&e/tag rm <playerName> <tagName>&7 - (OP) remove a custom tag"));
			return false;
		}

		Tags tag;
		String tagName, color = "";
		Player player = server.getPlayerExact(name);

		Medal[] medals;
		Document playerDoc = mongoDB.getPlayerByName(name);

		if(playerDoc==null) {
                    Collection<? extends Player> players = sender.getServer().getOnlinePlayers();

                    Iterator<? extends Player> iterator = players.iterator();

                    while(iterator.hasNext()) {
                        Player pl = iterator.next();

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

		String[] customTags;
		if(pp!=null) customTags = pp.getCustomTags();
		else {
			List<String> ctList = playerDoc.getList("customTags", String.class);
			customTags = ctList != null ? ctList.toArray(new String[0]) : new String[0];
		}

		switch(args[0].toLowerCase()) {
			case "set":
				if(args.length==1) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify the tag name! Try &e/tag list&7."));
					return false;
				}

				tagName = "["+args[1].toUpperCase()+"]";
				String newName = name, listName = name;

				// Check medal tags first
				try {
					tag = Tags.valueOf(args[1].toUpperCase());
					Medal medal = haveTag(tag, medals);

					if(medal!=null) {
						if(tag.hasCustomColor() && (medal.getMedalLevel().equals(MLevel.III) || medal.getMedal().equals(Medals.GOD))) color = tag.getColor();
						else if(medal.getMedalLevel().equals(MLevel.GOD)) {
							color = tag.hasCustomColor() ? tag.getColor() : "&3";
							tagName = Util.rainbowText(tagName);
						} else color = medal.getMedalLevel().getLevelColor();

						newName = String.format("%s%s&r %s", color, tagName, name);
						listName = medal.getMedalLevel().equals(MLevel.GOD) ? color+"&l"+name : color+name;

						mongoDB.getConfig().set("players."+player.getUniqueId(), newName +">"+ listName);
						player.setDisplayName(Util.chat(newName));
						player.setPlayerListName(Util.chat(listName));

						sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You set your tag to: %s%s", color, tagName)));
						break;
					}
				} catch(IllegalArgumentException e) { }

				// Check custom tags
				String foundTag = findCustomTag(args[1], customTags);
				if(foundTag!=null) {
					color = getCustomTagColor(foundTag);
					tagName = "["+getCustomTagName(foundTag).toUpperCase()+"]";

					newName = String.format("%s%s&r %s", color, tagName, name);
					listName = color+name;

					mongoDB.getConfig().set("players."+player.getUniqueId(), newName +">"+ listName);
					player.setDisplayName(Util.chat(newName));
					player.setPlayerListName(Util.chat(listName));

					sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You set your tag to: %s%s", color, tagName)));
					break;
				}

				sender.sendMessage(Util.chat("&b[MineStats]&7 - You dont have this tag."));
				return false;

			case "del":

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

				if(customTags.length > 0) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - &cCustom Tags:"));
					for(String ct : customTags) {
						color = getCustomTagColor(ct);
						tagName = "["+getCustomTagName(ct).toUpperCase()+"]";
						sender.sendMessage(Util.chat(String.format("    %s%s", color, tagName)));
					}
				}

				break;
			case "give":

				if(!sender.hasPermission(PermissionDefault.OP.name())) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't have permission to do that :(("));
					return false;
				}

				if(args.length==1 || args.length==2) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You forgot to specify some arguments try: /tag give <playerName> <&ColorTagName>"));
					return false;
				}

				tagName = args[2];

				if(tagName.startsWith("&") && tagName.length() > 2) {
					color = tagName.substring(0, 2);
					tagName = tagName.substring(2);
				} else {
					color = "&7";
				}

				String playerName = args[1],
				       giveNewName = String.format("%s[%s]&r %s", color, tagName.toUpperCase(), playerName),
                                       giveListName = color+playerName;

				Document givePlayerDoc = mongoDB.getPlayerByName(playerName);

				if(givePlayerDoc==null) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
					return false;
				}

				UUID givePlayerId = (UUID) givePlayerDoc.get(Stats.PLAYERID.getQuery());

				// Persist to MongoDB
				mongoDB.addCustomTag(givePlayerId, args[2]);

				// Update in-memory if online
				ServerPlayer giveSp = serverMan.getPlayerFromHashMap(givePlayerId);
				if(giveSp!=null) {
					String[] oldCustomTags = giveSp.getCustomTags();
					String[] newCustomTags = new String[oldCustomTags.length + 1];
					System.arraycopy(oldCustomTags, 0, newCustomTags, 0, oldCustomTags.length);
					newCustomTags[oldCustomTags.length] = args[2].toUpperCase();
					giveSp.setCustomTags(newCustomTags);
				}

				Player playerGive = server.getPlayerExact(playerName);
				if(playerGive!=null) {
					playerGive.setDisplayName(Util.chat(giveNewName));
					playerGive.setPlayerListName(Util.chat(giveListName));
				}

				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You gave the %s&7 tag to %s", args[2], playerName)));

				Bukkit.broadcastMessage(Util.chat(String.format("&b[MineStats]&7 - %s received the %s&7 tag! :D", playerName, args[2])));

				break;
			case "rm":

				if(!sender.hasPermission(PermissionDefault.OP.name())) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't have permission to do that :(("));
					return false;
				}

				if(args.length==1 || args.length==2) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - You forgot to specify some arguments try: /tag rm <playerName> <tagName>"));
					return false;
				}

				String rmPlayerName = args[1];
				String rmTagName = args[2];

				Document rmPlayerDoc = mongoDB.getPlayerByName(rmPlayerName);

				if(rmPlayerDoc==null) {
					sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
					return false;
				}

				UUID rmPlayerId = (UUID) rmPlayerDoc.get(Stats.PLAYERID.getQuery());

				// Load custom tags for this player
				String[] rmCustomTags;
				ServerPlayer rmSp = serverMan.getPlayerFromHashMap(rmPlayerId);
				if(rmSp!=null) rmCustomTags = rmSp.getCustomTags();
				else {
					List<String> rmCtList = rmPlayerDoc.getList("customTags", String.class);
					rmCustomTags = rmCtList != null ? rmCtList.toArray(new String[0]) : new String[0];
				}

				// Find the matching custom tag (case-insensitive, strip color codes from input)
				String cleanRmTagName = removeColorFromTag(rmTagName);
				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - Searching for: &e%s", cleanRmTagName)));
				String rmFoundTag = findCustomTag(cleanRmTagName, rmCustomTags);
				if(rmFoundTag==null) {
					sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - %s doesn't have the &e%s&7 custom tag.", rmPlayerName, rmTagName)));
					return false;
				}

				// Remove from MongoDB
				mongoDB.removeCustomTag(rmPlayerId, rmFoundTag);

				// Update in-memory if online
				if(rmSp!=null) {
					String[] oldRmTags = rmSp.getCustomTags();
					String[] newRmTags = new String[oldRmTags.length - 1];
					int idx = 0;
					for(String t : oldRmTags) {
						if(!t.equals(rmFoundTag)) newRmTags[idx++] = t;
					}
					rmSp.setCustomTags(newRmTags);
				}

				sender.sendMessage(Util.chat(String.format("&b[MineStats]&7 - &aSuccess! &7You removed the &e%s&7 tag from %s", rmFoundTag, rmPlayerName)));

				break;
			default:
				sender.sendMessage(Util.chat("&b[MineStats]&7 - \n&e/tag set <tagName>&7 - Set a tag on your name.\n&e/tag del&7 - delete your current tag\n&e/tag list&7 - list all of your tags\n&e/tag give <playerName> <colorTagName>&7 - (OP) give a custom tag\n&e/tag rm <playerName> <tagName>&7 - (OP) remove a custom tag"));
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

	private String findCustomTag(String tagName, String[] customTags) {
		for(String ct : customTags) {
			System.out.println("[MineStats] findCustomTag: stored='" + ct + "' cleaned='" + removeColorFromTag(ct) + "' searching='" + tagName + "'");
			if(removeColorFromTag(ct).equalsIgnoreCase(tagName)) return ct;
		}
		return null;
	}

	private String getCustomTagColor(String ct) {
		return (ct.startsWith("&") && ct.length() > 2) ? ct.substring(0, 2) : "&7";
	}

	private String getCustomTagName(String ct) {
		return (ct.startsWith("&") && ct.length() > 2) ? ct.substring(2) : ct;
	}

	private String removeColorFromTag(String tag) {
		return tag.replaceAll("&[0-9a-zA-Z]", "");
	}
}
