package org.rage.pluginstats.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rage.pluginstats.Main;
import org.rage.pluginstats.mongoDB.DataBase;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordUtil {

	public static JDA getJda() {
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
	
	private static final Pattern USER_MENTION_PATTERN = Pattern.compile("(<@!?([0-9]{16,20})>)");
    private static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("(<#([0-9]{16,20})>)");
    private static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("(<@&([0-9]{16,20})>)");
    private static final Pattern EMOTE_MENTION_PATTERN = Pattern.compile("(<a?:([a-zA-Z]{2,32}):[0-9]{16,20}>)");

    /**
     * Converts Discord-compatible <@12345742934270> mentions to human readable @mentions
     * @param message the message
     * @return the converted message
     */
    public static String convertMentionsToNames(String message) {
        Matcher userMatcher = USER_MENTION_PATTERN.matcher(message);
        while (userMatcher.find()) {
            String mention = userMatcher.group(1);
            String userId = userMatcher.group(2);
            User user = getUserById(userId);
            message = message.replace(mention, user != null ? user.getName() : mention);
        }

        Matcher channelMatcher = CHANNEL_MENTION_PATTERN.matcher(message);
        while (channelMatcher.find()) {
            String mention = channelMatcher.group(1);
            String channelId = channelMatcher.group(2);
            TextChannel channel = getTextChannelById(channelId);
            message = message.replace(mention, channel != null ? channel.getName() : mention);
        }

        Matcher roleMatcher = ROLE_MENTION_PATTERN.matcher(message);
        while (roleMatcher.find()) {
            String mention = roleMatcher.group(1);
            String roleId = roleMatcher.group(2);
            Role role = getRole(roleId);
            message = message.replace(mention, role != null ? role.getName() : mention);
        }

        Matcher emoteMatcher = EMOTE_MENTION_PATTERN.matcher(message);
        while (emoteMatcher.find()) {
            message = message.replace(emoteMatcher.group(1), ":" + emoteMatcher.group(2) + ":");
        }

        return message;
    }
    
    public static User getUserById(String userId) {
            return getJda().getUserById(userId);
    }
    
    public static TextChannel getTextChannelById(String userId) {
        return getJda().getTextChannelById(userId);
    }
    
    public static Role getRole(String userId) {
        return getJda().getRoleById(userId);
}
}
