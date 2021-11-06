package org.rage.pluginstats;



import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.rage.pluginstats.commands.DownloadCommand;
import org.rage.pluginstats.commands.GiveMedalCommand;
import org.rage.pluginstats.commands.MedalCommand;
import org.rage.pluginstats.commands.MedalsCommand;
import org.rage.pluginstats.commands.MergeCommand;
import org.rage.pluginstats.commands.PlayerMedalsCommand;
import org.rage.pluginstats.commands.PlayerStatsCommand;
import org.rage.pluginstats.commands.UpdateAllCommand;
import org.rage.pluginstats.commands.UploadAllCommand;
import org.rage.pluginstats.commands.UploadCommand;
import org.rage.pluginstats.listeners.BlockListeners;
import org.rage.pluginstats.listeners.EntityListeners;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.listeners.PlayerListeners;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.PlayerProfile;

/**
 * @author Afonso Batista
 * 2021
 */
public class Main extends JavaPlugin {

	private boolean initialized = false;
	private boolean loadError = false;
	
	public static Server currentServer;
	
	private DataBase mongoDB;
	private Logger log;
	private ListenersController controller;
	
	@Override
	public void onLoad() {
		log = this.getServer().getLogger();
		try {
			
			loadConfig();
			
			Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.SEVERE);  //Para nao ter logs
			
			mongoDB = new DataBase(getConfig());
			currentServer = getServer();
			controller = new ListenersController(new HashMap<UUID, PlayerProfile>(), mongoDB, log);
			initialized = true;
		} catch(Exception e) {
			log.log(Level.INFO, "[MineStats] - Error on enable MongoDB.", e);
			loadError = true;
		}
	}
	
	@Override
	public void onEnable() {
		if(!initialized) onLoad();
		
		if(!loadError) {
			
			saveDefaultConfig();
			
			PluginManager pm = getServer().getPluginManager();
			
			getCommand("upload").setExecutor(new UploadCommand(controller));
			
			getCommand("uploadall").setExecutor(new UploadAllCommand(controller));
			
			getCommand("updateall").setExecutor(new UpdateAllCommand(controller, mongoDB));
			
			getCommand("download").setExecutor(new DownloadCommand(controller, mongoDB));
			
			getCommand("merge").setExecutor(new MergeCommand(controller, mongoDB));
			
			getCommand("givemedal").setExecutor(new GiveMedalCommand(controller, mongoDB));
			
			getCommand("medal").setExecutor(new MedalCommand());
			
			getCommand("medals").setExecutor(new MedalsCommand());
			
			getCommand("playermedals").setExecutor(new PlayerMedalsCommand(controller, mongoDB));
			
			getCommand("stats").setExecutor(new PlayerStatsCommand(controller, mongoDB));
			
			//Block Listener
			pm.registerEvents(new BlockListeners(controller), this);
			//Player Listener
			pm.registerEvents(new PlayerListeners(controller), this);
			//Entity Listener
			pm.registerEvents(new EntityListeners(controller), this);
			
		}
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	@Override
	public void onDisable() {
		if(!loadError) {
			controller.logOutAllPlayers();
			controller.uploadAll();
		}
	}
	
}
