package com.gmail.uprial.customdamage;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.uprial.customdamage.common.CustomLogger;

public final class CustomDamage extends JavaPlugin {
	private DamageConfig damageConfig;
	private CustomLogger customLogger;
	private DamageListener damageListener;
	
    @Override
    public void onEnable() {
    	saveDefaultConfig();
    	customLogger = new CustomLogger(getLogger());
    	damageConfig = new DamageConfig(getConfig(), customLogger);
    	damageListener = new DamageListener(this, customLogger);

    	getServer().getPluginManager().registerEvents(damageListener, this);
    	getCommand("customdamage").setExecutor(new CustomDamageCommandExecutor(this, customLogger));
    	customLogger.info("Plugin enabled");
    }
    
    @Override
    public void onDisable() {
    	HandlerList.unregisterAll(damageListener);
    	customLogger.info("Plugin disabled");
    }
    
    public DamageConfig getDamageConfig() {
    	return damageConfig;
    }
    
    public void reloadDamageConfig() {
		reloadConfig();
		damageConfig = new DamageConfig(getConfig(), customLogger);
    }
    
    public Player getPlayerByName(String playerName) {
    	Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
    	for (Player player : onlinePlayers)
			if(player.getName().equalsIgnoreCase(playerName))
				return player;
		
		return null;
    }
    
}
