package com.github.hwan.customenchantup.commands.subcommands;

import org.bukkit.command.CommandSender;

import com.github.hwan.customenchantup.CustomEnchantUp;
import com.github.hwan.customenchantup.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends AbstractSubCommand {
    private ConfigManager configManager;
    private CustomEnchantUp pluginInstance;

    public ReloadCommand(ConfigManager configManager) {
        super("reload", "ceu.admin", false);
        this.configManager = configManager;
        this.pluginInstance = CustomEnchantUp.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender, configManager)) return;

        try {
            pluginInstance.migrateConfigs();
            pluginInstance.reloadConfig();
            configManager.loadConfigs();
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("reload.success"));
        } catch (Exception e) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("reload.failed"));
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
