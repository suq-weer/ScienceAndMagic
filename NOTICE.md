# 授权范围声明

本项目 **Science And Magic**（模组 ID：`science_and_magic`）采用**分部分授权**：不同组成部分适用不同许可证，复用前请确认具体范围。

## 1. 代码与文档 —— GNU GPL v3 或更高版本（GPL-3.0-or-later）

除第 2 节「美术素材与创作数据」与第 3 节「第三方与模板文件」中明确列出的内容外，本仓库中的内容均以 **GNU General Public License, version 3 or later** 授权，其中主要包括：

- `src/main/java/**` 下的全部 Java 源代码（共 85 个文件，每个文件顶部都带有 SPDX 标识）；
- `src/main/resources/science_and_magic.mixins.json` 等作为代码组成部分的配置文件；
- `README.md`、`docs/**` 等文档，以及 `build.gradle`、`settings.gradle`、`gradle.properties` 等构建配置。

许可证全文见仓库根目录 [`LICENSE.txt`](LICENSE.txt)（FSF 官方 GPLv3 原文，未作任何改写）。

你可以自由地使用、研究、修改与再分发这些内容，但必须遵守 GPLv3 的条款，主要包括：

- 分发时必须随附本许可证全文，并保留原有的版权与许可声明；
- 分发修改版本时，必须显著标明「已修改」及相应日期；
- 你基于本内容派生的作品，整体也必须以 GPLv3（或更高版本）授权；
- 本内容不提供任何担保（见 GPLv3 §15、§16）。

每个 Java 源文件顶部的声明形式如下：

```java
// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)
```

## 2. 美术素材与创作数据 —— 保留所有权利（All Rights Reserved）

以下内容**不属于**第 1 节的 GPL 授权范围，版权归 XCM Team 所有，**保留所有权利**：

- `src/main/resources/assets/**`：贴图（`textures/**`）、模型与界面 JSON、语言文件等美术素材；
- `src/main/resources/gen/**`：树形预设与蓝图等创作源数据（`.shape`、`.json`、`.nbt`）；
- `src/main/resources/data/**`：手工编写的游戏数据内容。

**允许**：随本模组原样分发（例如放入整合包、服务器、视频演示）。

**不允许**：单独提取、修改、再分发、二次创作，或用于其它项目；如需上述用途，请先联系作者获得授权。

> 说明：GPLv3 允许同一份发行物中的不同部分采用不同许可（聚合作品），因此「代码采用 GPL、素材保留所有权利」并不冲突；也正因如此，本节的范围划分必须被遵守——不能因为代码是自由的，就认为贴图等素材可以自由取用。

## 3. 第三方与模板文件

- **NeoForged MDK 模板文件**：本仓库基于 [NeoForged MDK](https://github.com/NeoForged/MDK) 模板创建，由模板提供的文件为 **MIT** 许可，声明见 [`TEMPLATE_LICENSE.txt`](TEMPLATE_LICENSE.txt)。
- **Gradle Wrapper**：`gradlew`、`gradlew.bat`、`gradle/wrapper/**` 来自 Gradle 项目，适用其自身许可（Apache License 2.0）。
- **第三方依赖**：NeoForge、Curios、JEI、GeckoLib、Sodium、Iris、Xaero's Minimap/World Map、LDLib2 等依赖项遵循各自许可证，本项目不对其作任何再授权。
- **Mojang 素材**：本项目不分发任何 Mojang 的版权素材（原版贴图、音效、模型、数据等）。

## 4. 贡献

向本仓库提交贡献（例如 Pull Request）即表示你同意：你所提交的**代码贡献**以 GPL-3.0-or-later 授权，**美术与创作数据贡献**按第 2 节的方式处理（即授权本项目在保留所有权利的前提下使用与分发）。
