package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.common.CustomLogger;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigReader {
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static List<String> getStringList(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
        List<?> lines = config.getList(key);
        if(lines != null) {
            List<String> description = new ArrayList<>();
            int linesSize = lines.size();
            //noinspection ForLoopReplaceableByForEach
            for(int i = 0; i < linesSize; i++) {
                description.add(lines.get(i).toString());
            }

            return description;
        } else {
            customLogger.debug(String.format("Empty %s '%s'. Use default value NULL", title, name));
            return null;
        }
    }

    @SuppressWarnings({"BooleanParameter", "SameParameterValue", "BooleanMethodNameMustStartWithQuestion"})
    public static boolean getBoolean(FileConfiguration config, CustomLogger customLogger, String key, String title, String name, boolean defaultValue) {
        boolean value = defaultValue;

        if(config.getString(key) == null) {
            customLogger.debug(String.format("Empty %s '%s'. Use default value %b", title, name, defaultValue));
        } else {
            String strValue = config.getString(key);
            if(strValue.equalsIgnoreCase("true")) {
                value = true;
            } else if(strValue.equalsIgnoreCase("false")) {
                value = false;
            } else {
                customLogger.error(String.format("Invalid %s '%s'. Use default value %b", title, name, defaultValue));
            }
        }

        return value;
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
