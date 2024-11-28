package top.xiaosuoaa.scienceandmagic.basic.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ElementComponentRecord(List<String> element) {
	public static final Codec<ElementComponentRecord> ELEMENT_COMPONENT_RECORD_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(Codec.list(Codec.STRING)
							.fieldOf("element")
							.forGetter(ElementComponentRecord::element))
					.apply(instance, ElementComponentRecord::new)
	);
	public static final StreamCodec<ByteBuf, ElementComponentRecord> ELEMENT_COMPONENT_RECORD_STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.fromCodec(Codec.list(Codec.STRING)), ElementComponentRecord::element,
			ElementComponentRecord::new
	);
	public static final StreamCodec<ByteBuf, ElementComponentRecord> BUF_ELEMENT_COMPONENT_RECORD_STREAM_CODEC = StreamCodec.unit(new ElementComponentRecord(new ArrayList<>()));
	public static final String FIRE = "fire";
	public static final String ICE = "ice";
	public static final String WATER = "water";
	public static final String STONE = "stone";
	public static final String WOOD = "wood";
	public static final String LIGHTING = "lighting";

	/*
	 * 读取元素组件转换为组件外显译名。
	 */
	public static void elementTranslateCom(@NotNull List<String> list, List<Component> componentList) {
		MutableComponent line = Component.translatable("tooltip.science_and_magic.element_list").withStyle(ChatFormatting.BOLD);
		for (String s : list) {
			if (Objects.equals(s, FIRE)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_fire_short").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
			}
			if (Objects.equals(s, ICE)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_ice_short").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
			}
			if (Objects.equals(s, WATER)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_water_short").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD));
			}
			if (Objects.equals(s, STONE)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_stone_short").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD));
			}
			if (Objects.equals(s, WOOD)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_wood_short").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
			}
			if (Objects.equals(s, LIGHTING)) {
				line.append(Component.translatable("tooltip.science_and_magic.element_lighting_short").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
			}
			line.append(" ");
		}
		componentList.add(line);
	}
}
