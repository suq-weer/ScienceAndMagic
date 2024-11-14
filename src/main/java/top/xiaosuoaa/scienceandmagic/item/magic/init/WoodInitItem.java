package top.xiaosuoaa.scienceandmagic.item.magic.init;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord;

import java.util.List;
import java.util.function.Consumer;

public class WoodInitItem extends BaseInitItem implements GeoItem {
	public WoodInitItem() {
		super(List.of(ElementComponentRecord.WOOD));
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		consumer.accept(new GeoRenderProvider() {
			private InitItemRenderer<WoodInitItem> renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
				if (this.renderer == null)
					this.renderer = new InitItemRenderer<>(new InitItemModel<>("wood_init.png"));
				return this.renderer;
			}
		});
	}
}
