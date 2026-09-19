package com.github.hwan.customenchantup.commands.subcommands;

import me.yic.xconomy.api.XConomyAPI;
import me.yic.xconomy.data.syncdata.PlayerData;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;

import com.github.hwan.customenchantup.CustomEnchantUp;
import com.github.hwan.customenchantup.config.ConfigManager;
import com.github.hwan.customenchantup.utils.EnchantmentUpgradeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UpgradeCommand extends AbstractSubCommand {
    private ConfigManager configManager;
    private Random randomGenerator = new Random();
    private EnchantmentUpgradeService upgradeService;

    public UpgradeCommand(ConfigManager configManager) {
        super("upgrade", null, true);
        this.configManager = configManager;
        this.upgradeService = new EnchantmentUpgradeService(configManager);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender, configManager)) return;

        Player player = (Player) sender;
        ItemStack itemInHand = getItemInMainHand(player);

        if (itemInHand == null || itemInHand.getEnchantments().isEmpty()) {
            operationFailure(player, "upgrade.no_enchantment");
            return;
        }

        attemptUpgrade(player);
    }

    private void attemptUpgrade(Player player) {
        ItemStack itemInHand = getItemInMainHand(player);
        if (itemInHand == null || itemInHand.getEnchantments().isEmpty()) {
            operationFailure(player, "upgrade.no_enchantment");
            return;
        }
        if (!upgradeService.getUpgradeableEnchantments(itemInHand).isEmpty()) {
            String economyType = configManager.getEconomyType();
            if (economyType == null) {
                operationFailure(player, "economy.no_system");
                return;
            }
            int upgradeCost = "classic".equals(economyType)
                ? configManager.getClassicCost("upgrade", itemInHand) : configManager.getUpgradeCost();
            if ("xconomy".equals(economyType)) {
                handleXConomy(player, itemInHand, upgradeCost);
            } else if ("playerpoints".equals(economyType)) {
                handlePlayerPoints(player, itemInHand, upgradeCost);
            } else if ("classic".equals(economyType)) {
                handleClassic(player, itemInHand, upgradeCost);
            } else {
                operationFailure(player, "economy.no_system");
            }
        } else {
            operationFailure(player, "upgrade.no_upgradeable");
        }
    }

    private void handleXConomy(Player player, ItemStack itemInHand, int upgradeCost) {
        XConomyAPI api = CustomEnchantUp.xConomyAPI;
        if (api == null) {
            operationFailure(player, "economy.system_error");
            return;
        }

        PlayerData playerData = api.getPlayerData(player.getName());
        if (playerData.getBalance().longValue() < upgradeCost) {
            operationFailure(player, "upgrade.insufficient_balance");
            return;
        }

        api.changePlayerBalance(playerData.getUniqueId(), playerData.getName(),
            new BigDecimal(upgradeCost), false);

        finishUpgrade(player, itemInHand);
    }

    private void handlePlayerPoints(Player player, ItemStack itemInHand, int upgradeCost) {
        PlayerPoints plugin = CustomEnchantUp.playerPointsAPI;
        if (plugin == null) {
            operationFailure(player, "economy.system_error");
            return;
        }

        PlayerPointsAPI api = plugin.getAPI();
        if (api == null) {
            operationFailure(player, "economy.system_error");
            return;
        }

        int currentPoints = api.look(player.getUniqueId());
        if (currentPoints < upgradeCost) {
            operationFailure(player, "upgrade.insufficient_balance");
            return;
        }

        boolean success = api.take(player.getUniqueId(), upgradeCost);
        if (!success) {
            operationFailure(player, "economy.system_error");
            return;
        }

        finishUpgrade(player, itemInHand);
    }

    private void handleClassic(Player player, ItemStack itemInHand, int upgradeCost) {
        if (upgradeCost < 0) {
            operationFailure(player, "upgrade.classic_not_configured");
            return;
        }

        if (!configManager.consumeClassicItems(player, "upgrade", itemInHand, upgradeCost)) {
            operationFailure(player, "upgrade.insufficient_material");
            return;
        }

        finishUpgrade(player, itemInHand);
    }

    private void finishUpgrade(Player player, ItemStack itemInHand) {
        if (!isSuccessByChance(configManager.getSuccessChance())) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.failed"));
            playSound(player, "random-failure");
            return;
        }
        boolean upgraded = upgradeService.upgradeAll(itemInHand);
        if (upgraded) {
            player.sendMessage(getPrefix() + configManager.getMessage("upgrade.success"));
            playSound(player, "success");
        } else {
            operationFailure(player, "upgrade.no_upgradeable");
        }
    }

    private void operationFailure(Player player, String messagePath) {
        player.sendMessage(getPrefix() + configManager.getMessage(messagePath));
        playSound(player, "operation-failure");
    }

    private void playSound(Player player, String action) {
        String soundName = configManager.getUpgradeSound(action);
        if (soundName == null || soundName.isEmpty()) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0F, 1.0F);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private boolean isSuccessByChance(double chance) {
        if (chance >= 1.0D) return true;
        if (chance <= 0.0D) return false;
        return this.randomGenerator.nextDouble() < chance;
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