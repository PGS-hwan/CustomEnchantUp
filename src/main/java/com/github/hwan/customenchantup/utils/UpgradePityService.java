package com.github.hwan.customenchantup.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class UpgradePityService {
    private final JavaPlugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    public UpgradePityService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "pity-data.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean shouldGuarantee(UUID playerId, int cycleAttempts, int guaranteedSuccesses) {
        String path = playerId.toString();
        int attempts = data.getInt(path + ".attempts", 0);
        int successes = data.getInt(path + ".successes", 0);
        if (attempts >= cycleAttempts) {
            attempts = 0;
            successes = 0;
            setProgress(path, attempts, successes);
        }

        int remainingAttempts = cycleAttempts - attempts;
        int remainingSuccesses = Math.max(0, guaranteedSuccesses - successes);
        return remainingSuccesses >= remainingAttempts;
    }

    public void recordAttempt(UUID playerId, String playerName, boolean success, int cycleAttempts) {
        String path = playerId.toString();
        int attempts = data.getInt(path + ".attempts", 0) + 1;
        int successes = data.getInt(path + ".successes", 0) + (success ? 1 : 0);
        if (attempts >= cycleAttempts) {
            data.set(path, null);
        } else {
            data.set(path + ".player-name", playerName);
            setProgress(path, attempts, successes);
        }
        save();
    }

    private void setProgress(String path, int attempts, int successes) {
        data.set(path + ".attempts", attempts);
        data.set(path + ".successes", successes);
    }

    private void save() {
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("无法保存升级保底数据: " + dataFile.getName());
            exception.printStackTrace();
        }
    }
}