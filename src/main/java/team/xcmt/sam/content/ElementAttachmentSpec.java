package team.xcmt.sam.content;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.ElementType;

/**
 * 武器 / 伤害携带的元素附着（数据组件，注册名 {@code science_and_magic:element_attachment}）。
 * <p>
 * 这是「元素来源」的载体：把该组件挂到武器上，玩家用这把武器打出的伤害就会带上对应元素。
 * 结算时按 {@code DamageSource#getWeaponItem()} 读取，因此不需要改动原版 {@link DamageSource}
 * 的任何字段——原版伤害来源本身就持有 direct entity 的武器。
 * <p>
 * <b>附着武器统一二级强度</b>（{@link #WEAPON_STRENGTH} = 2）：克制判定会消耗该附着 1 级，
 * 剩 1 级（不为 0）即可"传染"给被攻击实体。
 */
public record ElementAttachmentSpec(ElementType element, int strength) {

    /** 所有「附着武器」统一的强度。 */
    public static final int WEAPON_STRENGTH = 2;

    public static final Codec<ElementAttachmentSpec> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    StringRepresentable.fromEnum(ElementType::values).fieldOf("element")
                            .forGetter(ElementAttachmentSpec::element),
                    Codec.INT.optionalFieldOf("strength", WEAPON_STRENGTH)
                            .forGetter(ElementAttachmentSpec::strength))
            .apply(instance, ElementAttachmentSpec::new));

    public static final StreamCodec<ByteBuf, ElementAttachmentSpec> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, spec -> spec.element().ordinal(),
            ByteBufCodecs.VAR_INT, ElementAttachmentSpec::strength,
            (ordinal, strength) -> new ElementAttachmentSpec(ElementType.values()[ordinal], strength));

    /** 数据组件注册表。 */
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,
                    ScienceAndMagic.MOD_ID);

    /** 元素附着组件。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ElementAttachmentSpec>> COMPONENT =
            DATA_COMPONENTS.registerComponentType("element_attachment",
                    builder -> builder.persistent(CODEC).networkSynchronized(STREAM_CODEC));

    /** 便捷构造：附着武器统一二级强度。 */
    public static ElementAttachmentSpec weapon(ElementType element) {
        return new ElementAttachmentSpec(element, WEAPON_STRENGTH);
    }

    /**
     * 读取伤害来源携带的元素附着（取自 direct entity 的武器）。
     *
     * @return 未携带时返回 {@code null}
     */
    @Nullable
    public static ElementAttachmentSpec of(DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        return weapon == null ? null : weapon.get(COMPONENT);
    }
}
