# BedWars1058-25.9 起床战争插件套件

基于 [BedWars1058](https://github.com/andrei1058/BedWars1058) 25.2（fork 版本 25.9）的起床战争插件源码，附带一个**经验模式附属插件** `bw1058xp`，并对 **Leaves/Paper 1.21.11** 做了版本支持适配。

## 功能总览

| 模块 | 说明 |
|---|---|
| **主插件** `BedWars1058` | 起床战争核心玩法：竞技场、商店、队伍、升级、计分板、数据统计 |
| **附属插件** `bw1058xp` | 经验模式：铁/金/绿宝石自动换算 XP，商店以 XP 结算，击杀转移 XP |
| **版本支持** | 原生支持 1.8.8 ~ 1.20.4；已适配 Leaves/Paper **1.21.11** |

---

## 目录结构

```
BedWars1058-25.9/
├── BedWars1058-25.9/          # 主插件源码（Maven 多模块）
│   ├── bedwars-api/           # 公共 API（供附属插件与第三方调用）
│   ├── bedwars-plugin/        # 主插件实现（shade 打包后为 BedWars1058.jar）
│   ├── versionsupport_*/      # 各版本 NMS 支持模块（1.8.8 ~ 1.20.4）
│   ├── versionsupport_v1_21/  # ★ 新增：1.21.11（mojang 映射）版本支持模块
│   ├── versionsupport_common/ # 版本通用监听器
│   ├── resetadapter_*/        # 地图重置适配器（ASWM / Slime）
│   └── pom.xml                # 父 POM（模块聚合）
├── untitled/                  # ★ 经验模式附属插件 bw1058xp 源码
│   └── target/bw1058xp-25.9.jar
├── 1058xp/                    # 空目录（预留）
└── bedwars-12111.jar          # ★ 适配 1.21.11 的成品主插件 jar
```

---

## 经验模式附属插件 `bw1058xp`

将起床战争的货币系统改为**经验值（XP）**结算：

### 资源换算（钻石不变）

| 资源 | XP |
|---|---|
| 1 个铁锭 | 1 XP |
| 1 个金锭 | 10 XP |
| 1 个绿宝石 | 100 XP |
| 钻石 | 不转换，仍为物品 |

- 拾取铁/金/绿宝石时自动转为 XP，屏幕上方 ActionBar 提示 `+N XP  |  当前经验: M XP`
- 兜底机制：每 20 tick 扫描竞技场玩家背包，把 `gen-split` 等直接塞入背包的资源也换算掉，防止刷资源

### 商店以 XP 结算

- 商店中以铁/金/绿宝石计价（`vault`/钻石计价除外）的商品，购买时消耗 XP
- 商品 lore 自动改写为 `花费: X XP`，并显示当前 `经验余额`，可买/不可买颜色区分
- 经验不足时提示并播放失败音效
- 永久物品档位、死亡恢复等机制与原生一致

### 击杀转移 XP

- 玩家 A 击杀玩家 B 时，将 B 的 XP 按百分比转移给 A（默认 100%）
- 支持所有击杀方式（PVP / 射箭 / 爆炸 / 虚空等）

### 配置文件 `config.yml`

```yaml
rates:
  iron: 1        # 1 铁 = 1 XP
  gold: 10       # 1 金 = 10 XP
  emerald: 100   # 1 绿宝石 = 100 XP

pickup-message: "&a+{amount} &bXP  &8|  &f当前经验: &b{balance} XP"
scan-interval-ticks: 20
reset-xp-on-death: false                 # 死亡是否清空 XP
kill-xp-transfer-rate: 1.0               # 击杀转移比例（1.0 = 100%）
kill-xp-message: "&b击杀奖励 &a+{amount} XP"
insufficient-message: "&c经验不足！还需要 &b{xp} XP"
cost-line: "&7花费: &b{xp} XP"
balance-lore-line: "&7经验余额: &b{balance} XP"
```

### 兼容性

- `bw1058xp-25.9.jar` 以 **Java 8** 编译（class 版本 52），**1.8.8 与 1.21.11 双版本通用**
- 通过反射访问 BedWars API 与商店内部类，BedWars 未安装时插件不报错、不生效
- 拾取事件按版本自动区分 `EntityPickupItemEvent` / `PlayerPickupItemEvent`
- 若 `versionsupport` 内部类结构与目标 BedWars 版本不一致，商店 XP 购买会降级为不生效，但资源转换仍可用

---

## Leaves/Paper 1.21.11 支持说明

### 背景

- 官方 BedWars1058 最新版（25.2）最高只支持到 **1.20.4**，1.21+ 无法加载
- 1.21.11 服务端 craftbukkit 类名变为 `org.bukkit.craftbukkit.CraftServer`（**无版本后缀**），而旧版是 `v1_20_R4` 等带版本后缀
- BedWars 通过 `Bukkit.getServer().getClass().getName().split("\\.")[3]` 解析版本并反射加载 `support.version.<版本>.<版本>` 类

### 适配内容（`versionsupport_v1_21`）

| 适配点 | 旧实现（spigot 映射） | 新实现（mojang 映射） |
|---|---|---|
| 类名/包名 | `support.version.v1_20_R4.v1_20_R4` | `support.version.CraftServer.CraftServer` |
| sidebar | `libs.sidebar.v1_20_R4.*` | `libs.sidebar.CraftServer.*` |
| 物品 NBT | `NBTTagCompound` / `CraftItemStack` | **PersistentDataContainer** |
| 物品类型判断 | `ItemSword` / `ItemArmor` 等类 | **Material 枚举判断** |
| 数据包 | `PacketPlayOutScoreboard*` 等 | `ClientboundSetScoreboard*` / `ClientboundSetPlayerTeamPacket` 等 |
| AI 目标 | `PathfinderGoalNearestAttackableTarget` | `NearestAttackableTargetGoal` |

### 已知取舍

- TNT 方块爆炸抗性修改（`registerTntWhitelist`）在 1.21 上以反射方式尽力实现，异常时静默跳过
- 1.21.11 无版本后缀的 craftbukkit 包名是 Mojang 映射与 Paper 分叉（Leaves/Paper/Purpur）的共同特征，本适配对同类服务端通用

---

## 编译方法

### 前置要求

- JDK 21（编译 1.21 支持模块与附属插件）
- Maven 3.8+
- 本地 Maven 仓库需包含：
  - 各版本 `spigot` NMS jar（`versionsupport_*` 编译依赖）
  - 1.21.11 服务端 jar（安装为本地依赖，供 `versionsupport_v1_21` 编译）
  - `com.andrei1058.spigot.sidebar` 系列库

> 提示：全局 Maven `settings.xml` 若配置了阿里云镜像（缺少 papermc 部分依赖），可使用项目自带的镜像配置：
> `untitled/.mvn/settings.xml`（mirror 指向 papermc 仓库）

### 编译主插件（含 1.21 支持）

```bash
cd BedWars1058-25.9
mvn clean package -DskipTests
```

> 1.21 支持模块 `versionsupport_v1_21` 需要将 1.21.11 服务端 jar 安装到本地仓库：
> ```bash
> mvn install:install-file -Dfile=leaves-1.21.11.jar \
>   -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.21.11-R0.1-SNAPSHOT \
>   -Dpackaging=jar
> ```

### 编译经验模式附属插件

```bash
cd untitled
mvn clean package -DskipTests
# 产物：untitled/target/bw1058xp-25.9.jar
```

---

## 部署方法

1. 将 `untitled/target/bw1058xp-25.9.jar` 放入服务端 `plugins/`
2. 将主插件 jar（1.21.11 用 `bedwars-12111.jar`）放入服务端 `plugins/`
3. 重启服务端，查看日志确认：

```
[BedWars1058] Loading support for paper/spigot: CraftServer
[BedWars1058] Enabling BedWars1058 v25.2
[BedWars1058] Initializing SidebarLib by andrei1058
[BedWars1058] Hooked into vault chat support!
Done (...s)!
```

---

## 常见问题

| 现象 | 原因/解决 |
|---|---|
| `I can't run on your version: v1_21_*` | 使用了未适配的 1.21 版本，使用本仓库 `bedwars-12111.jar` |
| `I can't run on your server software` | 缺少 `org.spigotmc.SpigotConfig`，需要在 Spigot/Paper/Leaves 系服务端运行 |
| 附属插件提示"未检测到 BedWars1058 API" | BedWars1058 未加载或版本过旧 |
| 计分板不显示 | sidebar 库版本与服务端不匹配（本仓库已含 1.21 sidebar 实现） |
| `UnsupportedClassVersionError`（其他插件） | 该插件用更高版本 JDK 编译，与服务端 Java 版本无关（如 Chunky） |

---

## 协议

主插件遵循 GPLv3（见各源码文件头部版权声明）。附属插件 `bw1058xp` 为定制实现，供学习与本地部署使用。
