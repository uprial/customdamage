package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.common.CustomLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;

import static com.gmail.uprial.customdamage.CustomDamageCommandExecutor.COMMAND_NS;

public final class CustomDamage extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private DamageConfig damageConfig = null;
    private CustomLogger consoleLogger = null;
    private DamageListener damageListener = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        damageConfig = loadConfig(getConfig(), consoleLogger);
        damageListener = new DamageListener(this, consoleLogger);

        getServer().getPluginManager().registerEvents(damageListener, this);
        getCommand(COMMAND_NS).setExecutor(new CustomDamageCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(damageListener);
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public DamageConfig getDamageConfig() {
        return damageConfig;
    }

    public void reloadDamageConfig(CustomLogger userLogger) {
        reloadConfig();
        damageConfig = loadConfig(getConfig(), userLogger, consoleLogger);
    }

    public Player getPlayerByName(String playerName) {
        Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (player.getName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }

        return null;
    }

    private static DamageConfig loadConfig(FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    private static DamageConfig loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        boolean isDebugMode = DamageConfig.isDebugMode(config, mainLogger);
        mainLogger.setDebugMode(isDebugMode);
        if(secondLogger != null) {
            secondLogger.setDebugMode(isDebugMode);
        }

        return DamageConfig.getFromConfig(config, mainLogger);
    }
}
