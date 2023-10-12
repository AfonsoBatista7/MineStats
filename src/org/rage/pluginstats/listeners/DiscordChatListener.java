package org.rage.pluginstats.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.DiscordUtil;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class DiscordChatListener extends ListenerAdapter {
	
	private DataBaseManager mongoDB;
	
	public DiscordChatListener(DataBaseManager mongoDB) {
		this.mongoDB = mongoDB;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
	
		
		if(event.getMember() == null || DiscordUtil.getJda() == null || event.getAuthor().equals(DiscordUtil.getJda().getSelfUser()))
	         	return;
		
		if(event.getChannel().getIdLong() != DiscordUtil.getChannelId()) return;
		
		event.getMessage().suppressEmbeds(true);
		
		String message = event.getMessage().getContentRaw();
		User user = event.getAuthor();
		Guild guild = event.getGuild();
		
		if(message.length() > mongoDB.getConfig().getInt("maxDiscordMessage")) {
	            event.getMessage().addReaction("\uD83D\uDCAC").queue();
	            message = message.substring(0, mongoDB.getConfig().getInt("maxDiscordMessage"));
	    }
		
		List<Member> members = guild.getMembersWithRoles(guild.getRoleById(DiscordUtil.getRoleLinkedId()));
		
		//If the message author dont have the Liked Role.
		if(!members.contains(guild.getMemberById(user.getIdLong()))) return;
		
		if (StringUtils.isBlank(EmojiParser.removeAllEmojis(message))) return;
		
		message = DiscordUtil.convertMentionsToNames(message);
		
		String minecraftMessage = DiscordUtil.buildDiscordToMinecraft(mongoDB.getPlayerByDiscordUser(user.getId()).getString(Stats.NAME.getQuery()), message);
		
		Bukkit.broadcastMessage(minecraftMessage);
	}

}
