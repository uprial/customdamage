package com.gmail.uprial.customdamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.uprial.customdamage.common.CustomLogger;

public class CustomDamageCommandExecutor implements CommandExecutor {
	private final CustomDamage plugin;
	private final CustomLogger customLogger;

	public CustomDamageCommandExecutor(CustomDamage plugin, CustomLogger customLogger) {
		this.plugin = plugin;
		this.customLogger = customLogger;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (command.getName().equalsIgnoreCase("customdamage")) {
			if((args.length >= 1) && (args[0].equalsIgnoreCase("reload"))) {
	    		if (sender.hasPermission("customdamage.reload")) {
	    			plugin.reloadDamageConfig();
	    			customLogger.userInfo(sender, "CustomDamage config reloaded.");
	    			return true;
	    		}
			}
			else if((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
				String Help = "==== CustomDamage help ====";
				if (sender.hasPermission("customdamage.reload"))
					Help += "\n/customdamage reload - reload config from disk";
				Help += "\n";
				customLogger.userInfo(sender, Help);
    			return true;
			}
    	} 
    	return false; 
    }    
}
