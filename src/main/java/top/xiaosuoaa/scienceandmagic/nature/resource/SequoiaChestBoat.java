package top.xiaosuoaa.scienceandmagic.nature.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SequoiaChestBoat extends ChestBoat {
	public SequoiaChestBoat(EntityType<? extends ChestBoat> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("entity.science_and_magic.sequoia_chest_boat");
	}
}
