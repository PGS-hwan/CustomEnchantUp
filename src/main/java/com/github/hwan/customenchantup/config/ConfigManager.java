package com.github.hwan.customenchantup.config;

import com.cryptomorin.xseries.XSound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {
    private static final Pattern LEGACY_HEX_COLOR = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern LEGACY_COLOR = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION =
        LegacyComponentSerializer.legacySection();
    private static ConfigManager instance;
    private Configuration mainConfig;
    private Configuration languageConfig;
    private JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfig();
        loadLanguageConfig();
    }

    public static ConfigManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    private void loadLanguageConfig() {
        File langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        this.languageConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public void loadConfigs() {
        this.mainConfig = plugin.getConfig();
        updateSoundNamesForServerVersion();
        loadLanguageConfig();
    }

    private void updateSoundNamesForServerVersion() {
        ConfigurationSection actions = mainConfig.getConfigurationSection("upgrade.actions");
        if (actions == null) {
            return;
        }

        boolean changed = false;
        for (String action : actions.getKeys(false)) {
            String path = "upgrade.actions." + action + ".sound";
            String configuredName = mainConfig.getString(path, "");
            if (configuredName == null || configuredName.trim().isEmpty()) {
                continue;
            }

            String normalizedName = configuredName.trim().toUpperCase();
            if (isSoundAvailable(normalizedName)) {
                continue;
            }

            String compatibleName = findCompatibleSoundName(normalizedName);
            if (compatibleName == null) {
                plugin.getLogger().warning("当前服务端不支持声音 " + configuredName + "（配置路径: " + path + "）");
                continue;
            }

            mainConfig.set(path, compatibleName);
            plugin.getLogger().info("已将声音 " + configuredName + " 转换为当前服务端支持的 " + compatibleName);
            changed = true;
        }

        if (changed) {
            plugin.saveConfig();
        }
    }

    private String findCompatibleSoundName(String configuredName) {
        Optional<XSound> matchedSound = XSound.of(configuredName);
        if (!matchedSound.isPresent()) {
            return null;
        }
        Sound bukkitSound = matchedSound.get().get();
        return bukkitSound == null ? null : bukkitSound.name();
    }

    private boolean isSoundAvailable(String soundName) {
        try {
            Sound.valueOf(soundName);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String getEconomyType() {
        return mainConfig.getString("economy.type", "xconomy");
    }

    public int getUpgradeCost() {
        String economyType = getEconomyType();
        if ("xconomy".equals(economyType)) {
            return mainConfig.getInt("economy.xconomy.upgrade-cost", 20000);
        } else if ("playerpoints".equals(economyType)) {
            return mainConfig.getInt("economy.playerpoints.upgrade-cost", 200);
        }
        return 5000;
    }

    public int getFixCost() {
        String economyType = getEconomyType();
        if ("xconomy".equals(economyType)) {
            return mainConfig.getInt("economy.xconomy.fix-cost", 50000);
        } else if ("playerpoints".equals(economyType)) {
            return mainConfig.getInt("economy.playerpoints.fix-cost", 500);
        }
        return 50000;
    }

    public int getClassicCost(String action, ItemStack item) {
        if (item == null) {
            return -1;
        }
        String slotKey = getClassicSlotKey(item);
        String path = "economy.classic." + action + "." + slotKey;
        ConfigurationSection section = mainConfig.getConfigurationSection(path);
        if (section == null) {
            return mainConfig.getInt("economy.classic." + action + ".default", -1);
        }

        String materialName = item.getType().name();
        int cost = mainConfig.getInt(path + "." + materialName, -1);
        if (cost >= 0) {
            return cost;
        }

        for (String key : section.getKeys(false)) {
            if (key != null && !key.trim().isEmpty()) {
                int fallbackCost = mainConfig.getInt(path + "." + key, -1);
                if (fallbackCost >= 0) {
                    return fallbackCost;
                }
            }
        }
        return mainConfig.getInt("economy.classic." + action + ".default", -1);
    }

    public Material getClassicConsumeMaterial(String action, ItemStack item) {
        if (item == null) {
            return null;
        }
        String slotKey = getClassicSlotKey(item);
        ConfigurationSection section = mainConfig.getConfigurationSection("economy.classic." + action + "." + slotKey);
        if (section == null) {
            return null;
        }

        for (String key : section.getKeys(false)) {
            if (key != null && !key.trim().isEmpty()) {
                try {
                    return Material.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public boolean consumeClassicItems(Player player, String action, ItemStack item, int cost) {
        if (player == null || item == null || cost <= 0) {
            return true;
        }

        Material material = getClassicConsumeMaterial(action, item);
        if (material == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int remaining = cost;
        for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(material).entrySet()) {
            ItemStack stack = entry.getValue();
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }

            int amount = stack.getAmount();
            int take = Math.min(amount, remaining);
            if (take <= 0) {
                continue;
            }

            if (amount - take <= 0) {
                inventory.setItem(entry.getKey(), null);
            } else {
                stack.setAmount(amount - take);
                inventory.setItem(entry.getKey(), stack);
            }

            remaining -= take;
            if (remaining <= 0) {
                break;
            }
        }

        if (remaining > 0) {
            return false;
        }

        player.updateInventory();
        return true;
    }

    private String getClassicSlotKey(ItemStack item) {
        if (item == null) {
            return "HAND";
        }
        String name = item.getType().name();
        if (name.endsWith("_HELMET")) {
            return "HELMET";
        }
        if (name.endsWith("_CHESTPLATE")) {
            return "CHESTPLATE";
        }
        if (name.endsWith("_LEGGINGS")) {
            return "LEGGINGS";
        }
        if (name.endsWith("_BOOTS")) {
            return "BOOTS";
        }
        return "HAND";
    }

    public double getSuccessChance() {
        return mainConfig.getDouble("upgrade.success-chance", 50.0);
    }

    public boolean isPityEnabled() {
        return mainConfig.getBoolean("upgrade.pity.enabled", false);
    }

    public int getPityAttempts() {
        return Math.max(1, mainConfig.getInt("upgrade.pity.attempts", 10));
    }

    public int getPityGuaranteedSuccesses() {
        int guaranteedSuccesses = mainConfig.getInt("upgrade.pity.guaranteed-successes", 1);
        return Math.max(1, Math.min(guaranteedSuccesses, getPityAttempts()));
    }

    public String getUpgradeSound(String action) {
        return mainConfig.getString("upgrade.actions." + action + ".sound", "");
    }

    public int getMaxLevel() {
        return mainConfig.getInt("upgrade.max-level", 10);
    }

    @SuppressWarnings("deprecation")
    public int getMaxLevel(Enchantment enchantment) {
        if (enchantment == null) {
            return getMaxLevel();
        }
        String path = "upgrade.single-max-level." + enchantment.getName().toUpperCase();
        int overrideLevel = mainConfig.getInt(path, -1);
        if (overrideLevel > 0) {
            return overrideLevel;
        }
        return getMaxLevel();
    }

    @SuppressWarnings("deprecation")
    public boolean isEnchantmentBlocked(Enchantment enchantment) {
        List<String> blocked = mainConfig.getStringList("upgrade.blocked-enchantments");
        if (blocked == null || blocked.isEmpty()) {
            return false;
        }
        String name = enchantment.getName().toUpperCase();
        for (String blockedName : blocked) {
            if (blockedName != null && blockedName.toUpperCase().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(String text) {
        if (text == null) {
            return "";
        }
        if (!isMiniMessageEnabled()) {
            return ChatColor.translateAlternateColorCodes('&', text);
        }
        Component component = MINI_MESSAGE.deserialize(convertLegacyColors(text));
        return LEGACY_SECTION.serialize(component);
    }

    public boolean isMiniMessageEnabled() {
        return languageConfig.getBoolean("mini-message", false);
    }

    private String convertLegacyColors(String text) {
        Matcher hexMatcher = LEGACY_HEX_COLOR.matcher(text);
        StringBuffer converted = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(converted, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(converted);

        Matcher colorMatcher = LEGACY_COLOR.matcher(converted.toString());
        converted = new StringBuffer();
        while (colorMatcher.find()) {
            colorMatcher.appendReplacement(converted, legacyTag(colorMatcher.group(1).charAt(0)));
        }
        colorMatcher.appendTail(converted);
        return converted.toString();
    }

    private String legacyTag(char code) {
        switch (Character.toLowerCase(code)) {
            case '0': return "<black>";
            case '1': return "<dark_blue>";
            case '2': return "<dark_green>";
            case '3': return "<dark_aqua>";
            case '4': return "<dark_red>";
            case '5': return "<dark_purple>";
            case '6': return "<gold>";
            case '7': return "<gray>";
            case '8': return "<dark_gray>";
            case '9': return "<blue>";
            case 'a': return "<green>";
            case 'b': return "<aqua>";
            case 'c': return "<red>";
            case 'd': return "<light_purple>";
            case 'e': return "<yellow>";
            case 'f': return "<white>";
            case 'k': return "<obfuscated>";
            case 'l': return "<bold>";
            case 'm': return "<strikethrough>";
            case 'n': return "<underlined>";
            case 'o': return "<italic>";
            case 'r': return "<reset>";
            default: return "";
        }
    }

    public String getPrefix() {
        return formatMessage(languageConfig.getString("prefix", "<gold>CustomEnchantUp <dark_gray>» "));
    }

    public String getMessage(String path) {
        String message = languageConfig.getString(path, "");
        return formatMessage(message);
    }

    public String getMessage(String path, String... replacements) {
        String message = languageConfig.getString(path, "");
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return formatMessage(message);
    }

    public Object get(String path) {
        return mainConfig.get(path);
    }

    public int getInt(String path, int defaultValue) {
        return mainConfig.getInt(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        return mainConfig.getString(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return mainConfig.getBoolean(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return mainConfig.getDouble(path, defaultValue);
    }
}
