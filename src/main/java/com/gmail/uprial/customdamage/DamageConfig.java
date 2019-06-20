package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.config.ConfigReader;
import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
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
    public static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReader.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static DamageConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {

        List<HItem> handlers = new ArrayList<>();
        Map<String,Integer> keys = new HashMap<>();

        List<?> handlersConfig = config.getList("handlers");
        if((handlersConfig == null) || (handlersConfig.size() <= 0)) {
            throw new InvalidConfigException("Empty 'handlers' list");
        }

        int handlersConfigSize = handlersConfig.size();
        for(int i = 0; i < handlersConfigSize; i++) {
            Object item = handlersConfig.get(i);
            if(item == null) {
                throw new InvalidConfigException(String.format("Null key in 'handlers' at pos %d", i));
            }
            String key = item.toString();
            if(key.length() < 1) {
                throw new InvalidConfigException(String.format("Empty key in 'handlers' at pos %d", i));
            }
            String keyLC = key.toLowerCase(Locale.getDefault());
            if(keys.containsKey(keyLC)) {
                throw new InvalidConfigException(String.format("key '%s' in 'handlers' is not unique", key));
            }

            if(config.getConfigurationSection(key) == null) {
                throw new InvalidConfigException(String.format("Null definition of handler '%s' at pos %d", key, i));
            }

            try {
                HItem handler = HItem.getFromConfig(config, customLogger, key);
                handlers.add(handler);
                keys.put(keyLC, 1);
            } catch (InvalidConfigException e) {
                customLogger.error(e.getMessage());
            }
        }

        if(handlers.size() < 1) {
            throw new InvalidConfigException("There are no valid handlers definitions");
        }

        return new DamageConfig(handlers);
    }
}
