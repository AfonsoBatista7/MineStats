package org.rage.pluginstats.discord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.DiscordUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Guild;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class LinkManager {
	
	private Map<Integer, UUID> playerLinkCodes;
	private DataBaseManager mongoDB;
	private ServerManager serverMan;
	private final int RAND_RATIO = 10000;
	private final String LINK = "link";
	
	public LinkManager(DataBaseManager mongoDB, ServerManager serverMan) {
		playerLinkCodes = new HashMap<>();
		this.mongoDB = mongoDB;
		this.serverMan = serverMan;
	}
	
	public int generateNewCode(UUID playerId) {
		
		Random rand = new Random();
		int code = rand.nextInt(RAND_RATIO);
		
		Iterator<Entry<Integer, UUID>> it = playerLinkCodes.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer,UUID> entry = it.next();
			if(entry.getValue().equals(playerId)) {
				playerLinkCodes.remove(entry.getKey());
				break;
			}
		}
		
		while(playerLinkCodes.containsKey(code))
			code = rand.nextInt(RAND_RATIO);
		
		playerLinkCodes.put(code, playerId);
		
		return code;
	}
	
	public UUID getPlayer(int code) {
		return playerLinkCodes.get(code);
	}
	
	public boolean hasGenCode(int code) {
		return playerLinkCodes.containsKey(code);
	}
	
	public String link(String rawCode, String userId) {
		
		Iterator<Integer> it = playerLinkCodes.keySet().iterator();
		
		int code;
		
		while(it.hasNext()) {
			
			code = it.next();
			
			if(rawCode.contains(String.valueOf(code))) {
				String playerName = serverMan.getPlayerFromHashMap(getPlayer(code)).getName();
				linkProcess(code, userId);
				return String.format("You were linked with player %s!", playerName);
			}
		}
			
		return "The code inserted dont match... try /link on minecraft to link your account";
	}
	
	public void unlink(UUID playerId) {
		
		String userId = mongoDB.getDiscordUserByPlayer(playerId).getString("userId");
		
		mongoDB.updateOneServer(Filters.eq(Stats.PLAYERID.getQuery(), playerId), Updates.unset(LINK));
		mongoDB.updateOneDiscord(Filters.eq("userId", userId), Updates.unset(LINK));
		
		Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
    	
    	guild.removeRoleFromMember(userId, guild.getRoleById(DiscordUtil.getRoleLinkedId())).complete();

	}
	
	private void linkProcess(int code, String userId) {
		
		UUID playerId = getPlayer(code);
		
		mongoDB.updateOneServer(Filters.eq(Stats.PLAYERID.getQuery(), playerId), Updates.set(LINK, userId));
		mongoDB.updateOneDiscord(Filters.eq("userId", userId), Updates.set(LINK, playerId));
		
		playerLinkCodes.remove(code);
		
		Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
    	
    	guild.addRoleToMember(userId, guild.getRoleById(DiscordUtil.getRoleLinkedId())).complete();
		
	}
	
	

}
