// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

/**
 * 树形<b>预设</b>：一个木种生长行为与外观的完整声明。
 * <p>
 * <b>核心模型是「桶」</b>：key 是树苗矩阵边长 N，value 是「长成哪个形状」的带权重候选表。
 * 种植时按玩家摆出的矩阵大小取对应桶，桶内加权抽签。这是原版 {@code TreeGrower}
 * 「主树形 / 次树形 + secondaryChance」那套机制的推广——原版只能表达 1x1 与 2x2、且概率写死在
 * {@code secondaryChance} 里；换成桶 + 权重后可以精确表达原版概率
 * （例如橡木 {@code 1x1: oak@90 + fancy_oak@10}），也能表达任意 N，还能让同一 N 下有多个形状。
 * <p>
 * <b>自然生成</b>不受桶限制：每次都从「全部桶的并集」里抽。抽样权重 = 形状权重 × 桶权重，
 * 桶权重默认取 {@code 1/N²}（见 {@link #generationWeight(int, int)}），让 3x3、4x4 这类大树
 * 不至于和 1x1 小树一样常见。
 * <p>
 * 预设<b>不进注册表</b>（原因见 {@link TreePresetCatalog}），因此桶里直接持有 {@link TreeShape}
 * 而不是 {@code Holder<TreeShape>}。
 *
 * @param id             预设 id，例如 {@code science_and_magic:fancy}
 * @param buckets        桶：矩阵边长 N → 带权重候选表；不含空桶
 * @param bucketWeights  桶权重覆盖（N → 权重）；未声明的桶用默认 {@code 1/N²}
 * @param placementCount 自然生成时每个区块的尝试次数
 */
