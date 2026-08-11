package com.github.hwan.customenchantup.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.hwan.customenchantup.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminCommand extends AbstractSubCommand {
    private ConfigManager configManager;

    public AdminCommand(ConfigManager configManager) {
        super("admin", "ceu.admin", false);
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender, configManager)) return;

        if (args.length < 2) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.usage"));
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
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.no_item"));
            return;
        }

        if (tryUpgradeItem(targetItem)) {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.target_success"));
            sender.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("admin.upgrade.success").replace("%player%", player.getName()));
        } else {
            player.sendMessage(configManager.getPrefix() + configManager.getMessage("admin.upgrade.no_upgradeable"));
            sender.sendMessage(configManager.getPrefix() + 
                configManager.getMessage("admin.upgrade.failed").replace("%player%", player.getName()));
        }
    }

    private boolean tryUpgradeItem(ItemStack targetItem) {
        Map<Enchantment, Integer> enchantments = targetItem.getEnchantments();
        boolean wasUpgraded = false;

        for (Map.Entry<Enchantment, Integer> entry : new ArrayList<>(enchantments.entrySet())) {
            int maxLevel = configManager.getMaxLevel(entry.getKey());
            if (entry.getValue() < maxLevel && !isEnchantmentBlocked(entry.getKey())) {
                targetItem.removeEnchantment(entry.getKey());
                targetItem.addUnsafeEnchantment(entry.getKey(), entry.getValue() + 1);
                wasUpgraded = true;
            }
        }

        return wasUpgraded;
    }

    private boolean isEnchantmentBlocked(Enchantment enchantment) {
        return configManager.isEnchantmentBlocked(enchantment);
    }

    @Override
    public List<String> getTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            completions.add("fix");
            completions.add("repair");
            completions.add("upgrade");
        } else if (args.length == 3) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
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