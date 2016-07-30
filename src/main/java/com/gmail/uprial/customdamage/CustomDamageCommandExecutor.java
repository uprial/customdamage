package com.gmail.uprial.customdamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.uprial.customdamage.common.CustomLogger;

class CustomDamageCommandExecutor implements CommandExecutor {
    public static final String COMMAND_NS = "customdamage";

    private final CustomDamage plugin;

    CustomDamageCommandExecutor(CustomDamage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(COMMAND_NS)) {
            CustomLogger customLogger = new CustomLogger(plugin.getLogger(), sender);

            if((args.length >= 1) && (args[0].equalsIgnoreCase("reload"))) {
                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    plugin.reloadDamageConfig(customLogger);
                    customLogger.info("CustomDamage config reloaded.");
                    return true;
                }
            }
            else if((args.length >= 1) && (args[0].equalsIgnoreCase("info"))) {
                boolean hasPermissions = true;

                String playerName = null;
                if (args.length >= 2) {
                    if (sender.hasPermission(COMMAND_NS + ".info")) {
                        playerName = args[1];
                    } else {
                        customLogger.info("You have no permissions to use this command");
                        hasPermissions = false;
                    }
                } else {
                    playerName = sender.getName();
                }

                Player player = null;
                if (hasPermissions) {
                    player = plugin.getPlayerByName(playerName);
                    if(player == null) {
                        customLogger.info(String.format("Player '%s' is not exists.", playerName));
                        hasPermissions = false;
                    }
                }
                if (hasPermissions) {
                    String info = plugin.getDamageConfig().getPlayerInfo(player);
                    customLogger.info(info);
                }

                return true;
            }
            else if((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                String helpString = "==== CustomDamage help ====\n";

                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    helpString += '/' + COMMAND_NS + " reload - reload config from disk\n";
                }

                helpString += '/' + COMMAND_NS + " info - show damage modifiers\n";

                if (sender.hasPermission(COMMAND_NS + ".info")) {
                    helpString += '/' + COMMAND_NS + " info <player> - show damage modifiers of <player>\n";
                }

                customLogger.info(helpString);
                return true;
            }
        }
        return false;
    }
}
