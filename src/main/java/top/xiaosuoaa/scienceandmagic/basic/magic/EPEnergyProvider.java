package top.xiaosuoaa.scienceandmagic.basic.magic;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class EPEnergyProvider implements ICapabilityProvider<Player,Void, EPEnergy>, INBTSerializable<Tag> {
	private EPEnergy epStorage = null;
	private Player player;

	private EPEnergy createPlayerEP(@NotNull Player object) {
		player = object;
		int level = object.experienceLevel;
		double v = level * level / 0.1 + 100;
        if (this.epStorage == null) {
	        this.epStorage = new EPEnergy((int) v, 0);
        } else {
	        this.epStorage.setMaxEp((int) v);
        }
        return this.epStorage;
    }

	@Override
	public @Nullable EPEnergy getCapability(@NotNull Player object, Void context) {
		return this.createPlayerEP(object);
	}

	@Override
	public @UnknownNullability Tag serializeNBT(HolderLookup.@NotNull Provider provider) {
		CompoundTag nbt = new CompoundTag();
        createPlayerEP(player).saveNBTData(nbt);
        return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull Tag nbt) {
		createPlayerEP(player).loadNBTData((CompoundTag) nbt);
	}

	public Player getPlayer() {
		return player;
	}
}
