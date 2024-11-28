package top.xiaosuoaa.scienceandmagic.item.magic.init;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

public class InitItemModel<T extends GeoItem> extends GeoModel<T> {
	private final ResourceLocation textureResource;

	public InitItemModel(String location) {
		textureResource = ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "textures/item/" + location);
	}

	@Override
	public ResourceLocation getModelResource(T animatable) {
		return ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "geo/init.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(T animatable) {
		return textureResource;
	}

	@Override
	public ResourceLocation getAnimationResource(T animatable) {
		return ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "animations/init.animation.json");
	}
}
