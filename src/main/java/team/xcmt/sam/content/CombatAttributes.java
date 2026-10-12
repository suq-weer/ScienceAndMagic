package team.xcmt.sam.content;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;

/**
 * 模组自定义的玩家战斗属性。
 * <p>
 * 这 12 项属性全部走原版 {@link net.minecraft.core.Registry 属性注册表}，因此天然支持：
 * <ul>
 *     <li>装备 / 药水 / 魂骨通过 {@code AttributeModifier} 增减数值；</li>
 *     <li>{@code setSyncable(true)} 自动同步到客户端；</li>
 *     <li>服务器与客户端读到同一套数值，无需自建网络包。</li>
 * </ul>
 * <b>百分比约定</b>：所有标注 [%] 的属性都以「数值 100 表示 100%」存储（例如 PE=100 即 ×1.00），
 * 结算时在 {@link team.xcmt.sam.api.combat.CombatMath} 内除以 100。
 * <p>
 * <b>白板玩家的面板</b>：bATK=2、bDEF=0、BLD=50、BLDk=5、pATK=mATK=sATK=0、
 * pDEF=mDEF=0、mEP=0、PE=100、MC=100，于是非致命空手攻击的最终伤害为 2。
 */
public final class CombatAttributes {

    /** 属性注册表。 */
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ScienceAndMagic.MOD_ID);

    // ---- 基础值与致命（10 进制直接相加的"平值"属性）----

    /** bATK：基础攻击力（约等于空手攻击力）。白板 2。 */
    public static final DeferredHolder<Attribute, Attribute> BASE_ATTACK =
            reg("base_attack", 2.0, 0.0, 2048.0);

    /** bDEF：基础防御力（约等于无甲防御）。白板 0。 */
    public static final DeferredHolder<Attribute, Attribute> BASE_DEFEND =
            reg("base_defend", 0.0, 0.0, 2048.0);

    /** BLD：致命率[%]。白板 50。 */
    public static final DeferredHolder<Attribute, Attribute> BLOOD_COUNTER =
            reg("blood_counter", 50.0, 0.0, 100.0);

    /** BLDk：致命加伤。白板 5；触发致命时在 [0, BLDk] 内均匀取实数加到伤害。 */
    public static final DeferredHolder<Attribute, Attribute> BLOOD_COUNT =
            reg("blood_count", 5.0, 0.0, 2048.0);

    // ---- 加成 / 防御率（百分比属性）----

    /** pATK：物伤加成[%]。 */
    public static final DeferredHolder<Attribute, Attribute> PHYSIC_ATTACK =
            reg("physic_attack", 0.0, -1024.0, 2048.0);

    /** mATK：法伤加成[%]。 */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_ATTACK =
            reg("magic_attack", 0.0, -1024.0, 2048.0);

    /** sATK：科技熟练加成[%]（科技类伤害预留，本轮不参与结算）。 */
    public static final DeferredHolder<Attribute, Attribute> SCIENCE_ATTACK =
            reg("science_attack", 0.0, -1024.0, 2048.0);

    /** pDEF：物伤防御率[%]，触发时额外挡下 bDEF 的物理伤害。 */
    public static final DeferredHolder<Attribute, Attribute> PHYSIC_DEFEND =
            reg("physic_defend", 0.0, 0.0, 100.0);

    /** mDEF：法伤防御率[%]。 */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_DEFEND =
            reg("magic_defend", 0.0, 0.0, 100.0);

    /** mEP：魔力值上限。 */
    public static final DeferredHolder<Attribute, Attribute> MAX_EP =
            reg("max_ep", 0.0, 0.0, 100000.0);

    /** PE：体魄[%]，最终玩家物理伤 / 防加权。白板 100。 */
    public static final DeferredHolder<Attribute, Attribute> PLAYER_ENERGY =
            reg("player_energy", 100.0, 0.0, 1000.0);

    /** MC：意志力[%]，最终玩家法术伤 / 防加权。白板 100。 */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_COUNT =
            reg("magic_count", 100.0, 0.0, 1000.0);

    /** 全部自定义战斗属性（用于统一挂载与遍历）。 */
    public static final List<DeferredHolder<Attribute, Attribute>> ALL = List.of(
            BASE_ATTACK, BASE_DEFEND, BLOOD_COUNTER, BLOOD_COUNT,
            PHYSIC_ATTACK, MAGIC_ATTACK, SCIENCE_ATTACK,
            PHYSIC_DEFEND, MAGIC_DEFEND, MAX_EP,
            PLAYER_ENERGY, MAGIC_COUNT);

    private CombatAttributes() {
    }

    /**
     * 注册一项自定义属性。
     * <p>
     * 翻译键即 {@code attribute.name.science_and_magic.<id>}，与 {@code ModLanguageProvider} 对应。
     */
    private static DeferredHolder<Attribute, Attribute> reg(String id, double defaultValue,
            double minValue, double maxValue) {
        return ATTRIBUTES.register(id, () -> new RangedAttribute(
                "attribute.name." + ScienceAndMagic.MOD_ID + "." + id,
                defaultValue, minValue, maxValue).setSyncable(true));
    }

    /**
     * 把 12 项属性挂到玩家实体上。
     * <p>
     * {@link EntityAttributeModificationEvent} 是 Mod 总线事件，必须在属性注册完成之后触发，
     * 因此 {@link team.xcmt.sam.Register} 里先 {@code ATTRIBUTES.register(bus)} 再 {@code addListener}。
     */
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (DeferredHolder<Attribute, Attribute> attribute : ALL) {
            event.add(EntityTypes.PLAYER, attribute);
        }
    }
}
