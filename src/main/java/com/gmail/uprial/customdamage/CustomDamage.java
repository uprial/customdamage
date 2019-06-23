package com.gmail.uprial.customdamage;

import com.gmail.uprial.customdamage.common.CustomLogger;
import com.gmail.uprial.customdamage.config.InvalidConfigException;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        damageConfig = loadConfig(getConfig(), consoleLogger);

        getServer().getPluginManager().registerEvents(new DamageListener(this, consoleLogger), this);
        getCommand(COMMAND_NS).setExecutor(new CustomDamageCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
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

    DamageConfig getDamageConfig() {
        return damageConfig;
    }

    void reloadDamageConfig(CustomLogger userLogger) {
        reloadConfig();
        damageConfig = loadConfig(getConfig(), userLogger, consoleLogger);
    }

    Player getPlayerByName(String playerName) {
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
        DamageConfig damageConfig = null;
        try {
            boolean isDebugMode = DamageConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            damageConfig = DamageConfig.getFromConfig(config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }

        return damageConfig;
    }
}
