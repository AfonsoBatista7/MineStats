package org.rage.pluginstats.listeners;

import org.rage.pluginstats.discord.LinkManager;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */

public class DiscordLinkListener extends ListenerAdapter {

	private LinkManager linkMan;
	
	public DiscordLinkListener(LinkManager linkMan) {
		this.linkMan = linkMan;
	}
	
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
    	
        //Don't process messages sent by the bot
    	if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) return;
    	
    	String userId = event.getAuthor().getId(), code = event.getMessage().getContentRaw();
    	
    	event.getChannel().sendMessage(linkMan.link(code, userId)).queue();
    	
    }
}
