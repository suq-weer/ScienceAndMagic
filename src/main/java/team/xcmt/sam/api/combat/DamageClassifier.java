package team.xcmt.sam.api.combat;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

/**
 * 伤害归类：按具体 {@link DamageSource} 判定物理 / 法术 / 科技。
 * <p>
 * 判定顺序：
 * <ol>
 *     <li>科技伤害：由模组显式登记（{@link #registerTech}），默认空；</li>
 *     <li>法术伤害：命中内置法术伤害类型集合，或伤害 id 含 {@code magic}；</li>
 *     <li>其余一律按物理处理（含坠落、挤压、爆炸等环境伤害）。</li>
 * </ol>
 */
public final class DamageClassifier {

    /** 内置法术伤害类型。 */
    private static final Set<ResourceKey<DamageType>> MAGIC = Set.of(
            DamageTypes.MAGIC,
            DamageTypes.INDIRECT_MAGIC,
            DamageTypes.THORNS,
            DamageTypes.DRAGON_BREATH,
            DamageTypes.WITHER,
            DamageTypes.WITHER_SKULL);

    /** 模组登记的科技伤害类型。 */
    private static final Set<ResourceKey<DamageType>> TECH = new CopyOnWriteArraySet<>();

    private DamageClassifier() {
    }

    /** 登记科技类伤害类型（sATK 预留）。 */
    public static void registerTech(ResourceKey<DamageType> damageType) {
        TECH.add(damageType);
    }

    /** 归类一次伤害。 */
    public static DamageCategory classify(DamageSource source) {
        if (source.typeHolder().unwrapKey().map(TECH::contains).orElse(false)) {
            return DamageCategory.TECH;
        }
        if (MAGIC.contains(source.typeHolder().unwrapKey().orElse(null))) {
            return DamageCategory.MAGIC;
        }
        if (source.getMsgId().contains("magic")) {
            return DamageCategory.MAGIC;
        }
        return DamageCategory.PHYSICAL;
    }
}
