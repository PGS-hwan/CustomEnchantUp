# 安装指南

## 运行环境

- Java 8 或更高版本
- Spigot / Paper 服务器
- XConomy 或 PlayerPoints 之一可用于经济扣费；Classic 模式无需经济插件

## 安装步骤

1. 使用 Gradle Wrapper 构建插件：
   ```bash
   ./gradlew clean shadowJar
   ```
2. 将 `build/libs/` 中生成的完整 JAR 文件复制到服务器 `plugins` 目录。
3. 启动或重启服务器。
4. 检查 `plugins/CustomEnchantUp` 目录中的配置文件是否生成。
5. 如需修改配置，编辑 `config.yml` 和 `lang.yml`，然后执行 `/ceu reload`。

## 验证安装

启动完成后，请检查控制台日志中是否出现插件加载信息。若未显示插件信息，请确认 `plugin.yml` 配置正确并且服务器已成功加载该插件。