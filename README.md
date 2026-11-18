# Better Fishing · 更好的钓鱼体验

给原版钓鱼加 9 条新鱼。每条鱼分**生 / 熟**两版，共 18 个物品；按稀有度分为常见、稀有、传说三档，越稀有营养越高、食用效果越强。稀有鱼不是随便就能钓到——有水域、时间、天气条件。

---

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 26.1 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.145.1+26.1 |
| JDK | **25**（必须，低版本编译会报 "invalid target release"） |
| Gradle | 用仓库自带的 wrapper 即可 |

## 编译与运行

```bash
# 启动客户端（开发环境）
./gradlew runClient

# 打包成 jar：产物在 build/libs/
./gradlew build
```

> **命令行编译必须指定 JDK 25**，否则用的是系统默认 java（可能是 17）：
> ```bash
> JAVA_HOME="C:/Users/lenovo/.jdks/openjdk-25.0.2" ./gradlew build
> ```
> 在 IDEA 里点运行不受影响，因为 Gradle JVM 已经配好了。

## 钓鱼条件

原版鱼池权重合计 100（鳕鱼 60 / 鲑鱼 25 / 河豚 13 / 热带鱼 2），本 Mod 新增权重合计 102，两者大约各占一半。

| 鱼 | 稀有度 | 权重 | 条件 | 实际概率 |
|---|---|---|---|---|
| 银鲫 | 常见 | 30 | 无 | ≈18% |
| 溪鳟 | 常见 | 25 | 无 | ≈15% |
| 月水母 | 常见 | 20 | 仅夜晚 | 夜里 ≈11% |
| 太阳鱼 | 稀有 | 10 | 仅白天 | 白天 ≈6% |
| 霜米诺 | 稀有 | 8 | 雪地群系 或 下雨 | 视地点 |
| 水晶鳗 | 稀有 | 5 | 仅雷雨天 | 仅雷雨 |
| 深渊鲸 | 传说 | 2 | 无 | ≈1.2% |
| 金龙鱼 | 传说 | 1 | 仅雷雨天 | 仅雷雨 |
| 星辉鱼 | 传说 | 1 | 仅夜晚 | 夜里 ≈0.6% |

时间判定用世界时钟（一天 24000 tick，0 = 日出、13000 = 入夜）；群系判定按**鱼钩所在坐标**，站在雪地边缘抛竿也算雪地。

## 食用效果

生吃是「应急」，烤熟才是「正餐」：熟鱼营养更高、效果更强，生鱼的部分负面效果在烤熟后消失。

| 鱼 | 生吃（营养 / 效果） | 烤熟（营养 / 效果） |
|---|---|---|
| 银鲫 | 2 / 无 | 5 / 生命恢复 I 5s |
| 溪鳟 | 2 / 速度 I 8s | 5 / 速度 I 30s |
| 月水母 | 1 / 夜视 20s，15% 中毒 4s | 4 / 夜视 60s + 发光 30s |
| 太阳鱼 | 3 / 生命恢复 I 4s | 6 / 生命恢复 II 8s + 抗火 20s |
| 霜米诺 | 2 / 缓降 30s，30% 缓慢 5s | 5 / 缓降 90s + 水下呼吸 30s |
| 水晶鳗 | 4 / 抗性 I 10s + 发光 10s | 7 / 抗性 II 20s + 吸收 4♥ 30s |
| 深渊鲸 | 6 / 水下呼吸 60s + 海豚之恩 30s + 夜视 30s，**缓慢 10s** | 9 / 水下呼吸 180s + 海豚之恩 90s + 夜视 90s + 抗性 I 30s |
| 金龙鱼 | 6 / 力量 I 15s + 生命上限 2♥ 60s，**饥饿 10s** | 10 / 力量 II 30s + 生命上限 4♥ 120s + 抗性 I 30s + 抗火 60s |
| 星辉鱼 | 5 / 跳跃提升 II 30s + 缓降 60s + 发光 30s | 9 / 跳跃提升 II 60s + 缓降 120s + 生命恢复 II 5s + 吸收 4♥ 60s |

熟鱼有三种烹饪方式：熔炉 200 tick、烟熏 100 tick（最快）、营火 600 tick。

## 目录结构

```
src/main/java/com/mod/fish/
├── ABetterFishingExperience.java   主入口，挂两个 initialize()
├── ModItems.java                   物品注册 + 食用效果（改数值看这里）
└── ModLootTables.java              钓鱼战利品注入 + 稀有度条件（改概率看这里）

src/main/resources/
├── assets/better_fishing/
│   ├── items/*.json                物品定义（26.1 新增的 items/ 层，18 个）
│   ├── models/item/*.json          item/generated 模型（18 个）
│   ├── textures/item/*.png         贴图，32×32 RGBA（18 张）
│   └── lang/{zh_cn,en_us}.json     名称
├── data/better_fishing/recipe/     烹饪配方（27 个 = 9 鱼 × 3 种炉具）
└── data/minecraft/tags/item/fishes.json   把 18 个物品登记为「鱼类」
```

## 常见改动

### 调钓鱼概率
打开 `ModLootTables.java`，改对应条目的 `setWeight(n)`。权重是相对值，越大越常见。

```java
.add(LootItem.lootTableItem(ModItems.SILVER_CARP).setWeight(30))
```

### 调食用效果
打开 `ModItems.java`，改 `buff(效果, 秒数, 等级-1)` 或 `chance(效果, 秒数, 等级-1, 概率)`：

```java
buff(MobEffects.STRENGTH, 30, 1)   // 力量 II，30 秒
chance(MobEffects.POISON, 4, 0, 0.15F)  // 15% 概率中毒 I，4 秒
```

> 26.1 里食用效果不在 `FoodProperties`，而在 `Consumable` 组件上，写法参考原版河豚：
> `item.Properties.food(FoodProperties, Consumable)`。

### 换贴图
把 32×32 的透明背景 PNG 同名覆盖到 `textures/item/` 即可，模型和代码都不用动。
熟鱼是 `cooked_<名字>.png`，想手绘直接覆盖。

### 加第 10 条鱼
按这个顺序改 6 处，缺一处就会显示紫黑格或钓不到：

1. **`ModItems.java`**：加两个 `register(...)`，生鱼 + 熟鱼，各自配好营养和 `consume(...)` 效果
2. **`ModLootTables.java`**：在 `modifyPools` 里加一条 `.add(...).setWeight(n)`，需要时加 `.when(条件)`
3. **`assets/better_fishing/items/<名字>.json`** + **`models/item/<名字>.json`**：各一份（熟鱼同样要一份）
4. **`textures/item/<名字>.png`**：32×32 RGBA
5. **`lang/zh_cn.json` 与 `en_us.json`**：加 `item.better_fishing.<名字>`
6. **`data/better_fishing/recipe/cooked_<名字>.json`** 等 3 个配方 + **`fishes.json`** 里登记

## 已知限制

- 熟鱼贴图是程序化着色（暖调 + 焦边）生成的，不是手绘，观感统一但不够精致
- 「雪天」判定用 `LocationCheck(雪地群系) 或 WeatherCheck(下雨)`，温暖群系下雨也会满足；要更严格可改成「雪地 或 寒冷群系下雨」
- 未使用 Mixin，全部靠 Fabric API 事件实现，升级版本时迁移成本低
- 26.1 没有 `is_snowy` / `is_cold` 生物群系标签，雪地群系只能按 `Biomes.*` 逐个列

## 许可

基于 Fabric 官方模板，模板部分为 CC0。
