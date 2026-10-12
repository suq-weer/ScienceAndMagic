
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
