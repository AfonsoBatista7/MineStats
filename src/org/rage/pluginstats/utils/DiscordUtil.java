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
}
