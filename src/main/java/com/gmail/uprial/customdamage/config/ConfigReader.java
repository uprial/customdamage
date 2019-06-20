package com.gmail.uprial.customdamage.config;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigReader {
    public static String getKey(Object item, String title, int i) throws InvalidConfigException {
        if (item == null) {
            throw new InvalidConfigException(String.format("Null key in %s at pos %d", title, i));
        }
        if (!(item instanceof String)) {
            throw new InvalidConfigException(String.format("Key '%s' in %s at pos %d is not a string", item.toString(), title, i));
        }
        String key = item.toString();
        if (key.length() < 1) {
            throw new InvalidConfigException(String.format("Empty key in %s at pos %d", title, i));
        }
        return key;
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "SameParameterValue"})
    public static String getStringUnsafe(FileConfiguration config, CustomLogger customLogger, String key, String title) {
        String string = config.getString(key);

        if(string == null) {
            customLogger.debug(String.format("Null/Empty %s", title));
            return null;
        }

        return string;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static List<String> getStringList(FileConfiguration config, CustomLogger customLogger, String key, String title) {
        List<?> lines = config.getList(key);
        if(lines != null) {
            List<String> strings = new ArrayList<>();
            for (Object line : lines) {
                strings.add(line.toString());
            }

            return strings;
        } else {
            customLogger.debug(String.format("Empty %s. Use default value NULL", title));
            return null;
        }
    }

    @SuppressWarnings({"BooleanParameter", "BooleanMethodNameMustStartWithQuestion"})
    public static boolean getBoolean(FileConfiguration config, CustomLogger customLogger, String key, String title, boolean defaultValue) throws InvalidConfigException {
        String strValue = config.getString(key);

        if(strValue == null) {
            customLogger.debug(String.format("Empty %s. Use default value %b", title, defaultValue));
            return defaultValue;
        } else if(strValue.equalsIgnoreCase("true")) {
            return true;
        } else if(strValue.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new InvalidConfigException(String.format("Invalid %s", title));
        }
    }
}