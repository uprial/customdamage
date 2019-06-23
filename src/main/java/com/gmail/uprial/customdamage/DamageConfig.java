package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.config.ConfigReaderSimple;
import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
import com.gmail.uprial.customdamage.schema.HItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

import static com.gmail.uprial.customdamage.config.ConfigReaderSimple.getKey;

final class DamageConfig {

    @SuppressWarnings("FieldCanBeLocal")
    private final String EMPTY_INFO = "[INFORMATION IS NOT AVAILABLE]";
    private final List<HItem> handlers;

    private DamageConfig(List<HItem> handlers) {
        this.handlers = handlers;
    }

    double calculateDamageByEntity(double baseDamage, Entity source, Entity target, DamageCause cause) {
        for (HItem handler : handlers) {
            baseDamage = handler.calculateDamageByEntity(baseDamage, source, target, cause);
        }
        return baseDamage;
    }

    double calculateDamage(double baseDamage, Entity target, DamageCause cause) {
        for (HItem handler : handlers) {
            baseDamage = handler.calculateDamage(baseDamage, target, cause);
        }
        return baseDamage;
    }

    String getPlayerInfo(Player player) {
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
    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static DamageConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {

        List<HItem> handlers = new ArrayList<>();
        Set<String> keys = new HashSet<>();

        List<?> handlersConfig = config.getList("handlers");
        if((handlersConfig == null) || (handlersConfig.size() <= 0)) {
            throw new InvalidConfigException("Empty 'handlers' list");
        }

        int handlersConfigSize = handlersConfig.size();
        for(int i = 0; i < handlersConfigSize; i++) {
            String key = getKey(handlersConfig.get(i), "'handlers'", i);
            String keyLC = key.toLowerCase(Locale.getDefault());
            if(keys.contains(keyLC)) {
                throw new InvalidConfigException(String.format("key '%s' in 'handlers' is not unique", key));
            }
            if(config.get(key) == null) {
                throw new InvalidConfigException(String.format("Null definition of handler '%s' at pos %d", key, i));
            }
            keys.add(keyLC);

            try {
                handlers.add(HItem.getFromConfig(config, customLogger, key));
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
