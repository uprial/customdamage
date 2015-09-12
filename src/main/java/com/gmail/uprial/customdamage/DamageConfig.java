package com.gmail.uprial.customdamage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.schema.HItem;

public class DamageConfig {

	private Map<String,Integer> keys;
	private List<HItem> handlers;
	
    public DamageConfig(FileConfiguration config, CustomLogger customLogger) {
    	readConfig(config, customLogger);
    }
    
    public double calculateDamageByEntity(double baseDamage, Entity source, Entity target, DamageCause cause) {
    	for (HItem handler : handlers) {
    		baseDamage = handler.calculateDamageByEntity(baseDamage, source, target, cause);
    	}
    	return baseDamage;
    }
    
    public double calculateDamage(double baseDamage, Entity target, DamageCause cause) {
    	for (HItem handler : handlers) {
    		baseDamage = handler.calculateDamage(baseDamage, target, cause);
    	}
    	return baseDamage;
    }

    private void readConfig(FileConfiguration config, CustomLogger customLogger) {
		
		handlers = new ArrayList<HItem>();
		keys = new HashMap<String,Integer>();
		
		boolean debug = ConfigReader.getBoolean(config, customLogger, "debug", "value flag", "debug", false);
		customLogger.setDebugMode(debug);
		
		List<?> handlersConfig = config.getList("handlers");
		if((null == handlersConfig) || (handlersConfig.size() <= 0)) {
			customLogger.error("Empty 'handlers' list");
			return;
		}
		
		for(int i = 0; i < handlersConfig.size(); i++) {
			Object item = handlersConfig.get(i);
			if(null == item) {
				customLogger.error(String.format("Null key in 'handlers' at pos %d", i));
				continue;
			}
			String key = item.toString();
			if(key.length() < 1) {
				customLogger.error(String.format("Empty key in 'handlers' at pos %d", i));
				continue;
			}
			if(keys.containsKey(key.toLowerCase())) {
				customLogger.error(String.format("key '%s' in 'handlers' is not unique", key));
				continue;
			}

			if(null == config.getConfigurationSection(key)) {
				customLogger.error(String.format("Null definition of handler '%s' at pos %d", key, i));
				continue;
			}
		
			HItem handler = HItem.getFromConfig(config, customLogger, key);
			if(null == handler)
				continue;
			
			handlers.add(handler);
			keys.put(key.toLowerCase(), 1);
		}
		
		if(handlers.size() < 1)
			customLogger.error("There are no valid handlers definitions");
		
	}
}
