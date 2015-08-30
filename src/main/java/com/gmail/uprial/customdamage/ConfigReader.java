package com.gmail.uprial.customdamage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.gmail.uprial.customdamage.common.CustomLogger;

public class ConfigReader {
	
	public static List<String> getStringList(FileConfiguration config, CustomLogger customLogger, String key, String title, String name) {
		List<?> lines = config.getList(key);
		if(null != lines) {
			List<String> description = new ArrayList<String>();
			for(int i = 0; i < lines.size(); i++)
				description.add(lines.get(i).toString());
			
			return description;
		} else {
			customLogger.debug(String.format("Empty %s '%s'. Use default value NULL", title, name));
			return null;
		}
	}
	
	public static boolean getBoolean(FileConfiguration config, CustomLogger customLogger, String key, String title, String name, boolean defaultValue) {
		boolean value = defaultValue;
		
		if(null == config.getString(key)) {
			customLogger.debug(String.format("Empty %s '%s'. Use default value %b", title, name, defaultValue));
		} else {
			String strValue = config.getString(key);
			if(strValue.equalsIgnoreCase("true"))
				value = true;
			else if(strValue.equalsIgnoreCase("false"))
				value = false;
			else
				customLogger.error(String.format("Invalid %s '%s'. Use default value %b", title, name, defaultValue));
		}

		return value;
	}

}
