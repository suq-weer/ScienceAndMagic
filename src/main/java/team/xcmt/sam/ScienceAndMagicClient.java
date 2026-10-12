package team.xcmt.sam;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.client.OklabRecolorSpriteSource;

/**
 * 客户端注册：图集重着色源、船实体渲染器与船模型层。
 * <p>
 * 这里注册的三个事件（{@code RegisterSpriteSourcesEvent}、{@code EntityRenderersEvent.RegisterLayerDefinitions}、
 * {@code EntityRenderersEvent.RegisterRenderers}）都是 mod 总线事件；FML 的 {@code @EventBusSubscriber}
 * 只接受 {@code value}/{@code modid}，会依据事件类型自动投递到正确的总线。
 * <p>
 * <b>染色不在客户端做</b>。木种的方块/物品贴图由自定义 sprite source
 * （{@code science_and_magic:oklab_recolor}）在装配图集时逐像素重着色，
 * 因此这里<b>没有</b>也不需要 {@code BlockTintSource} 注册。
 * <p>
 * 模型面不再带 {@code tintindex}，方块渲染层（cutout/translucent）由 26.2 依据 sprite 自身的
 * {@code Transparency} 自动推导，同样无需注册。
 */
@EventBusSubscriber(Dist.CLIENT)
public class ScienceAndMagicClient {

    /**
     * 注册自定义图集源类型 {@code science_and_magic:oklab_recolor}。
     * <p>
     * 木种的方块/物品贴图就是靠它在装配图集时从原版贴图动态重着色出来的，
     * 因此这个事件是整套木头外观生效的前提。
     */
    @SubscribeEvent
    public static void registerSpriteSources(RegisterSpriteSourcesEvent event) {
        OklabRecolorSpriteSource.register(event);
    }

    /**
     * 注册船 / 箱船的模型层。
     * <p>
     * {@code BoatRenderer} 会用「模型层路径」推导贴图路径，因此层路径
     * {@code <ns>:boat/<wood>_boat} 对应数据生成阶段烘焙出的
     * {@code <ns>:textures/entity/boat/<wood>_boat.png}。
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (WoodSet set : WoodSet.all()) {
            if (set.boatEntity() == null) {
                continue;
            }
            WoodMaterial material = set.material();
            event.registerLayerDefinition(boatLayer(material), BoatModel::createBoatModel);
            event.registerLayerDefinition(chestBoatLayer(material), BoatModel::createChestBoatModel);
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (WoodSet set : WoodSet.all()) {
            if (set.boatEntity() == null) {
                continue;
            }
            WoodMaterial material = set.material();
            event.registerEntityRenderer(set.boatEntity().get(),
                    context -> new BoatRenderer(context, boatLayer(material)));
            event.registerEntityRenderer(set.chestBoatEntity().get(),
                    context -> new BoatRenderer(context, chestBoatLayer(material)));
        }
    }

    private static ModelLayerLocation boatLayer(WoodMaterial material) {
        return new ModelLayerLocation(
                Identifier.fromNamespaceAndPath(material.namespace(), material.boatPath("boat")), "main");
    }

    private static ModelLayerLocation chestBoatLayer(WoodMaterial material) {
        return new ModelLayerLocation(
                Identifier.fromNamespaceAndPath(material.namespace(), material.boatPath("chest_boat")), "main");
    }
}
