package team.xcmt.sam.content.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import org.jspecify.annotations.NonNull;
import team.xcmt.sam.content.ModParticles;

/**
 * 伤害数字粒子的参数：数值 + 颜色 + 缩放。
 * <p>
 * 之所以把「颜色与缩放」也放进参数而不是让客户端自己判断，是因为伤害数字的样式由<b>服务端</b>
 * 决定（例如「是否致命」只有服务端的战斗结算知道），客户端只负责画出来。
 *
 * @param damage 要显示的数字（服务端已按规则取整/取整前的实际扣血量）
 * @param color  颜色（24 位 RGB，不含透明度；透明度由粒子的淡出过程计算）
 * @param scale  额外缩放倍率（例如致命时放大）
 */
public record DamageNumberParticleOptions(float damage, int color, float scale) implements ParticleOptions {

    public static final MapCodec<DamageNumberParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.FLOAT.fieldOf("damage").forGetter(DamageNumberParticleOptions::damage),
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(DamageNumberParticleOptions::color),
                    Codec.FLOAT.fieldOf("scale").forGetter(DamageNumberParticleOptions::scale))
            .apply(instance, DamageNumberParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageNumberParticleOptions> STREAM_CODEC = StreamCodec
            .composite(ByteBufCodecs.FLOAT, DamageNumberParticleOptions::damage,
                    ByteBufCodecs.INT, DamageNumberParticleOptions::color,
                    ByteBufCodecs.FLOAT, DamageNumberParticleOptions::scale,
                    DamageNumberParticleOptions::new);

    @Override
    public @NonNull ParticleType<?> getType() {
        return ModParticles.DAMAGE_NUMBER.get();
    }
}
