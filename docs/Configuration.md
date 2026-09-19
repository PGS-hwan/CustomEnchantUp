# 配置说明

本插件采用两个主要配置文件：`config.yml` 和 `lang.yml`。启用保底后，插件还会自动生成 `pity-data.yml` 保存玩家进度。

## config.yml

### 经济设置

- `economy.type`：选择使用的经济系统，支持 `xconomy`、`playerpoints` 和 `classic`。
- `economy.xconomy.upgrade-cost`：使用 XConomy 时升级消耗金额。
- `economy.xconomy.fix-cost`：使用 XConomy 时修复消耗金额。
- `economy.playerpoints.upgrade-cost`：使用 PlayerPoints 时升级消耗点数。
- `economy.playerpoints.fix-cost`：使用 PlayerPoints 时修复消耗点数。
- `economy.classic.repair.<槽位>.<材料>`：使用 Classic 模式修复时，按装备槽位配置背包材料与数量。
- `economy.classic.upgrade.<槽位>.<材料>`：使用 Classic 模式升级时，按装备槽位配置背包材料与数量。

Classic 模式支持 `HELMET`、`CHESTPLATE`、`LEGGINGS`、`BOOTS` 和 `HAND` 槽位。材料名称使用 Bukkit `Material` 枚举名称；未配置当前物品对应规则时，操作不会执行。

### 升级设置

- `upgrade.success-chance`：升级成功概率，范围为 `0.0` 到 `1.0`，例如 `0.5` 为 50%。
- `upgrade.pity.enabled`：是否启用玩家升级保底。
- `upgrade.pity.attempts`：一个保底周期包含的付费升级尝试次数。
- `upgrade.pity.guaranteed-successes`：每个周期内至少成功的次数，范围为 `1` 到 `upgrade.pity.attempts`。
- `upgrade.actions.success.sound`：随机升级成功时播放的 Bukkit 声音，默认 `BLOCK_ANVIL_USE`。
- `upgrade.actions.random-failure.sound`：随机升级失败时播放的 Bukkit 声音，默认 `BLOCK_ANVIL_USE`。
- `upgrade.actions.operation-failure.sound`：无法执行升级时播放的 Bukkit 声音，默认 `ENTITY_ENDERMAN_TELEPORT`。
- `upgrade.max-level`：允许升级的最大附魔等级。
- `upgrade.single-max-level`：按附魔名称单独配置最大等级。
- `upgrade.blocked-enchantments`：禁止升级的附魔名称列表。

### 示例配置

```yaml
upgrade:
  success-chance: 0.5
  pity:
    enabled: true
    attempts: 10
    guaranteed-successes: 5
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

插件会在服务器启动和执行 `/ceu reload` 时检查声音名称，并通过 XSound 维护的 Bukkit 声音注册表与历史别名，将配置转换为当前服务端实际支持的 `Sound` 枚举名称后写回 `config.yml`。这覆盖 XSound 已收录的新旧 Bukkit 声音名称，不限于默认配置中的铁砧和末影人传送声音。名称无法识别，或该声音在当前服务端没有可用实现时，原配置会保留并在控制台输出警告。

保底进度按玩家 UUID 独立记录在 `pity-data.yml`，服务器重启后不会丢失。自然概率产生的成功会计入当前周期；当剩余尝试次数等于尚欠的成功次数时，后续尝试会被强制成功。余额不足、材料不足、无可升级附魔等未实际扣费的操作不会累计次数。管理员升级不参与保底统计。

### pity-data.yml

该文件由插件自动维护，不需要手动创建。每个未完成周期的记录包含玩家 UUID、最近一次升级时的玩家名、当前尝试次数和成功次数。周期完成后，对应记录会被清除并从下一次付费升级重新统计。

```yaml
550e8400-e29b-41d4-a716-446655440000:
  player-name: ExamplePlayer
  attempts: 4
  successes: 1
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