public record TreePreset(Identifier id, Map<Integer, WeightedList<TreeShape>> buckets,
        Map<Integer, Double> bucketWeights, int placementCount) {

    /**
     * 支持的最大树苗矩阵边长。
     * <p>
     * 生长时要对每个候选 N 做一次 NxN 扫描，成本是 O(N⁴)；
     * 再大手笔的蓝图在游戏里也不实用，因此设成硬上限而不是静默容忍。
     */
    public static final int MAX_MATRIX_SIZE = 16;

    /** 未声明桶权重时的默认规则：{@code 1/N²}。 */
    public static double defaultBucketWeight(int size) {
        return 1.0 / (size * (double) size);
    }

    public TreePreset {
        Map<Integer, WeightedList<TreeShape>> kept = new LinkedHashMap<>();
        for (Map.Entry<Integer, WeightedList<TreeShape>> entry : buckets.entrySet()) {
            Integer size = entry.getKey();
            WeightedList<TreeShape> list = entry.getValue();
            if (size == null || size < 1) {
                throw new IllegalArgumentException("预设 " + id + " 的桶 key 必须是 >= 1 的矩阵边长，收到 " + size);
            }
            // 空桶直接忽略：它只代表「这个 N 下没有形状」，留着只会让描述与遍历变复杂。
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (Weighted<TreeShape> weighted : list.unwrap()) {
                TreeShape shape = weighted.value();
                if (shape == null) {
                    throw new IllegalArgumentException("预设 " + id + " 的桶 " + size + "x" + size + " 里有 null 形状");
                }
                if (shape.size() != size) {
                    throw new IllegalArgumentException("预设 " + id + " 的桶 " + size + "x" + size
                            + " 里混入了边长 " + shape.size() + " 的形状 " + shape.id());
                }
            }
            kept.put(size, list);
        }
        if (kept.isEmpty()) {
            throw new IllegalArgumentException("预设 " + id + " 没有任何非空桶，树苗永远长不出东西");
        }
        if (placementCount < 0) {
            throw new IllegalArgumentException("placementCount 不能为负：" + placementCount);
        }
        // 必须用 LinkedHashMap 保留声明顺序，不能用 Map.copyOf：后者的迭代顺序带 JVM 级哈希扰动
        // （ImmutableCollections 的 SALT），同一个源在不同次运行里会生成数组顺序不同的产物，
        // 于是每次 runData 都产生无谓 diff。桶顺序还直接决定地物里加权选择器的成员顺序。
        buckets = Collections.unmodifiableMap(new LinkedHashMap<>(kept));
        bucketWeights = Collections.unmodifiableMap(new LinkedHashMap<>(bucketWeights));
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    /** 该 N 的桶；不存在时返回 {@code null}。 */
    public WeightedList<TreeShape> bucket(int size) {
        return this.buckets.get(size);
    }

    /** 最大的桶边长，即「最多支持几 x 几 的树苗矩阵」。 */
    public int maxSize() {
        int max = 1;
        for (Integer size : this.buckets.keySet()) {
            max = Math.max(max, size);
        }
        return max;
    }

    /** 单株树苗能否生长（与原版深色橡木「只有 2x2 才能长」相对）。 */
    public boolean growsFromSingleSapling() {
        return this.buckets.containsKey(1);
    }

    /**
     * 自然生成时某个桶成员的抽样权重。
     * <p>
     * {@code Weighted.weight} 是 int，而桶权重是分数，因此统一放大 1000 倍后取整；
     * 所有成员同乘一个常数不影响相对概率。
     */
    public int generationWeight(int size, int shapeWeight) {
        double bucketWeight = this.bucketWeights.getOrDefault(size, defaultBucketWeight(size));
        return Math.max(1, (int) Math.round(bucketWeight * shapeWeight * 1000.0));
    }

    /**
     * 本预设用到的全部形状（按 id 去重，保持声明顺序）。
     * <p>
     * 地物只为这些形状注册，避免为用不到的形状生成死数据。
     */
    public List<TreeShape> allShapes() {
        Set<Identifier> seen = new LinkedHashSet<>();
        List<TreeShape> out = new ArrayList<>();
        for (WeightedList<TreeShape> list : this.buckets.values()) {
            for (Weighted<TreeShape> weighted : list.unwrap()) {
                TreeShape shape = weighted.value();
                if (seen.add(shape.id())) {
                    out.add(shape);
                }
            }
        }
        return List.copyOf(out);
    }

    /** 供日志使用的可读描述，例如 {@code 1x1=pine@1, 2x2=mega_pine@1}。 */
    public String describe() {
        return this.buckets.entrySet().stream()
                .map(entry -> entry.getKey() + "x" + entry.getKey() + "=" + entry.getValue().unwrap().stream()
                        .map(weighted -> weighted.value().name() + "@" + weighted.weight())
                        .collect(Collectors.joining("+")))
                .collect(Collectors.joining(", "));
    }

    // ------------------------------------------------------------------
    // builder
    // ------------------------------------------------------------------

    /** 供 datagen 侧（{@code TreeSourceLoader}）与运行时 JSON 解析共用的装配器。 */
    public static final class Builder {

        private final Identifier id;
        private final Map<Integer, List<Weighted<TreeShape>>> buckets = new LinkedHashMap<>();
        private final Map<Integer, Double> bucketWeights = new LinkedHashMap<>();
        private int placementCount = 1;

        private Builder(Identifier id) {
            this.id = id;
        }

        /** 自然生成时每区块的尝试次数（默认 1）。 */
        public Builder placementCount(int count) {
            this.placementCount = count;
            return this;
        }

        /** 覆盖某个桶在自然生成时的抽样权重（默认 {@code 1/N²}）。 */
        public Builder bucketWeight(int size, double weight) {
            this.bucketWeights.put(size, weight);
            return this;
        }

        public Builder add(int size, TreeShape shape, int weight) {
            if (shape != null) {
                this.buckets.computeIfAbsent(size, ignored -> new ArrayList<>())
                        .add(new Weighted<>(shape, weight));
            }
            return this;
        }

        public Builder add(int size, TreeShape shape) {
            return this.add(size, shape, 1);
        }

        public TreePreset build() {
            Map<Integer, WeightedList<TreeShape>> built = new LinkedHashMap<>();
            this.buckets.forEach((size, list) -> built.put(size, WeightedList.of(list)));
            return new TreePreset(this.id, built, this.bucketWeights, this.placementCount);
        }
    }
}
