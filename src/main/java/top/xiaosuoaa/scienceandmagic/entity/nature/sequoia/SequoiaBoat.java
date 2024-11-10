package top.xiaosuoaa.scienceandmagic.entity.nature.sequoia;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;

public class SequoiaBoat extends Boat {
	public SequoiaBoat(EntityType<? extends Boat> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("entity.science_and_magic.sequoia_boat");
	}
}
