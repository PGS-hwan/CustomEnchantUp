# Custom Enchant Up

![CustomEnchantUp](./docs/assets/CustomEnchantUp.png)

[![GitHub](https://img.shields.io/badge/GitHub-CustomEnchantUp-blue?logo=github)](https://github.com/PGS-hwan/CustomEnchantUp)
![Java](https://img.shields.io/badge/Java-8%2B-red?logo=java)
![Gradle](https://img.shields.io/badge/Build-Gradle-green?logo=gradle)
![License](https://img.shields.io/badge/License-GPL3.0-orange?logo=gpl-3.0)

一款 Spigot/Bukkit 服务器插件，提供附魔升级和物品修复功能，支持经济插件或背包材料消费。

## 项目概述

本插件允许玩家消耗经济余额、点数或背包材料，修复手中物品耐久并对附魔物品执行升级操作。插件支持 XConomy、PlayerPoints 和 Classic 三种消费模式。

## 核心功能

- `fix` / `repair`：修复玩家手持物品的耐久
- `upgrade`：提升手持物品附魔等级，支持可配置成功率和按玩家独立统计的保底机制
- `reload`：重载配置文件
- `admin`：管理员命令，用于修复或升级指定玩家的物品
- 跨版本声音兼容：启动和重载时通过 XSound 映射自动转换已收录的新旧 Bukkit 声音名称
- 保底进度持久化：按玩家 UUID 保存，并记录最近一次升级使用的玩家名
- 语言文件支持：所有提示消息可通过 `lang.yml` 自定义

## 系统要求

- Java 8 或更高版本
- 1.8.8-26.1 Spigot / Paper 服务器
- 软依赖：XConomy、PlayerPoints

## 命令说明

### 普通玩家命令

- `/ceu fix` 或 `/ceu repair`
  - 作用：修复当前手持物品的耐久
- `/ceu upgrade`
  - 作用：尝试升级当前手持物品的附魔等级
- `/ceu help`
  - 作用：显示帮助信息

### 管理员命令

- `/ceu admin fix <玩家名>`
  - 作用：为指定玩家修复手持物品
- `/ceu admin repair <玩家名>`
  - 作用：为指定玩家修复手持物品
- `/ceu admin upgrade <玩家名>`
  - 作用：为指定玩家升级手持物品的附魔等级
- `/ceu reload`
  - 作用：重新加载配置文件

### 权限节点

- `ceu.user`：基础用户命令权限
- `ceu.admin`：管理员命令权限

## 构建说明

建议使用项目内置 Gradle Wrapper：

```bash
./gradlew clean build
```

构建生成的完整插件 JAR 位于 `build/libs/` 目录。

## 更多内容

### 统计数据

本项目使用 bStats 来收集匿名统计数据，用于改进插件。您可以在 [bStats 平台](https://bstats.org/) 上查看统计信息。

### 贡献和反馈

- **GitHub Issues**: [提交问题和建议](../../issues)


### 致谢

- 感谢所有为本项目做出贡献和反馈的人！

> ⭐ 如果您觉得本项目不错，欢迎给予 Star ⭐
