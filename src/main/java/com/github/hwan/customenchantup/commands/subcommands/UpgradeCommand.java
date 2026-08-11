package com.github.hwan.customenchantup.commands.subcommands;

import me.yic.xconomy.api.XConomyAPI;
import me.yic.xconomy.data.syncdata.PlayerData;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.hwan.customenchantup.CustomEnchantUp;
import com.github.hwan.customenchantup.config.ConfigManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UpgradeCommand extends AbstractSubCommand {
    private ConfigManager configManager;
    private Random randomGenerator = new Random();

    public UpgradeCommand(ConfigManager configManager) {
        super("upgrade", null, true);
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender, configManager)) return;

        Player player = (Player) sender;
        ItemStack itemInHand = getItemInMainHand(player);

        if (itemInHand == null || itemInHand.getEnchantments().isEmpty()) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_enchantment"));
            return;
        }

        String economyType = configManager.getEconomyType();
        if (economyType == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.no_system"));
            return;
        }

        int upgradeCost = "classic".equals(economyType) ? configManager.getClassicCost("upgrade", itemInHand) : configManager.getUpgradeCost();
        double successChance = configManager.getSuccessChance();

        if ("xconomy".equals(economyType)) {
            handleXConomy(player, itemInHand, upgradeCost, successChance);
        } else if ("playerpoints".equals(economyType)) {
            handlePlayerPoints(player, itemInHand, upgradeCost, successChance);
        } else if ("classic".equals(economyType)) {
            handleClassic(player, itemInHand, upgradeCost, successChance);
        }
    }

    private void handleXConomy(Player player, ItemStack itemInHand, int upgradeCost, double successChance) {
        XConomyAPI api = CustomEnchantUp.xConomyAPI;
        if (api == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }
        if (!hasUpgradeableEnchantment(itemInHand)) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            return;
        }

        PlayerData playerData = api.getPlayerData(player.getName());
        if (playerData.getBalance().longValue() < upgradeCost) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.insufficient_balance"));
            return;
        }

        api.changePlayerBalance(playerData.getUniqueId(), playerData.getName(),
            new BigDecimal(upgradeCost), false);

        if (isSuccessByChance(successChance)) {
            if (tryUpgradeItem(itemInHand)) {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.success"));
            } else {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            }
        } else {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.failed"));
        }
    }

    private void handlePlayerPoints(Player player, ItemStack itemInHand, int upgradeCost, double successChance) {
        PlayerPoints plugin = CustomEnchantUp.playerPointsAPI;
        if (plugin == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }

        PlayerPointsAPI api = plugin.getAPI();
        if (api == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }

        if (!hasUpgradeableEnchantment(itemInHand)) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            return;
        }

        int currentPoints = api.look(player.getUniqueId());
        if (currentPoints < upgradeCost) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.insufficient_balance"));
            return;
        }

        boolean success = api.take(player.getUniqueId(), upgradeCost);
        if (!success) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }

        if (isSuccessByChance(successChance)) {
            if (tryUpgradeItem(itemInHand)) {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.success"));
            } else {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            }
        } else {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.failed"));
        }
    }

    private void handleClassic(Player player, ItemStack itemInHand, int upgradeCost, double successChance) {
        if (upgradeCost < 0) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.classic_not_configured"));
            return;
        }

        if (!hasUpgradeableEnchantment(itemInHand)) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            return;
        }

        if (!configManager.consumeClassicItems(player, "upgrade", itemInHand, upgradeCost)) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.insufficient_material"));
            return;
        }

        if (isSuccessByChance(successChance)) {
            if (tryUpgradeItem(itemInHand)) {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.success"));
            } else {
                player.sendMessage(getPrefix() + configManager.getMessage("upgrade.no_upgradeable"));
            }
        } else {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.failed"));
        }
    }

    private boolean tryUpgradeItem(ItemStack itemInHand) {
        Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
        boolean wasUpgraded = false;

        for (Map.Entry<Enchantment, Integer> entry : new ArrayList<>(enchantments.entrySet())) {
            int maxLevel = configManager.getMaxLevel(entry.getKey());
            if (entry.getValue() < maxLevel && !isEnchantmentBlocked(entry.getKey())) {
                itemInHand.removeEnchantment(entry.getKey());
                itemInHand.addUnsafeEnchantment(entry.getKey(), entry.getValue() + 1);
                wasUpgraded = true;
            }
        }

        return wasUpgraded;
    }

    private boolean hasUpgradeableEnchantment(ItemStack itemInHand) {
        Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            int maxLevel = configManager.getMaxLevel(entry.getKey());
            if (entry.getValue() < maxLevel && !isEnchantmentBlocked(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnchantmentBlocked(Enchantment enchantment) {
        return configManager.isEnchantmentBlocked(enchantment);
    }

    private boolean isSuccessByChance(double chance) {
        if (chance >= 100.0D) return true;
        if (chance <= 0.0D) return false;
        double roll = this.randomGenerator.nextDouble() * 100.0D;
        return roll < chance;
    }

    @Override
    public List<String> getTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    private String getPrefix() {
        return configManager.getPrefix();
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