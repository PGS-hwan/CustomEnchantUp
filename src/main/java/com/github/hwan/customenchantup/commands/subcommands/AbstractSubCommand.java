package com.github.hwan.customenchantup.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.hwan.customenchantup.config.ConfigManager;

import java.util.List;

public abstract class AbstractSubCommand {
    protected String name;
    protected String permission;
    protected boolean playerOnly;

    public AbstractSubCommand(String name, String permission, boolean playerOnly) {
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public abstract List<String> getTabComplete(CommandSender sender, String[] args);

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(CommandSender sender) {
        if (permission == null) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    protected boolean checkPlayer(CommandSender sender, ConfigManager configManager) {
        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("error.console_only_error"));
            return false;
        }
        return true;
    }

    protected boolean checkPermission(CommandSender sender, ConfigManager configManager) {
        if (!hasPermission(sender)) {
            sender.sendMessage(configManager.getMessage("permission.denied"));
            return false;
        }
        return true;
    }
}
