package com.github.hwan.customenchantup.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.hwan.customenchantup.config.ConfigManager;
import com.github.hwan.customenchantup.commands.subcommands.AbstractSubCommand;
import com.github.hwan.customenchantup.commands.subcommands.AdminCommand;
import com.github.hwan.customenchantup.commands.subcommands.FixCommand;
import com.github.hwan.customenchantup.commands.subcommands.HelpCommand;
import com.github.hwan.customenchantup.commands.subcommands.ReloadCommand;
import com.github.hwan.customenchantup.commands.subcommands.UpgradeCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private Map<String, AbstractSubCommand> subCommands = new HashMap<>();
    private ConfigManager configManager;

    public CommandManager(ConfigManager configManager) {
        this.configManager = configManager;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("fix", new FixCommand(configManager));
        subCommands.put("repair", new FixCommand(configManager));
        subCommands.put("upgrade", new UpgradeCommand(configManager));
        subCommands.put("admin", new AdminCommand(configManager));
        subCommands.put("help", new HelpCommand(configManager));
        subCommands.put("reload", new ReloadCommand(configManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();

        AbstractSubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("error.unknown_command"));
            return true;
        }

        if (!subCommand.hasPermission(sender)) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("permission.denied"));
            return true;
        }

        if (subCommand.isPlayerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(configManager.getMessage("error.console_only_error"));
            return true;
        }

        try {
            subCommand.execute(sender, args);
        } catch (Exception e) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("error.execute_failed"));
            e.printStackTrace();
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
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

    public Map<String, AbstractSubCommand> getSubCommands() {
        return subCommands;
    }
}
