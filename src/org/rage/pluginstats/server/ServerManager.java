package org.rage.pluginstats.server;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.utils.Util;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class ServerManager {
	
	private HashMap<UUID, ServerPlayer> stats;
	private DataBaseManager mongoDB;
	private String currentVertion;
	
	public ServerManager(DataBase mongoDB, Logger log) {
		this.stats = new HashMap<UUID, ServerPlayer>();
		this.mongoDB = new DataBaseManager(mongoDB, log, this);
		this.currentVertion = getServerVersion();
	}
	
	public DataBaseManager gerDataBaseManager() {
		return mongoDB;
	}
	
	public String getCurrentServerVersion() {
		return currentVertion;
	}
	
	/**
	 * @return Minecraft version the server is currently running on.
	 */
	public String getServerVersion() {
		String version = Main.currentServer.getVersion();
		int start = version.indexOf("MC: ") + 4;
		int end = version.length() - 1;
		Bukkit.broadcastMessage(
				Util.chat("Version: <v>".replace("<v>", version.substring(start, end))));
		return version.substring(start, end);
	}
	
	/**
	 * Uploads all players statistics to database
	 */
	public void uploadAll() {
		for(ServerPlayer ps : stats.values()) {
			ps.flushSessionPlaytime();
			ps.stopPersisting();
			mongoDB.uploadToDataBase(ps);
			ps.startPersisting();
		}
	}
	
	public void logOutAllPlayers() {
		synchronized(stats) {
			for(ServerPlayer pp : stats.values())
				pp.quit();
		}
		uploadAll();
	}
	
	public boolean playerAlreadyInServer(UUID playerId) {
		return stats.containsKey(playerId);
	}
	
	public ServerPlayer getPlayerStats(UUID playerId) {
		return stats.get(playerId);
	}
	
	public void newPlayerOnServer(ServerPlayer pp) {
		stats.put(pp.getPlayerID(), pp);
	}
	
	public ServerPlayer getPlayerFromHashMap(UUID player) {
		return stats.get(player);
	}
	
	public ServerPlayer deleteFromHashMap(UUID player) {
		return stats.remove(player);
	}
	
}
