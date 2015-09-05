package com.gmail.uprial.customdamage;


import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;

import com.gmail.uprial.customdamage.common.CustomLogger;

public class DamageListener implements Listener {
	private final String UNKNOWN_SOURCE = "?";
	private final CustomDamage plugin;
	private final CustomLogger customLogger;
	
	public DamageListener(CustomDamage plugin, CustomLogger customLogger) {
		this.plugin = plugin;
		this.customLogger = customLogger;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void EntityDamageEvent(EntityDamageEvent event) {
		Entity target = event.getEntity();
		DamageCause cause = event.getCause();
		Double baseDamage = event.getDamage();
		
		Double calculatedDamage;
		String stringSource;
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent)event;
			Entity source = getRealSource(byEntityEvent.getDamager());

			if (null != source) {
				calculatedDamage = plugin.getDamageConfig().calculateDamageByEntity(baseDamage, source, target, cause);
				stringSource = getEntityName(source);
			} else {
				calculatedDamage = plugin.getDamageConfig().calculateDamage(baseDamage, target, cause);
				stringSource = UNKNOWN_SOURCE;
			}
		} else if (event instanceof EntityDamageByBlockEvent) {
			calculatedDamage = plugin.getDamageConfig().calculateDamage(baseDamage, target, cause);
			
			EntityDamageByBlockEvent byBlockEvent = (EntityDamageByBlockEvent)event;
			Block source = byBlockEvent.getDamager();
			if (null != source)
				stringSource = source.getType().toString();
			else
				stringSource = UNKNOWN_SOURCE;
		} else {
			calculatedDamage = plugin.getDamageConfig().calculateDamage(baseDamage, target, cause);
			stringSource = UNKNOWN_SOURCE;
		}
		
		event.setDamage(calculatedDamage);
		
		customLogger.debug(String.format("%s > %s (%s): %.2f > %.2f",
											stringSource, getEntityName(target), cause.toString(),
											baseDamage, calculatedDamage));
	}
	
	private String getEntityName(Entity entity) {
		if  (entity instanceof Player) {
			Player player = (Player)entity;
			return String.format("%s:%s", entity.getType().toString(), player.getName());
		} else
			return entity.getType().toString();
	}
	
	private Entity getRealSource(Entity source) {
		if (source instanceof Projectile) {
			Projectile projectile = (Projectile)source;
			ProjectileSource projectileSource = projectile.getShooter();
			if (projectileSource instanceof Entity) {
				Entity projectileEntity = (Entity)projectileSource;
				source = projectileEntity;
			}
		}
		return source;
	}
	
}
