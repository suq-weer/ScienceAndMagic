package top.xiaosuoaa.scienceandmagic.basic.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class PlayerCapabilityProvider implements ICapabilityProvider<Player, Void, PlayerCapability>, INBTSerializable<Tag> {
	private PlayerCapability playerCapability = null;
	private Player player;

	private PlayerCapability createPlayerEP(@NotNull Player player) {
		this.player = player;
		int level = player.experienceLevel;
		double v = level * level / 0.1 + 100;
		if (this.playerCapability == null) {
			this.playerCapability = new PlayerCapability((int) v);
		} else {
			this.playerCapability.setMEp((int) v);
		}
		return this.playerCapability;
	}

	@Override
	public @Nullable PlayerCapability getCapability(@NotNull Player object, Void context) {
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
