package top.xiaosuoaa.scienceandmagic.basic.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EPEnergy implements IEnergyStorage {
	protected int ep;
    protected int maxEp;

	public EPEnergy(int maxEp, int ep) {
		this.maxEp = maxEp;
		this.ep = Math.max(0, Math.min(maxEp, ep));
	}

	@Override
	public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }
        int energyReceived = Mth.clamp(this.maxEp - this.ep, 0, toReceive);
        if (!simulate)
            this.ep += energyReceived;
        return energyReceived;
    }


    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        int energyExtracted = Math.min(this.ep, toExtract);
        if (!simulate)
            this.ep -= energyExtracted;
        return energyExtracted;
    }


    @Override
    public int getEnergyStored() {
        return this.ep;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.maxEp;
    }

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	public void setEp(int ep) {
		this.ep = ep;
	}

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("ep", ep);
    }

    // 从 NBT 数据中加载口渴值
    public void loadNBTData(CompoundTag nbt) {
        ep = nbt.getInt("ep");
    }

	public void setMaxEp(int maxEp1) {
		maxEp = maxEp1;
	}
}
