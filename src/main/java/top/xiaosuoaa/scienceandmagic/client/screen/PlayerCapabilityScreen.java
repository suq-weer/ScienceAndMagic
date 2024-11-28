package top.xiaosuoaa.scienceandmagic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapability;
import top.xiaosuoaa.scienceandmagic.menu.PlayerCapabilityMenu;

public class PlayerCapabilityScreen extends AbstractContainerScreen<PlayerCapabilityMenu> {
	private static final ResourceLocation texture = ResourceLocation.parse("science_and_magic:textures/screens/player_capability_gui.png");

	public PlayerCapabilityScreen(PlayerCapabilityMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.imageWidth = 196;
		this.imageHeight = 136;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		LocalPlayer localPlayer = getMinecraft().player;
		if (localPlayer instanceof LivingEntity livingEntity) {
			this.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 35, this.topPos + 62, 0f + (float) Math.atan((this.leftPos + 35 - mouseX) / 40.0), (float) Math.atan((this.topPos + 13 - mouseY) / 40.0), livingEntity);
		}
		this.renderTooltip(guiGraphics, mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 35 && mouseY < topPos + 44)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_base_attack"), mouseX, mouseY);
		if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 35 && mouseY < topPos + 44)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_base_defend"), mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 19 && mouseY < topPos + 28)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_max_ep"), mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 54 && mouseY < topPos + 63)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_blood_counter"), mouseX, mouseY);
		if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 54 && mouseY < topPos + 63)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_blood_count"), mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 73 && mouseY < topPos + 82)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_physic_attack"), mouseX, mouseY);
		if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 73 && mouseY < topPos + 82)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_magic_attack"), mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 91 && mouseY < topPos + 100)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_science_attack"), mouseX, mouseY);
		if (mouseX > leftPos + 70 && mouseX < leftPos + 94 && mouseY > topPos + 109 && mouseY < topPos + 118)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_physic_defend"), mouseX, mouseY);
		if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 109 && mouseY < topPos + 118)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_magic_defend"), mouseX, mouseY);
		if (mouseX > leftPos + 7 && mouseX < leftPos + 31 && mouseY > topPos + 66 && mouseY < topPos + 75)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_player_energy"), mouseX, mouseY);
		if (mouseX > leftPos + 7 && mouseX < leftPos + 31 && mouseY > topPos + 85 && mouseY < topPos + 94)
			guiGraphics.renderTooltip(font, Component.translatable("gui.science_and_magic.player_capability_gui.tooltip_magic_count"), mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			if (this.minecraft != null && this.minecraft.player != null) {
				this.minecraft.player.closeContainer();
			}
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
		LocalPlayer localPlayer = getMinecraft().player;
		if (localPlayer != null) {
			PlayerCapability capability = localPlayer.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER);
			if (capability != null) {
				guiGraphics.drawString(this.font, Component.literal("mEP: ").append(String.valueOf(capability.getMEp())), 70, 19, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("bATK: ").append(String.valueOf(capability.getBaseATK())), 70, 37, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("bDEF: ").append(String.valueOf(capability.getBaseDEF())), 124, 37, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("BLD: ").append(String.valueOf(capability.getBLD() * 100).substring(0, 3) + "%"), 70, 55, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("BLDk: ").append(String.valueOf(capability.getBLDk())), 124, 55, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("pATK: ").append(String.valueOf(capability.getPhysicATK() * 100).substring(0, 3) + "%"), 70, 73, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("mATK: ").append(String.valueOf(capability.getMagicATK() * 100).substring(0, 3) + "%"), 124, 73, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("sATK: ").append(String.valueOf(capability.getScienceATK() * 100).substring(0, 3) + "%"), 70, 91, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("pDEF: ").append(String.valueOf(capability.getPhysicDEF() * 100).substring(0, 3) + "%"), 70, 109, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("mDEF: ").append(String.valueOf(capability.getMagicDEF() * 100).substring(0, 3) + "%"), 124, 109, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("PE: ").append(String.valueOf(capability.getPE() * 100).substring(0, 3) + "%"), 7, 66, -12829636, false);
				guiGraphics.drawString(this.font, Component.literal("MC: ").append(String.valueOf(capability.getMC() * 100).substring(0, 3) + "%"), 7, 86, -12829636, false);
				guiGraphics.drawString(this.font, Component.translatable("gui.science_and_magic.player_capability_gui.label_player_capability_gui"), 7, 6, -12829636, false);
			}
		} else {
			guiGraphics.drawString(this.font, Component.literal("Error! No player Capability!"), 70, 19, -12829636, false);
		}
	}

	@Override
	public void init() {
		super.init();
	}

	private void renderEntityInInventoryFollowsAngle(GuiGraphics guiGraphics, int x, int y, float angleXComponent, float angleYComponent, LivingEntity entity) {
		Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf cameraOrientation = new Quaternionf().rotateX(angleYComponent * 20 * ((float) Math.PI / 180F));
		pose.mul(cameraOrientation);
		float f2 = entity.yBodyRot;
		float f3 = entity.getYRot();
		float f4 = entity.getXRot();
		float f5 = entity.yHeadRotO;
		float f6 = entity.yHeadRot;
		entity.yBodyRot = 180.0F + angleXComponent * 20.0F;
		entity.setYRot(180.0F + angleXComponent * 40.0F);
		entity.setXRot(-angleYComponent * 20.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		InventoryScreen.renderEntityInInventory(guiGraphics, x, y, 25, new Vector3f(0, 0, 0), pose, cameraOrientation, entity);
		entity.yBodyRot = f2;
		entity.setYRot(f3);
		entity.setXRot(f4);
		entity.yHeadRotO = f5;
		entity.yHeadRot = f6;
	}
}
