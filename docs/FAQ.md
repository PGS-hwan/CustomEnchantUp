# 常见问题

## 插件支持哪些服务器版本？

CustomEnchantUp 支持 Java 8 及以上，以及 1.8.8 至 26.1 的 Spigot / Paper 服务器。

## 需要安装哪些前置插件？

使用 `xconomy` 经济模式时，请安装 XConomy；使用 `playerpoints` 模式时，请安装 PlayerPoints。这两个插件均为软依赖，但所选经济模式对应的插件必须已启用。

`classic` 模式不依赖经济插件，会从玩家背包中扣除配置指定的物品。

## 为什么插件提示未发现受支持的经济系统？

请确认 XConomy 或 PlayerPoints 已正确安装并在 CustomEnchantUp 前成功启用。然后检查 `config.yml` 中的 `economy.type` 是否与已安装的经济插件匹配，修改后执行 `/ceu reload`。

## 为什么玩家无法使用命令？

普通玩家命令需要 `ceu.user` 权限，管理员命令和重载配置需要 `ceu.admin` 权限。默认情况下，`ceu.user` 对所有玩家开放，`ceu.admin` 仅对 OP 开放；使用权限管理插件时，请为玩家或权限组显式授予对应节点。

## 为什么修复或升级没有生效？

执行命令前请将目标物品拿在主手中。修复仅对可损耗且尚未满耐久的物品生效；升级则要求物品带有可升级、未被禁止且未达到最大等级的附魔。

管理员命令同样会操作目标玩家当前主手中的物品。

## 为什么附魔不能继续升级？

检查以下配置项：

- `upgrade.max-level`：所有附魔的默认最高等级。
- `upgrade.single-max-level`：指定附魔的单独最高等级，会覆盖默认最高等级。
- `upgrade.blocked-enchantments`：列表内的附魔不会参与升级。

附魔名称应使用 Bukkit 名称，例如 `DURABILITY`、`SILK_TOUCH`。修改配置后执行 `/ceu reload`。

## 如何设置升级成功率？

使用 `upgrade.success-chance` 设置，取值为 `0.0` 至 `1.0`：`0.5` 表示 50%，`1.0` 表示必定成功，`0.0` 表示必定失败。

## 如何配置 classic 模式的消耗物品？

将 `economy.type` 设为 `classic`，再按操作类型和装备槽位设置材料与数量。例如：

```yaml
economy:
  type: classic
  classic:
    repair:
      HELMET:
        DIAMOND: 3
    upgrade:
      HELMET:
        DIAMOND: 2
```

支持的槽位键为 `HELMET`、`CHESTPLATE`、`LEGGINGS`、`BOOTS` 和 `HAND`。插件会从玩家背包中扣除对应材料；未配置匹配规则时，操作不会执行。

## 修改配置后为什么没有变化？

确认 YAML 缩进和键名正确，然后执行 `/ceu reload` 重载 `config.yml` 与 `lang.yml`。如仍无法确认原因，可将 `debug` 设为 `true`，重载或重启服务器后查看控制台的调试输出。

## 如何修改插件提示文字？

编辑插件数据目录中的 `lang.yml`，其中包含命令、权限、修复、升级和经济系统的反馈文本。文本支持 Bukkit 颜色代码，例如 `&a`；修改后执行 `/ceu reload`。

## 在哪里反馈问题？

请在 [GitHub Issues](https://github.com/PGS-hwan/CustomEnchantUp/issues) 中提供服务器版本、插件版本、相关配置和完整报错日志，以便定位问题。