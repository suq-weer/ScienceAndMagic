package team.xcmt.sam.datagen;

import net.minecraft.data.PackOutput;

/**
 * 生成方块状态文件；所有木种共享 {@link WoodBlockStateTemplates} 中的模板。
 */
public final class WoodBlockStateProvider extends TemplatedJsonProvider {

    public WoodBlockStateProvider(PackOutput output) {
        super(output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates"),
                WoodBlockStateTemplates.TEMPLATES, "Wood Block States");
    }
}
