package org.rage.pluginstats.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author Afonso Batista
 * 2021
 */
public class EntityListeners implements Listener {

	private ListenersController controller;
	
	public EntityListeners(ListenersController controller) {
		this.controller = controller;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		
		Entity victim = event.getEntity();
		Entity attacker = event.getEntity().getKiller();
		
		if(attacker instanceof Player) {
			
			if(victim instanceof Player) controller.die((Player) victim);	
			
			controller.kill((Player) attacker, victim);
		}
		
			
	}
}
