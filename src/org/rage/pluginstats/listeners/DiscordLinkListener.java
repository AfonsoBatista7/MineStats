package org.rage.pluginstats.listeners;

import org.rage.pluginstats.utils.Util;
import org.rage.pluginstats.discord.LinkManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        
        String playerName = linkMan.link(code, userId);
    	
    	event.getChannel().sendMessage(String.format("You were linked with player %s!", playerName)).queue();

        Player player = Bukkit.getPlayer(playerName);
        if (player==null) return;

        player.sendMessage(Util.chat(String.format("&b[MineStats]&7 - You were linked with User &d&l%s&7!", event.getAuthor().getName())));
    	
    }
}
