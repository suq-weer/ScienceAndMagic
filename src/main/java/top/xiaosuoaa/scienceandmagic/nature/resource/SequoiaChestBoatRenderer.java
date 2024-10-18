package top.xiaosuoaa.scienceandmagic.nature.resource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

public class SequoiaChestBoatRenderer extends BoatRenderer {
	private static final ResourceLocation SEQUOIA_CHEST_BOAT_LOCATION = ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "textures/entity/boat/sequoia_chest_boat.png");

	public SequoiaChestBoatRenderer(EntityRendererProvider.Context pContext, boolean pChestBoat) {
		super(pContext, pChestBoat);
	}

	@Override
	public @NotNull Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(@NotNull Boat pEntity) {
		return Pair.of(SEQUOIA_CHEST_BOAT_LOCATION, super.getModelWithLocation(pEntity).getSecond());
	}

	@Override
	public void render(@NotNull Boat pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
	}
}

