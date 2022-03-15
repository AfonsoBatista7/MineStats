package org.rage.pluginstats.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

import org.bukkit.Bukkit;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.DiscordUtil;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;


public class DiscordChatListener extends ListenerAdapter {
	
	private DataBaseManager mongoDB;
	
	public DiscordChatListener(DataBaseManager mongoDB) {
		this.mongoDB = mongoDB;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
	
		
		if (event.getMember() == null || DiscordUtil.getJDA() == null || event.getAuthor().equals(DiscordUtil.getJDA().getSelfUser()))
	         	return;
		
		if(event.getChannel().getIdLong() != DiscordUtil.getChannelId()) return;
		
		String message = event.getMessage().getContentRaw();
		User user = event.getAuthor();
		Guild guild = event.getGuild();
		
		List<Member> members = guild.getMembersWithRoles(guild.getRoleById(DiscordUtil.getRoleLinkedId()));
		
		//If the message author dont have the Liked Role.
		if(!members.contains(guild.getMemberById(user.getIdLong()))) return;
		
		String minecraftMessage = DiscordUtil.buildDiscordToMinecraft(mongoDB.getPlayerByDiscordUser(user.getId()).getString(Stats.NAME.getQuery()), message);
		
		Bukkit.broadcastMessage(minecraftMessage);
	}

}
