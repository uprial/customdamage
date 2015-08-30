package com.gmail.uprial.customdamage;


import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.uprial.customdamage.common.CustomLogger;

public class DamageListener implements Listener {
	private final CustomDamage plugin;
	private final CustomLogger customLogger;
	
	public DamageListener(CustomDamage plugin, CustomLogger customLogger) {
		this.plugin = plugin;
		this.customLogger = customLogger;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Entity source = event.getDamager();
		Entity target = event.getEntity();
		DamageCause cause = event.getCause();
		Double baseDamage = event.getDamage();
		
		Double calculatedDamage = plugin.getDamageConfig().calculateDamageByEntity(baseDamage, source, target, cause);
		event.setDamage(calculatedDamage);
		
		customLogger.debug(String.format("%s > %s (%s): %.2f > %.2f",
											source.getType().toString(), target.getType().toString(), cause.toString(),
											baseDamage, calculatedDamage));
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void EntityDamageEvent(EntityDamageEvent event) {
		Entity target = event.getEntity();
		DamageCause cause = event.getCause();
		Double baseDamage = event.getDamage();
		
		Double calculatedDamage = plugin.getDamageConfig().calculateDamage(baseDamage, target, cause);
		event.setDamage(calculatedDamage);
		
		customLogger.debug(String.format("> %s (%s): %.2f > %.2f",
											target.getType().toString(), cause.toString(),
											baseDamage, calculatedDamage));
	}
}
