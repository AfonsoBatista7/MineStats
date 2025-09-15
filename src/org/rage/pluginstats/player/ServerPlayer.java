package org.rage.pluginstats.player;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.utils.DiscordUtil;
import org.rage.pluginstats.utils.Util;

import net.dv8tion.jda.api.entities.Guild;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class ServerPlayer extends PlayerProfile {

	private final long TIME_BETWEEN_SAVES, TIME_DISCORD_LINK;
	
	private Date sessionMarkTime;
	private DataBaseManager mongoDB;
	
	private Timer timer;
	
	public ServerPlayer(UUID playerID, DataBaseManager mongoDB) {
		super(playerID);
		
		this.mongoDB = mongoDB;
		
		TIME_DISCORD_LINK = mongoDB.getConfig().getInt("timeDiscordLink");
		TIME_BETWEEN_SAVES = mongoDB.getConfig().getInt("timeBetweenSaves");
		timer = new Timer();
	}
	
	public boolean resetLinkTrys() {
		Date now = new Date();
		long dif = now.getTime() - lastLogin.getTime() / 1000;
		if(dif>TIME_DISCORD_LINK) {
			setLinkTrys(0);
			return true;
		}
		return false;
	}
	
	public void setSessionMarkTime(Date sessionMarkTime) {
		this.sessionMarkTime = sessionMarkTime;
	}
	
	public void flushSessionPlaytime() {
		if(sessionMarkTime != null)
		{
			Date now = new Date();
			long dif = (now.getTime() - sessionMarkTime.getTime()) / 60000;
			timePlayed += dif;
			sessionMarkTime = now;
		}
	}
	
	public void quit() {
		flushSessionPlaytime();
		sessionMarkTime = null;
		online = false;
	}
	
	public void join() {
		sessionMarkTime = new Date();
		resetLinkTrys();
		lastLogin = new Date();
		online = true;
		timesLogin++;
	}
	
	public long die() {
		return deaths++;
	}
	
	public long move() {
		kilometer++;
		return metersTraveled++;
	}
	
	public void resetKilometer() {
		kilometer = 0;
	}
	
	public long breakBlock(String blockName) {
		return blockStats.breakBlock(blockName);
	}
	
	public long placeBlock(String blockName) {
		return blockStats.placeBlock(blockName);
	}
	
	public long useRedstone() {
		return blockStats.useRedstone();
	}
	
	public long killPlayer() {
		return mobStats.killPlayer();
	}
	
	public long killMob(int mobId, String mobName) {
		return mobStats.killMob(mobId, mobName);
	}
	
	public long killEnderDragon() {
		return mobStats.killEnderDragon();
	}
	
	public long killWither() {
		return mobStats.killWither();
	}
	
	public long fishCaught() {
		return mobStats.fishCaught();
	}
	
	public long mineBlock() {
		return blockStats.mineBlock();
	}
	
	/**
	 * Checks if the player have already the medal, if not adds it to data base.
	 * 
	 * @param medal - Medal to check
	 * @param player - Player to check
	 */
	public void newMedalOnDataBase(Medals medal, Player player) {
			
		if(!haveMedal(medal)) {
			Medal newMedal = new Medal(medal);
			newMedal(newMedal);
			mongoDB.newMedalOnDataBase(newMedal, player);
			newMedal.newMedalEffect(player);
			Bukkit.broadcastMessage(
					Util.chat("&b[MineStats]&7 - &a<player>&7, received the &c<medalName> &6<level>&7 Medal!!! :D.".replace("<player>", getName())
															   													   .replace("<medalName>", medal.toString())
															   													   .replace("<level>", medal.getMedalLevel().toString())));
		}
	}
	
	/**
	 * Checks if <player> have a <stat> higher enough to upgrade/get the <medal>
	 * 
	 * @param medal - Medal to check
	 * @param stat - The current value of the stat correspondent to the medal
	 * @param player - Player to check
	 */
	public void medalCheck(Medals medal, long stat, Player player) {		
		
		boolean haveTrasition = false;
		
		if(!haveMedal(medal)) {
			if(stat >= medal.getTransition()) {
				Medal newMedal = new Medal(medal);
				newMedal(newMedal);
				
				mongoDB.newMedalOnDataBase(newMedal, player);
				
				haveTrasition = getMedalByMedal(medal).checkLevelTransition(stat);
				
			} else return;
		} else {
			haveTrasition = getMedalByMedal(medal).checkLevelTransition(stat);
			if(!haveTrasition) return;
		}
		
		if(haveTrasition) {
			mongoDB.levelUpMedal(player, getMedalByMedal(medal));
			
			giveNewMedalRole(medal, playerId);
			
			Bukkit.broadcastMessage(
					Util.chat("&b[&a<player>&b]&7 - &aLEVEL UP!").replace("<player>", getName()));
		}
		
		if(player!=null) getMedalByMedal(medal).newMedalEffect(player);
			
		Bukkit.broadcastMessage(
				Util.chat("&b[MineStats]&7 - &a<player>&7 achieved <statCounter> <statName> and received the &c<medalName> &6<level>&7 Medal!!! :D.".replace("<player>", getName())
																																				    .replace("<statCounter>", String.valueOf(stat))
																																					.replace("<statName>",medal.getStatName())
																																				    .replace("<medalName>", medal.toString())
																																					.replace("<level>", getMedalByMedal(medal).getMedalLevel().toString())));
		if(haveAllMedalsGod()) {
			newMedalOnDataBase(Medals.GOD, player);
			giveNewMedalRole(medal, playerId);
		}
	
	}
	
	public void giveNewMedalRole(Medals medal, UUID playerId) {
		Document discUser = mongoDB.getDiscordUserByPlayer(playerId);
		if(discUser!=null && medal.getRoleId()!=0 && getMedalByMedal(medal).getMedalLevel().equals(MLevel.GOD)) {
			Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
    	
    		guild.addRoleToMember(discUser.getString("userId"), guild.getRoleById(medal.getRoleId())).complete();
		}
	}
	
	// Utility methods
	public void startPersisting() {
		timer.scheduleAtFixedRate(new StatsTimerTask(), TIME_BETWEEN_SAVES, TIME_BETWEEN_SAVES);
	}
	
	public void stopPersisting() {
		timer.cancel();
		timer = new Timer();
	}
	
	public void uploadToDataBase() {
		mongoDB.uploadToDataBase(this);
	}
	
	private class StatsTimerTask extends TimerTask {
		
		@Override
		public void run() {
			
			if(!isRealyOnline()) quit();
			else {
				flushSessionPlaytime();
				medalCheck(Medals.TIMEWALKER, getTimePlayed()/60, Main.currentServer.getPlayer(getName()));
				uploadToDataBase();
			}
		}
	}
	
}
