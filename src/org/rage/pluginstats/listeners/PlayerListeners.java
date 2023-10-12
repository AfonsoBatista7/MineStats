package org.rage.pluginstats.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class PlayerListeners implements Listener {

	private ListenersController controller;
	
	public PlayerListeners(ListenersController controller) {
		this.controller = controller;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		controller.playerJoin(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		controller.playerQuit(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		if(!event.isCancelled())
			controller.playerKick(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPLayerMove(PlayerMoveEvent event) {
		if(!event.isCancelled() && !samePlace(event.getFrom(), event.getTo()))
			controller.playerMove(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerFish(PlayerFishEvent event) {
		if(!event.isCancelled() && event.getState().equals(State.CAUGHT_FISH))
			controller.playerFishCaught(event.getPlayer(), event.getCaught());
	}
	
	public boolean samePlace(Location l1, Location l2) {
		return (l1.getBlockX() == l2.getBlockX()) &&
			   (l1.getBlockY() == l2.getBlockY()) &&
			   (l1.getBlockZ() == l2.getBlockZ());
	}
}
