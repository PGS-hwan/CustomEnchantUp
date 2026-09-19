# 配置说明

本插件采用两个主要配置文件：`config.yml` 和 `lang.yml`。

## config.yml

### 经济设置

- `economy.type`：选择使用的经济系统，支持 `xconomy`、`playerpoints` 和 `classic`。
- `economy.xconomy.upgrade-cost`：使用 XConomy 时升级消耗金额。
- `economy.xconomy.fix-cost`：使用 XConomy 时修复消耗金额。
- `economy.playerpoints.upgrade-cost`：使用 PlayerPoints 时升级消耗点数。
- `economy.playerpoints.fix-cost`：使用 PlayerPoints 时修复消耗点数。

### 升级设置

- `upgrade.success-chance`：升级成功概率，范围为 `0.0` 到 `1.0`，例如 `0.5` 为 50%。
- `upgrade.actions.success.sound`：随机升级成功时播放的 Bukkit 声音，默认 `BLOCK_ANVIL_USE`。
- `upgrade.actions.random-failure.sound`：随机升级失败时播放的 Bukkit 声音，默认 `BLOCK_ANVIL_USE`。
- `upgrade.actions.operation-failure.sound`：无法执行升级时播放的 Bukkit 声音，默认 `ENTITY_ENDERMAN_TELEPORT`。
- `upgrade.max-level`：允许升级的最大附魔等级。
- `upgrade.single-max-level`：按附魔名称单独配置最大等级。
- `upgrade.blocked-enchantments`：禁止升级的附魔名称列表。

### 示例配置

```yaml
upgrade:
  actions:
    success:
      sound: BLOCK_ANVIL_USE
    random-failure:
      sound: BLOCK_ANVIL_USE
    operation-failure:
      sound: ENTITY_ENDERMAN_TELEPORT
  max-level: 10
  single-max-level:
    DURABILITY: 5
  blocked-enchantments:
    - "ARROW_INFINITE"
    - "KNOCKBACK"
    - "ARROW_KNOCKBACK"
    - "SILK_TOUCH"
    - "THORNS"
```

### 调试设置

- `debug`：启用后插件将在控制台输出额外调试信息，便于排查问题。

## lang.yml

该文件用于定义插件提示信息与命令反馈文本。主要包含以下字段：

- `prefix`：消息前缀。
- `help.*`：帮助界面文本。
- `fix.*`：修复命令反馈信息。
- `upgrade.*`：升级命令反馈信息。
- `economy.*`：经济系统相关提示。
- `permission.*`：权限错误提示。
- `admin.*`：管理员命令反馈信息。
- `reload.*`：重载命令反馈信息。
- `error.*`：通用错误提示。

### 自定义说明

`lang.yml` 中的文本支持 MiniMessage 格式，同时兼容 Bukkit `&` 颜色代码和 `&#RRGGBB` 十六进制颜色。两种格式可以在同一条消息中混用。

```yaml
prefix: "<gradient:#FFD166:#EF476F><bold>CustomEnchantUp</bold></gradient> <dark_gray>» "
upgrade:
  success: "<green>升级成功!"
  failed: "&c升级失败!"
```

支持颜色、渐变、粗体、斜体、下划线等文本样式。由于 Bukkit 传统字符串消息不支持交互组件，MiniMessage 的点击和悬停事件不会发送到玩家。配置中的 `%player%` 等插件占位符仍可正常使用。