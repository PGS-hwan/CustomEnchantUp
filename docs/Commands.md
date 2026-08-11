# 命令列表

## 用户命令

| 命令 | 说明 | 所需权限 |
|------|------|----------|
| `/ceu help` | 显示帮助信息 | `ceu.user` |
| `/ceu fix` | 修复当前手持物品耐久 | `ceu.user` |
| `/ceu repair` | 修复当前手持物品耐久（同 /ceu fix） | `ceu.user` |
| `/ceu upgrade` | 尝试升级当前手持物品附魔等级 | `ceu.user` |

## 管理员命令

| 命令 | 说明 | 所需权限 |
|------|------|----------|
| `/ceu admin fix <玩家名>` | 为指定玩家修复手持物品 | `ceu.admin` |
| `/ceu admin repair <玩家名>` | 为指定玩家修复手持物品 | `ceu.admin` |
| `/ceu admin upgrade <玩家名>` | 为指定玩家升级手持物品附魔 | `ceu.admin` |
| `/ceu reload` | 重新加载配置文件 | `ceu.admin` |

## 权限节点

- `ceu.user`：基础用户命令权限
- `ceu.admin`：管理员命令权限

## 命令说明

- `fix` / `repair`：检查玩家当前手持物品，如果物品可修复且余额足够，则执行修复操作。
- `upgrade`：对当前手持的附魔物品进行升级，若没有可升级的附魔或余额不足，则返回相应提示。
- `admin`：管理员可指定目标玩家执行修复或升级操作，适用于远程管理。
- `reload`：重新加载 `config.yml` 和 `lang.yml`。
