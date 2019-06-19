package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigReader {
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static List<String> getStringList(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
        List<?> lines = config.getList(key);
        if(lines != null) {
            List<String> description = new ArrayList<>();
            for(Object line : lines) {
                description.add(line.toString());
            }

            return description;
        } else {
            customLogger.debug(String.format("Empty %s '%s'. Use default value NULL", title, name));
            return null;
        }
    }

    @SuppressWarnings({"BooleanParameter", "SameParameterValue", "BooleanMethodNameMustStartWithQuestion"})
    public static boolean getBoolean(FileConfiguration config, CustomLogger customLogger, String key, String title, String name, boolean defaultValue) throws InvalidConfigException {
        String strValue = config.getString(key);

        if(strValue == null) {
            customLogger.debug(String.format("Empty %s '%s'. Use default value %b", title, name, defaultValue));
            return defaultValue;
        } else if(strValue.equalsIgnoreCase("true")) {
            return true;
        } else if(strValue.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new InvalidConfigException(String.format("Invalid %s '%s'", title, name));
        }
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "SameParameterValue"})
    public static String getString(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
        String string = config.getString(key);

        if(string == null) {
            customLogger.debug(String.format("Null/Empty %s '%s'", title, name));
            return null;
        }

        return string;
    }
}
