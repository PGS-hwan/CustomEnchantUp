package com.github.hwan.customenchantup.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigMigrationService {
    private final JavaPlugin plugin;

    public ConfigMigrationService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void migrateConfigs() {
        migrate("config.yml");
        migrate("lang.yml");
    }

    private void migrate(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }

        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaultConfig = loadDefaultConfig(fileName);
        if (defaultConfig == null) {
            plugin.getLogger().warning("无法读取默认配置文件: " + fileName);
            return;
        }

        String currentVersion = currentConfig.getString("version", "");
        String defaultVersion = defaultConfig.getString("version", "");
        if (defaultVersion.equals(currentVersion)) {
            return;
        }

        String backupVersion = currentVersion.isEmpty() ? "unknown" : currentVersion;
        File backupFile = new File(plugin.getDataFolder(), fileName + "." + backupVersion + ".bak");
        if (!backupFile.exists() && !file.renameTo(backupFile)) {
            plugin.getLogger().warning("无法备份旧配置文件: " + fileName);
            return;
        }

        for (String path : defaultConfig.getKeys(true)) {
            if (!defaultConfig.isConfigurationSection(path) && currentConfig.contains(path)) {
                defaultConfig.set(path, currentConfig.get(path));
            }
        }
        defaultConfig.set("version", defaultVersion);

        try {
            defaultConfig.save(file);
            plugin.getLogger().info(fileName + " 已从版本 " + currentVersion + " 迁移至 " + defaultVersion);
        } catch (IOException exception) {
            plugin.getLogger().severe("无法写入迁移后的配置文件: " + fileName);
            exception.printStackTrace();
        }
    }

    private YamlConfiguration loadDefaultConfig(String fileName) {
        if (plugin.getResource(fileName) == null) {
            return null;
        }
        try (Reader reader = new InputStreamReader(plugin.getResource(fileName), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException exception) {
            plugin.getLogger().severe("无法读取默认配置文件: " + fileName);
            exception.printStackTrace();
            return null;
        }
    }
}