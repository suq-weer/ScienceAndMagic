// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;

/**
 * 树形预设的静态目录。
 * <p>
 * <b>刻意不使用 NeoForge 自定义注册表</b>：静态注册表在启动阶段结束就冻结，而数据包在那之后才加载，
 * 于是「往 {@code gen/} 丢一个形状 → 重跑 {@code runData} → 游戏内 {@code /reload}」这条链路
 * 无法通过注册表实现（注册表只能在启动时、且只能扫 mod 自身）。改成一个普通静态表之后，
 * 目录可以随时整体替换。
 * <p>
 * 装载点有两处：
 * <ul>
 *     <li><b>datagen</b>：{@code TreeSourceLoader} 读 {@code src/main/resources/gen/tree_shapes/**}
 *         并在全部校验通过后装入，
 *         供 {@link WoodTreeFeatures} 的 bootstrap 使用；</li>
 *     <li><b>运行时</b>：{@code TreePresetCatalogLoader}（服务器数据包重载监听）读
 *         {@code data/<namespace>/tree_preset/*.json} 装入——这些 JSON 由 {@code runData} 产出。</li>
 * </ul>
 * 装入的是不可变 Map 且整体原子替换（{@code volatile}），因此读侧不需要加锁。
 */
public final class TreePresetCatalog {

    private static volatile Map<Identifier, TreePreset> presets = Map.of();

    private TreePresetCatalog() {
    }

    /** 整体替换目录。传空 Map 表示「没有任何预设」。 */
    public static void install(Map<Identifier, TreePreset> loaded) {
        // 保留传入顺序（不用 Map.copyOf：它的迭代顺序带 JVM 级哈希扰动），让日志与遍历顺序在任何
        // 一次运行里都一致，便于对照。
        presets = Collections.unmodifiableMap(new LinkedHashMap<>(loaded));
    }

    /**
     * 按 id 取预设。
     *
     * @return 不存在时返回 {@code null}（调用方负责给出可读的错误信息）
     */
    public static TreePreset get(Identifier id) {
        return presets.get(id);
    }

    /** 全部预设；未装载时为空。 */
    public static Collection<TreePreset> all() {
        return presets.values();
    }

    public static boolean isEmpty() {
        return presets.isEmpty();
    }

    public static int size() {
        return presets.size();
    }
}
