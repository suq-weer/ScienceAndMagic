package team.xcmt.sam.content;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * {@link WoodTemplateFeature} 的配置：一个蓝图 + 一个木种 + 树苗矩阵边长。
 * <p>
 * 刻意保持极简——把「木种」写成 id 而不是把方块表塞进配置里，运行时再用
 * {@code WoodSet.byId} 反查即可。这样每个 {@code (木种, 蓝图形状)} 组合的配置 JSON 都很小，
 * 也不会因为方块表变化而失效。
 * <p>
 * 因为要按木种替换方块，所以<b>不能</b>直接用原版的 {@code Feature.TEMPLATE}
 * ——它的配置里没有木种，也没有地方挂处理器。
 *
 * @param template  蓝图 id（{@code data/<ns>/structure/**} 之下的 .nbt）
 * @param wood      目标木种 id（{@code science_and_magic:sequoia}）
 * @param rotations 允许的随机旋转，默认只有不旋转
 * @param size      树苗矩阵边长 N，也就是蓝图在桶目录里的 {@code <N>x<N>}。
 *                  锚点检测要靠它做「NxN 足迹匹配」——没有 N 就只能退化成按包围盒猜。
 *                  它是<b>必填</b>字段：给默认值会导致静默错位，宁可让手写的 JSON
 *                  在数据包加载期直接报错
 */
public record WoodTemplateConfiguration(Identifier template, Identifier wood, List<Rotation> rotations, int size)
        implements FeatureConfiguration {

    public static final Codec<WoodTemplateConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(WoodTemplateConfiguration::template),
            Identifier.CODEC.fieldOf("wood").forGetter(WoodTemplateConfiguration::wood),
            Rotation.CODEC.listOf().optionalFieldOf("rotations", List.of(Rotation.NONE))
                    .forGetter(WoodTemplateConfiguration::rotations),
            Codec.INT.fieldOf("size").forGetter(WoodTemplateConfiguration::size))
            .apply(instance, WoodTemplateConfiguration::new));

    public WoodTemplateConfiguration {
        rotations = List.copyOf(rotations);
    }
}
