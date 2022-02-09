package org.rage.pluginstats.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class ListenersController {
	
	
	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	
	
	public ListenersController(DataBaseManager mongoDB, ServerManager serverMan) {
		
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
	}
	
	@SuppressWarnings("deprecation")
	public void playerJoin(Player player) {
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.join();
		
		if(mongoDB.getConfig().getString("players."+player.getUniqueId())!=null)
			player.setDisplayName(Util.chat(mongoDB.getConfig().getString("players."+player.getUniqueId())));
		
		mongoDB.updateStat(Filters.eq(Stats.PLAYERID.getQuery(), pp.getPlayerID()), Updates.set(Stats.ONLINE.getQuery(), true));
		pp.startPersisting();
		
		if(pp.addNewVersion(serverMan.getCurrentServerVersion())) pp.medalCheck(Medals.TIMETRAVELLER, pp.getNumVersions(), player);
		pp.medalCheck(Medals.VETERAN, pp.getLastLoginDate().getYear() - pp.getPlayerSinceDate().getYear(), player);
		pp.medalCheck(Medals.LOGINNER, pp.getTimesLogin(), player);
			
	}
	
	public void playerQuit(Player player) {
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
		ServerPlayer pp = mongoDB.getPlayerStats(player);
		pp.medalCheck(Medals.ZOMBIE, pp.die(), player);
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
