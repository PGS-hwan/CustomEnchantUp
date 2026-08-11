package com.github.hwan.customenchantup.commands.subcommands;

import me.yic.xconomy.api.XConomyAPI;
import me.yic.xconomy.data.syncdata.PlayerData;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.hwan.customenchantup.CustomEnchantUp;
import com.github.hwan.customenchantup.config.ConfigManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class FixCommand extends AbstractSubCommand {
    private ConfigManager configManager;

    public FixCommand(ConfigManager configManager) {
        super("fix", null, true);
        this.configManager = configManager;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(CommandSender sender, String[] args) {
        if (!checkPlayer(sender, configManager)) return;

        Player player = (Player) sender;
        ItemStack itemInHand = getItemInMainHand(player);

        if (itemInHand == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.no_item"));
            return;
        }

        if (itemInHand.getType().getMaxDurability() <= 0 || itemInHand.getDurability() <= 0) {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.no_durability"));
            return;
        }

        String economyType = configManager.getEconomyType();
        if (economyType == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.no_system"));
            return;
        }

        int fixCost = "classic".equals(economyType) ? configManager.getClassicCost("repair", itemInHand) : configManager.getFixCost();

        if ("xconomy".equals(economyType)) {
            handleXConomy(player, itemInHand, fixCost);
        } else if ("playerpoints".equals(economyType)) {
            handlePlayerPoints(player, itemInHand, fixCost);
        } else if ("classic".equals(economyType)) {
            handleClassic(player, itemInHand, fixCost);
        }
    }

    private void handleXConomy(Player player, ItemStack itemInHand, int fixCost) {
        XConomyAPI api = CustomEnchantUp.xConomyAPI;
        if (api == null) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }

        PlayerData playerData = api.getPlayerData(player.getName());
        if (playerData.getBalance().longValue() >= fixCost) {
            api.changePlayerBalance(playerData.getUniqueId(), playerData.getName(),
                new BigDecimal(fixCost), false);
            itemInHand.setDurability((short) 0);
            player.sendMessage(getPrefix() + configManager.getMessage("fix.success"));
        } else {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.insufficient_balance"));
        }
    }

    private void handlePlayerPoints(Player player, ItemStack itemInHand, int fixCost) {
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

        int currentPoints = api.look(player.getUniqueId());
        if (currentPoints < fixCost) {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.insufficient_balance"));
            return;
        }

        boolean success = api.take(player.getUniqueId(), fixCost);
        if (!success) {
            player.sendMessage(getPrefix() + configManager.getMessage("economy.system_error"));
            return;
        }

        itemInHand.setDurability((short) 0);
        player.sendMessage(getPrefix() + configManager.getMessage("fix.success"));
    }

    private void handleClassic(Player player, ItemStack itemInHand, int fixCost) {
        if (fixCost < 0) {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.classic_not_configured"));
            return;
        }

        if (!configManager.consumeClassicItems(player, "repair", itemInHand, fixCost)) {
            player.sendMessage(getPrefix() + configManager.getMessage("fix.insufficient_material"));
            return;
        }

        itemInHand.setDurability((short) 0);
        player.sendMessage(getPrefix() + configManager.getMessage("fix.success"));
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