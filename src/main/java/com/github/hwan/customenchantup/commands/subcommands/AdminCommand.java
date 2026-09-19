package com.github.hwan.customenchantup.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.hwan.customenchantup.config.ConfigManager;
import com.github.hwan.customenchantup.utils.EnchantmentUpgradeService;

import java.util.ArrayList;
import java.util.List;

public class AdminCommand extends AbstractSubCommand {
    private ConfigManager configManager;
    private EnchantmentUpgradeService upgradeService;

    public AdminCommand(ConfigManager configManager) {
        super("admin", "ceu.admin", false);
        this.configManager = configManager;
        this.upgradeService = new EnchantmentUpgradeService(configManager);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender, configManager)) return;

        if (args.length < 2 || "help".equalsIgnoreCase(args[1])) {
            showAdminHelp(sender);
            return;
        }

        String adminAction = args[1].toLowerCase();
        String playerName = args.length > 2 ? args[2] : (sender instanceof Player ? sender.getName() : null);

        if (playerName == null) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.no_player_name"));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(configManager.getPrefix() + " " + 
                configManager.getMessage("admin.player_not_found").replace("%player%", playerName));
            return;
        }

        ItemStack targetItem = getItemInMainHand(targetPlayer);
        if (targetItem == null) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.no_item"));
            return;
        }

        switch (adminAction) {
            case "fix":
            case "repair":
                handleAdminFix(sender, targetPlayer, targetItem);
                break;
            case "upgrade":
                handleAdminUpgrade(sender, targetPlayer, targetItem);
                break;
            default:
                sender.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.unknown_subcommand"));
        }
    }

    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage(configManager.getMessage("admin.help.title"));
        sender.sendMessage(configManager.getMessage("admin.help.fix"));
        sender.sendMessage(configManager.getMessage("admin.help.repair"));
        sender.sendMessage(configManager.getMessage("admin.help.upgrade"));
        sender.sendMessage(configManager.getMessage("admin.help.footer"));
    }

    @SuppressWarnings("deprecation")
    private void handleAdminFix(CommandSender sender, Player player, ItemStack targetItem) {
        if (targetItem.getType().getMaxDurability() <= 0 || targetItem.getDurability() <= 0) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.fix.no_durability"));
            return;
        }

        targetItem.setDurability((short) 0);
        player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.fix.target_success"));
        sender.sendMessage(configManager.getPrefix() + 
            configManager.getMessage("admin.fix.success").replace("%player%", player.getName()));
    }

    private void handleAdminUpgrade(CommandSender sender, Player player, ItemStack targetItem) {
        if (targetItem.getEnchantments().isEmpty()) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.no_item"));
            return;
        }

        if (upgradeService.upgradeAll(targetItem)) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.target_success"));
            sender.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("admin.upgrade.success").replace("%player%", player.getName()));
        } else {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.no_upgradeable"));
            sender.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("admin.upgrade.failed").replace("%player%", player.getName()));
        }
    }

    @Override
    public List<String> getTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            completions.add("help");
            completions.add("fix");
            completions.add("repair");
            completions.add("upgrade");
        } else if (args.length == 3) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }

    @SuppressWarnings("deprecation")
    private ItemStack getItemInMainHand(Player player) {
        try {
            return player.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            return player.getInventory().getItemInHand();
        }
    }
}