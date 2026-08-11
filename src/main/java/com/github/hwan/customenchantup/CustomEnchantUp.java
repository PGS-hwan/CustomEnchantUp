package com.github.hwan.customenchantup;

import java.io.File;
import me.yic.xconomy.api.XConomyAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.hwan.customenchantup.commands.CommandManager;
import com.github.hwan.customenchantup.commands.CommandTabCompleter;
import com.github.hwan.customenchantup.config.ConfigManager;

import org.black_ixx.playerpoints.PlayerPoints;

public final class CustomEnchantUp extends JavaPlugin {
    public static CustomEnchantUp pluginInstance;
    public static CustomEnchantUp instance;
    public static XConomyAPI xConomyAPI;
    public static PlayerPoints playerPointsAPI;
    public static Configuration pluginConfig;
    public static String activeEconomyType;
    
    private static ConfigManager configManager;

    public void onEnable() {
        pluginInstance = this;
        instance = this;
        
        configManager = ConfigManager.getInstance(this);
        
        if (!(new File(getDataFolder(), "config.yml")).exists()) {
            saveDefaultConfig();
        }
        saveDefaultConfig();
        
        reloadConfig();
        pluginConfig = getConfig();
        configManager.loadConfigs();
        
        if (Bukkit.getPluginManager().getPlugin("XConomy") != null) {
            xConomyAPI = new XConomyAPI();
            activeEconomyType = "xconomy";
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPointsAPI = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (activeEconomyType == null) {
                activeEconomyType = "playerpoints";
            }
        }

        if (configManager.getBoolean("debug", false)) {
            getLogger().info("Debug mode enabled.");
            getLogger().info("Upgrade cost: " + configManager.getUpgradeCost());
            getLogger().info("Fix cost: " + configManager.getFixCost());
            getLogger().info("Max enchantment level: " + configManager.getMaxLevel());
        }
        
        CommandManager ceuCommand = new CommandManager(configManager);
        PluginCommand cmd = getCommand("ceu");
        if (cmd != null) {
            cmd.setExecutor(ceuCommand);
            cmd.setTabCompleter(new CommandTabCompleter(ceuCommand.getSubCommands()));
        }
        
        getLogger().info("========================================");
        getLogger().info("");
        getLogger().info("CustomEnchantUp - " + getDescription().getVersion());

        boolean xConomyEnabled = xConomyAPI != null;
        boolean playerPointsEnabled = playerPointsAPI != null;

        if (xConomyEnabled) {
            getLogger().info("Xconomy: \u001B[32m已启用\u001B[0m");
        }
        if (playerPointsEnabled) {
            getLogger().info("PlayerPoints: \u001B[32m已启用\u001B[0m");
        }
        if (!xConomyEnabled && !playerPointsEnabled) {
            getLogger().info("\u001B[31m未发现受支持的经济系统\u001B[0m");
        }

        getLogger().info("");
        getLogger().info("========================================");

    }

    public void onDisable() {
        getLogger().info("Unloaded CustomEnchantUp plugin.");
    }
    
    public static CustomEnchantUp getInstance() {
        return instance;
    }
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
