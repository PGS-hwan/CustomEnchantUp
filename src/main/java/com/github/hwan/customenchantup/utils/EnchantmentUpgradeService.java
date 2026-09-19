package com.github.hwan.customenchantup.utils;

import com.github.hwan.customenchantup.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class EnchantmentUpgradeService {
    private final ConfigManager configManager;

    public EnchantmentUpgradeService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<Enchantment> getUpgradeableEnchantments(ItemStack item) {
        List<Enchantment> enchantments = new ArrayList<>();
        if (item == null) {
            return enchantments;
        }

        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            if (isUpgradeable(entry.getKey(), entry.getValue())) {
                enchantments.add(entry.getKey());
            }
        }
        return enchantments;
    }

    public boolean upgradeAll(ItemStack item) {
        boolean upgraded = false;
        for (Enchantment enchantment : getUpgradeableEnchantments(item)) {
            upgraded |= upgrade(item, enchantment);
        }
        return upgraded;
    }

    public boolean upgrade(ItemStack item, Enchantment enchantment) {
        if (item == null || enchantment == null) {
            return false;
        }

        Integer level = item.getEnchantments().get(enchantment);
        if (level == null || !isUpgradeable(enchantment, level)) {
            return false;
        }

        item.removeEnchantment(enchantment);
        item.addUnsafeEnchantment(enchantment, level + 1);
        return true;
    }

    private boolean isUpgradeable(Enchantment enchantment, int level) {
        return level < configManager.getMaxLevel(enchantment)
            && !configManager.isEnchantmentBlocked(enchantment);
    }
}