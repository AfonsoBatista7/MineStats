package org.rage.pluginstats;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.rage.pluginstats.commands.DiscordLinkCommand;
import org.rage.pluginstats.commands.DiscordUnLinkCommand;
import org.rage.pluginstats.commands.DownloadCommand;
import org.rage.pluginstats.commands.GiveMedalCommand;
import org.rage.pluginstats.commands.MedalCommand;
import org.rage.pluginstats.commands.MedalsCommand;
import org.rage.pluginstats.commands.MergeCommand;
import org.rage.pluginstats.commands.PlayerMedalsCommand;
import org.rage.pluginstats.commands.PlayerStatsCommand;
import org.rage.pluginstats.commands.TagsCommand;
import org.rage.pluginstats.commands.UpdateAllCommand;
import org.rage.pluginstats.commands.UploadAllCommand;
import org.rage.pluginstats.commands.UploadCommand;
import org.rage.pluginstats.discord.LinkManager;
import org.rage.pluginstats.listeners.BlockListeners;
import org.rage.pluginstats.listeners.EntityListeners;
import org.rage.pluginstats.listeners.ListenersController;
import org.rage.pluginstats.listeners.MessageListener;
import org.rage.pluginstats.listeners.PlayerListeners;
import org.rage.pluginstats.mongoDB.DataBase;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.listeners.DiscordChatListener;
import org.rage.pluginstats.listeners.DiscordLinkListener;

import com.google.common.collect.Sets;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class Main extends JavaPlugin {

	private boolean initialized = false;
	private boolean loadError = false;
	
	public static JDA jda = null;
	public static final Set<GatewayIntent> api = Sets.immutableEnumSet(EnumSet.of(
													GatewayIntent.GUILD_MEMBERS,
													GatewayIntent.GUILD_BANS,
													GatewayIntent.GUILD_EMOJIS,
													GatewayIntent.GUILD_VOICE_STATES,
													GatewayIntent.GUILD_MESSAGES,
													GatewayIntent.DIRECT_MESSAGES
												));
		
	public static Server currentServer;
	
	private Logger log;
	private ListenersController controller;
	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private LinkManager linkMan;
	
	@Override
	public void onLoad() {
		
		log = this.getServer().getLogger();
		try {
			loadConfig();
			
			Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.SEVERE);  //TO NOT HAVE LOGS ON CONSOLE
			
			currentServer = getServer();
			serverMan = new ServerManager(new DataBase(getConfig()), log, this);
			mongoDB = serverMan.getDataBaseManager();
			linkMan = new LinkManager(mongoDB, serverMan);
			controller = new ListenersController(mongoDB, serverMan);
			
			Thread initThread = new Thread(this::init, "[MineStats] - Initialization Discord Bot");
	        initThread.setUncaughtExceptionHandler((t, e) -> {
	            getLogger().severe("[MineStats] - failed to load Discord functions properly: " + e.getMessage());
	        });
	        initThread.start();
			
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
			
			getCommand("upload").setExecutor(new UploadCommand(mongoDB));
			
			getCommand("uploadall").setExecutor(new UploadAllCommand(serverMan));
			
			getCommand("updateall").setExecutor(new UpdateAllCommand(mongoDB, serverMan));
			
			getCommand("download").setExecutor(new DownloadCommand(mongoDB, serverMan));
			
			getCommand("merge").setExecutor(new MergeCommand(mongoDB, serverMan));
			
			getCommand("givemedal").setExecutor(new GiveMedalCommand(mongoDB, serverMan));
			
			getCommand("medal").setExecutor(new MedalCommand());
			
			getCommand("medals").setExecutor(new MedalsCommand(mongoDB, serverMan));
			
			getCommand("playermedals").setExecutor(new PlayerMedalsCommand(mongoDB, serverMan));
			
			getCommand("stats").setExecutor(new PlayerStatsCommand(mongoDB, serverMan));
			
			getCommand("tag").setExecutor(new TagsCommand(this.getServer(), mongoDB, serverMan));
			
			getCommand("link").setExecutor(new DiscordLinkCommand(mongoDB, serverMan, linkMan));
			
			getCommand("unlink").setExecutor(new DiscordUnLinkCommand(mongoDB, serverMan, linkMan));

			
			pm.registerEvents(new BlockListeners(controller), this);
			pm.registerEvents(new PlayerListeners(controller), this);
			pm.registerEvents(new EntityListeners(controller), this);
			pm.registerEvents(new MessageListener(), this);
			
		}
	}
	
	public void init() {
		try {
			jda = JDABuilder.create(api)
				.setToken(getConfig().getString("token"))
				.addEventListeners(new DiscordChatListener(mongoDB))
				.addEventListeners(new DiscordLinkListener(linkMan))
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
				.build();
			
			jda.awaitReady();
		} catch (LoginException e) {
			log.log(Level.SEVERE, "[MineStats] - The Discord bot Token is incorrect.", e);
		} catch(InterruptedException e) {
			log.log(Level.SEVERE, "[MineStats] - An error occurred while connecting.", e);
		}
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	@Override
	public void onDisable() {
		if(!loadError) {

			 if (jda != null) {
				 jda.getEventManager().getRegisteredListeners().forEach(listener -> jda.getEventManager().unregister(listener));
				 jda.shutdownNow();
			 }
			
			saveConfig();
			serverMan.logOutAllPlayers();
			serverMan.uploadAll();
		}
	}
	
	public static JDA getJda() {
		return jda;
    }
	
}
