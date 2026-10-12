
<h1 style="text-align: center">Scinese And Magic / 科技与魔法</h1>

<p style="text-align: center"><b>欢迎来到科技与魔法融合的世界！</b></p>

---

## 前言

> “我们可以制作一个科技与魔法融合，加上各种机制，好像这个模组也很好玩耶！”

就是这句话，志同道合的朋友们一拍即合，一起诞生出了这个项目！

> **目前项目还处于早期开发阶段，实际的代码也只有一个人**_（策划一堆）_**，可以点个 Star 期待一下哦~**

## 目前可揭露的内容

- [x] 全新的伤害机制
- [x] 强大的玩家能力系统
- 较大的剧情与世界观（策划阶段）
- 更多的维度（策划阶段）
- 科技系统与魔法系统，还有将两者融合的玩法！（策划中）
- 多人联机要素（当前阶段不考虑）
- ~~屎山代码~~

## 参与贡献

### 初始化仓库

#### 克隆仓库

```shell
git clone https://github.com/suq-weer/ScienceAndMagic.git
```

#### Gradle 首次构建

```shell
./gradlew build
```

#### 启动测试实例

```shell
./gradlew runClient

./gradlew runServer

./gradlew runClientAfterData
```

> 模组采用了依赖 Datagen 的方式来生成部分核心内容，为了保证表现一致性，仓库把 `src/generated` 添加到了 `.gitgnore`，请在启动游戏实例前注意，或者改用 Gradle 运行配置 `runClientAfterData`。

## 开源许可证

本模组采用**分部分授权**，完整说明见 [`NOTICE.md`](./NOTICE.md)：

- **代码与文档**：`GPL-3.0-or-later`（GNU 通用公共许可证第 3 版或更高版本），许可证全文见 [`LICENSE.txt`](./LICENSE.txt)。
  你可以自由使用、研究、修改与再分发，但需遵守 GPLv3：分发时随附许可证全文、保留原有的版权与许可声明、修改版需标注「已修改」、派生作品整体继续以 GPLv3 授权；本软件不提供任何担保。
  所有 Java 源文件顶部都带有 `SPDX-License-Identifier: GPL-3.0-or-later` 声明。
- **美术素材与创作数据**（`src/main/resources/assets/**` 的贴图与界面、`src/main/resources/gen/**` 的树形预设、`src/main/resources/data/**` 的数据）：**保留所有权利（All Rights Reserved）**，仅允许随本模组原样分发，不得单独提取、修改或再分发。
- **模板与第三方**：NeoForged MDK 模板文件为 MIT（见 [`TEMPLATE_LICENSE.txt`](./TEMPLATE_LICENSE.txt)）；Gradle Wrapper 属 Gradle 项目；NeoForge、Curios、JEI 等依赖遵循各自许可证。本项目不分发任何 Mojang 的版权素材。
- **贡献**：提交 Pull Request 即表示你同意自己的代码贡献以 `GPL-3.0-or-later` 授权。
