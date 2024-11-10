package top.xiaosuoaa.scienceandmagic.basic.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 *
 * @param weaponCategory 0 物理；1 魔法；2 科技
 */
public record WeaponCategoryComponentRecord(int weaponCategory) {
	public static final Codec<WeaponCategoryComponentRecord> WEAPON_CATEGORY_COMPONENT_RECORD_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(Codec.INT
					.fieldOf("weapon_category")
					.forGetter(WeaponCategoryComponentRecord::weaponCategory))
					.apply(instance, WeaponCategoryComponentRecord::new)
	);
	public static final StreamCodec<ByteBuf, WeaponCategoryComponentRecord> WEAPON_CATEGORY_COMPONENT_RECORD_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(Codec.INT), WeaponCategoryComponentRecord::weaponCategory,
        WeaponCategoryComponentRecord::new
	);
	public static final StreamCodec<Object, WeaponCategoryComponentRecord> BUF_WEAPON_CATEGORY_COMPONENT_RECORD_STREAM_CODEC = StreamCodec.unit(new WeaponCategoryComponentRecord(0));
}
