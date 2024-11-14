package top.xiaosuoaa.scienceandmagic.item.magic.init;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class InitItemRenderer<T extends BaseInitItem> extends GeoItemRenderer<T> {
	public InitItemRenderer(GeoModel<T> model) {
		super(model);
	}
}
