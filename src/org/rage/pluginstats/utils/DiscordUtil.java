package org.rage.pluginstats.utils;

import org.rage.pluginstats.Main;
import org.rage.pluginstats.mongoDB.DataBase;

import net.dv8tion.jda.api.JDA;

public class DiscordUtil {

	public static JDA getJDA() {
		return Main.getJda();
	}
	
	public static long getGuildId() {
		return DataBase.getConfig().getLong("discordGuildId");
	}
	
	public static long getRoleLinkedId() {
		return DataBase.getConfig().getLong("discordRoleLinkedId");
	}
	
	public static long getChannelId() {
		return DataBase.getConfig().getLong("channelId");
	}
	
	public static String buildDiscordToMinecraft(String userName, String message) {		
		return Util.chat(String.format("[&9&lDiscord&r] <%s> &f%s",userName, message));
	}
	
	public static String buildMinecraftToDiscord(String message) {
		return "";
	}
}
