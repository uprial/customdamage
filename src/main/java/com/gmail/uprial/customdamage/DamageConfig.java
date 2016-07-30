package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.schema.HItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

public final class DamageConfig {

    @SuppressWarnings("FieldCanBeLocal")
    private final String EMPTY_INFO = "[INFORMATION IS NOT AVAILABLE]";
    private final List<HItem> handlers;

    private DamageConfig(List<HItem> handlers) {
        this.handlers = handlers;
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

    public String getPlayerInfo(Player player) {
        StringBuilder info = new StringBuilder();
        for (HItem handler : handlers) {
            if (handler.isUserVisible()) {
                info.append(handler.getPlayerInfo(player));
                info.append('\n');
            }
        }
        if (info.length() <= 0) {
            info.append(EMPTY_INFO);
        }

        return info.toString();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) {
        return ConfigReader.getBoolean(config, customLogger, "debug", "value flag", "debug", false);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static DamageConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) {

        List<HItem> handlers = new ArrayList<>();
        Map<String,Integer> keys = new HashMap<>();

        List<?> handlersConfig = config.getList("handlers");
        if((handlersConfig == null) || (handlersConfig.size() <= 0)) {
            customLogger.error("Empty 'handlers' list");
            return null;
        }

        int handlersConfigSize = handlersConfig.size();
        for(int i = 0; i < handlersConfigSize; i++) {
            Object item = handlersConfig.get(i);
            if(item == null) {
                customLogger.error(String.format("Null key in 'handlers' at pos %d", i));
                continue;
            }
            String key = item.toString();
            if(key.length() < 1) {
                customLogger.error(String.format("Empty key in 'handlers' at pos %d", i));
                continue;
            }
            String keyLC = key.toLowerCase(Locale.getDefault());
            if(keys.containsKey(keyLC)) {
                customLogger.error(String.format("key '%s' in 'handlers' is not unique", key));
                continue;
            }

            if(config.getConfigurationSection(key) == null) {
                customLogger.error(String.format("Null definition of handler '%s' at pos %d", key, i));
                continue;
            }

            HItem handler = HItem.getFromConfig(config, customLogger, key);
            if(handler == null) {
                continue;
            }

            handlers.add(handler);
            keys.put(keyLC, 1);
        }

        if(handlers.size() < 1) {
            customLogger.error("There are no valid handlers definitions");
            return null;
        }

        return new DamageConfig(handlers);
    }
}
