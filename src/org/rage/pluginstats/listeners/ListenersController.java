package org.rage.pluginstats.listeners;

import org.bson.Document;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.DiscordUtil;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class ListenersController {
	
	
	private DataBaseManager mongoDB;
	
	public ListenersController(DataBaseManager mongoDB, ServerManager serverMan) {
		
		this.mongoDB = mongoDB;
	}
	
	@SuppressWarnings("deprecation")
	public void playerJoin(Player player) {
		
		DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId())
							.getTextChannelById(DiscordUtil.getChannelId())
							.sendMessage("```fix\n"+ player.getName() +" joined the game.\n```").queue();
		
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.join();
		
		if(mongoDB.getConfig().getString("players."+player.getUniqueId())!=null) {
			
			String[] names = mongoDB.getConfig().getString("players."+player.getUniqueId()).split(">");
	
			player.setDisplayName(Util.chat(names[0]));
			player.setPlayerListName(Util.chat(names[1]));
		}
		
		mongoDB.updateStat(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.set(Stats.ONLINE.getQuery(), true));
		pp.startPersisting();
		
		Document doc = mongoDB.getPlayer(player.getUniqueId());
		
		if(mongoDB.checkVersions(ServerManager.getServerVersion(), doc)) 
			pp.medalCheck(Medals.TIMETRAVELLER, pp.getNumberOfVersions(), player);
		pp.medalCheck(Medals.VETERAN, pp.getLastLoginDate().getYear() - pp.getPlayerSinceDate().getYear(), player);
		pp.medalCheck(Medals.LOGINNER, pp.getTimesLogin(), player);
			
	}
	
	public void playerQuit(Player player) {
		
		DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId())
				   .getTextChannelById(DiscordUtil.getChannelId())
				   .sendMessage("```fix\n"+ player.getName() +" left the game.\n```").queue();
		
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.quit();
		pp.stopPersisting();
		mongoDB.uploadToDataBase(pp);
		
	}
	
	public void logInOnlinePlayers() {
		for(Player player : Main.currentServer.getOnlinePlayers()) playerJoin(player);
	}
	
	public void playerMove(Player player) {
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.move();
		if(pp.getKilometer()==1000) {
			pp.resetKilometer();
			pp.medalCheck(Medals.WORLDTRAVELLER, pp.getMetersTraveled(), player);
		}
		
	}
	
	public void playerKick(Player player) {
		playerQuit(player);
	}
	
	public void placeBlock(Player player, Block block) {
		if(block.getType().getId() > 0) {
			ServerPlayer pp = mongoDB.getPlayerStats(player);
			pp.medalCheck(Medals.BUILDER, pp.placeBlock(), player);
			
			switch(block.getType()) {
			case REDSTONE:
			case REDSTONE_BLOCK:
			case REDSTONE_COMPARATOR:
			case REDSTONE_WIRE:
			case REDSTONE_ORE:
			case REDSTONE_LAMP_OFF:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
				pp.medalCheck(Medals.REDSTONENGINEER, pp.useRedstone(), player);
				break;
			default:
			}
		}
	}
	
	public void breakBlock(Player player, Block block) {
		if(block.getType().getId() > 0) {
			int Ycord = player.getEyeLocation().getBlockY();
			ServerPlayer pp = mongoDB.getPlayerStats(player);
			
			pp.medalCheck(Medals.DESTROYER, pp.breakBlock(), player);
			if(Ycord>=1 && Ycord<=63 && isMining(block)) pp.medalCheck(Medals.MINER, pp.mineBlock(), player);
		}
	}
	
	public void playerFishCaught(Player player, Entity caught) {
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.medalCheck(Medals.FISHERMAN, pp.fishCaught(), player);
	}
	
	public void die(Player player) {
		
		player.getLastDamageCause().getCause().toString();
		
		DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId())
		.getTextChannelById(DiscordUtil.getChannelId())
		.sendMessage("```diff\n-"+ player.getName() + getDeathCause(player).toLowerCase().replace("_", " ") +"-\n```").queue();
				
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.medalCheck(Medals.ZOMBIE, pp.die(), player);
	}
	
	private String getDeathCause(Player player) {
		
		DamageCause cause = player.getLastDamageCause().getCause();
		
		switch(cause) {
		case BLOCK_EXPLOSION:
		case ENTITY_EXPLOSION:
			return " blew up";
		case ENTITY_ATTACK:
			return " was killed by a mob";
		default:
			return " was killed by " +cause.toString();
		}
	}
	
	public void kill(Player player, Entity entity) {
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		
		if(entity instanceof Player)
			pp.medalCheck(Medals.PVPMASTER, pp.killPlayer(), player);
		else {
			pp.medalCheck(Medals.MOBSLAYER, pp.killMob(), player);
			
			switch(entity.getType()) {
			case ENDER_DRAGON:
				pp.medalCheck(Medals.DRAGONSLAYER, pp.killEnderDragon(), player);
				break;
			case WITHER:
				pp.medalCheck(Medals.WITHERSLAYER, pp.killWither(), player);
				break;
			default:
			}
		}
	}
	
	public boolean isMining(Block block) {
		switch(block.getType()) {
		case STONE:
		case COAL_ORE:
		case IRON_ORE:
		case GOLD_ORE:
		case DIAMOND_ORE:
		case LAPIS_ORE:
		case REDSTONE_ORE:
		case EMERALD_ORE:
		case GRAVEL:
		case SANDSTONE:
		case OBSIDIAN:
			return true;
		default:
			return false;
		}
	}
}
