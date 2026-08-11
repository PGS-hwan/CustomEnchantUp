package com.github.hwan.customenchantup.commands.subcommands;

import org.bukkit.command.CommandSender;

import com.github.hwan.customenchantup.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends AbstractSubCommand {
    private ConfigManager configManager;

    public HelpCommand(ConfigManager configManager) {
        super("help", null, false);
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(configManager.getMessage("help.title"));
        sender.sendMessage(configManager.getMessage("help.fix"));
        sender.sendMessage(configManager.getMessage("help.repair"));
        sender.sendMessage(configManager.getMessage("help.upgrade"));
        if (sender.hasPermission("ceu.admin")) {
            sender.sendMessage(configManager.getMessage("help.admin_fix"));
            sender.sendMessage(configManager.getMessage("help.admin_repair"));
            sender.sendMessage(configManager.getMessage("help.admin_upgrade"));
            sender.sendMessage(configManager.getMessage("help.reload"));
        }
        sender.sendMessage(configManager.getMessage("help.footer"));
    }

    @Override
    public List<String> getTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
