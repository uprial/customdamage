package com.gmail.uprial.customdamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			else if((args.length >= 1) && (args[0].equalsIgnoreCase("info"))) {
				boolean error = false;
    			
				String playerName = null;
				if (args.length >= 2)
					if (sender.hasPermission("customdamage.info"))
						playerName = args[1];
					else {
						customLogger.userError(sender, "You have no permissions to use this command");
						error = true;
					}
				else
					playerName = sender.getName();

				Player player = null;
				if (!error) {
					player = plugin.getPlayerByName(playerName);
	    			if(null == player) {
	    				customLogger.userError(sender, String.format("Player '%s' is not exists.", playerName));
	    				error = true;
	    			}
				}
				if (!error) {
					String info = plugin.getDamageConfig().getPlayerInfo(player);
					customLogger.userInfo(sender,  info);
				}
				
				return true;
			}
			else if((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
				String Help = "==== CustomDamage help ====\n";

				if (sender.hasPermission("customdamage.reload"))
					Help += "/customdamage reload - reload config from disk\n";
				
				Help += "/customdamage info - show damage modifiers\n";

				if (sender.hasPermission("customdamage.info"))
					Help += "/customdamage info <player> - show damage modifiers of <player>\n";
				
				customLogger.userInfo(sender, Help);
    			return true;
			}
    	} 
    	return false; 
    }    
}
