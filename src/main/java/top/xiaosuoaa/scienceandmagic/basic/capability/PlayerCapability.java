package top.xiaosuoaa.scienceandmagic.basic.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

import java.util.Random;

public class PlayerCapability {
	// 动态（高刷新）属性
	private int ep;
	
	// 静态（低刷新）属性
    private int mEp; // 魔力值上限
	private int bATK; // 基础攻击力
	private int bDEF; // 基础防御力
	private double BLD; //致命率
	private int BLDk; // 致命加伤
	private double pATK; // 物伤加成
	private double mATK; // 法伤加成
	private double sATK; // 科技熟练加成
	private double pDEF; // 物伤防御率
	private double mDEF; // 法伤防御率
	private double PE; //体魄
	private double MC; // 意志力

	public static int useAttackCount(Player player, int damageType) {
		PlayerCapability playerCapability = player.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER);
		if (playerCapability != null) {
			double bpATK = 0;
			switch (damageType) {
				case 0 -> bpATK = playerCapability.getBaseATK() * playerCapability.getPhysicATK();
				case 1 -> bpATK = playerCapability.getBaseATK() * playerCapability.getMagicATK();
				case 2 -> bpATK = playerCapability.getBaseATK() * playerCapability.getScienceATK();
			}
			Random random = new Random();
			double isBLD = random.nextDouble();
			if (isBLD <= playerCapability.getBLD()) {
				bpATK += random.nextInt(0, playerCapability.getBLDk());
			}
			bpATK *= playerCapability.getPE();
			return (int) bpATK;
		}
		return 1;
	}

	/**
	 * 保存玩家属性数据方法。
	 */
	public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("ep", ep);
		nbt.putInt("mEp", mEp);
		nbt.putInt("bATK", bATK);
		nbt.putInt("bDEF", bDEF);
		nbt.putDouble("BLD", BLD);
		nbt.putInt("BLDk", BLDk);
		nbt.putDouble("pATK", pATK);
		nbt.putDouble("mATK", mATK);
		nbt.putDouble("sATK", sATK);
		nbt.putDouble("pDEF", pDEF);
		nbt.putDouble("mDEF", mDEF);
		nbt.putDouble("PE", PE);
		nbt.putDouble("MC", MC);
    }

	/**
	 * 加载玩家属性数据方法。
	 */
    public void loadNBTData(CompoundTag nbt) {
        ep = nbt.getInt("ep");
		mEp = nbt.getInt("mEp");
		bATK = nbt.getInt("bATK");
		bDEF = nbt.getInt("bDEF");
		BLD = nbt.getDouble("BLD");
		BLDk = nbt.getInt("BLDk");
		pATK = nbt.getDouble("pATK");
		mATK = nbt.getDouble("mATK");
		sATK = nbt.getDouble("sATK");
		pDEF = nbt.getDouble("pDEF");
		mDEF = nbt.getDouble("mDEF");
		PE = nbt.getDouble("PE");
		MC = nbt.getDouble("MC");
    }

	/**
	 * 初始化所有数值
	 * @param mEp 经验计算设置入口，创建时请注意最大值EP与原版经验绑定。
	 */
	public PlayerCapability(int mEp) {
		this.mEp = mEp;
		this.ep = 0;
		this.bATK = 1;
		this.bDEF = 0;
		this.BLD = 0.5d;
		this.BLDk = 1;
		this.pATK = 1.0d;
		this.mATK = 1.0d;
		this.sATK = 1.0d;
		this.pDEF = 0.0d;
		this.mDEF = 0.0d;
		this.PE = 1.0d;
		this.MC = 1.0d;
	}

	// mEP/EP
	public void addEp(int toReceive, boolean simulate) {
        if (!canLessEp() || toReceive <= 0) {
            return;
        }
        int energyReceived = Mth.clamp(this.mEp - this.ep, 0, toReceive);
        if (!simulate)
            this.ep += energyReceived;
	}
    public void lessEp(int toExtract, boolean simulate) {
        if (!canAddEp() || toExtract <= 0) {
            return;
        }

        int energyExtracted = Math.min(this.ep, toExtract);
        if (!simulate)
            this.ep -= energyExtracted;
    }
    public int getEp() {
		return this.ep;
	}
    public int getMEp() {
        return this.mEp;
    }
	public boolean canAddEp() {
		return true;
	}
	public boolean canLessEp() {
		return true;
	}
	public void setEp(int ep) {
		this.ep = ep;
	}
	public void setMEp(int maxEp1) {
		mEp = maxEp1;
	}

	// bATK
	public int getBaseATK() {
		return bATK;
	}
	public void setBaseATK(int willSet) {
		bATK = willSet;
	}
	public void addBaseATK(int willAdd) {
		bATK += willAdd;
	}
	public void lessBaseATK(int willLess) {
		bATK -= willLess;
	}

	//bDEF
	public int getBaseDEF() {
		return bDEF;
	}
	public void setBaseDEF(int willSet) {
		bDEF = willSet;
	}
	public void addBaseDEF(int willAdd) {
		bDEF += willAdd;
	}
	public void lessBaseDEF(int willLess) {
		bDEF -= willLess;
	}
	
	// BLD
	public double getBLD() {
		return BLD;
	}
	public void setBLD(double willSet) {
		BLD = willSet;
	}
	public void addBLD(double willAdd) {
		BLD += willAdd;
	}
	public void lessBLD(double willLess) {
		BLD -= willLess;
	}
	
	// BLDk
	public int getBLDk() {
		return BLDk;
	}
	public void setBLDk(int willSet) {
		BLDk = willSet;
	}
	public void addBLDk(int willAdd) {
		BLDk += willAdd;
	}
	public void lessBLDk(int willLess) {
		BLDk -= willLess;
	}
	
	// PhysicATK
	public double getPhysicATK() {
		return pATK;
	}
	public void setPhysicATK(double willSet) {
		pATK = willSet;
	}
	public void addPhysicATK(double willAdd) {
		pATK += willAdd;
	}
	public void lessPhysicATK(double willLess) {
		pATK -= willLess;
	}
	
	// mATK
	public double getMagicATK() {
		return mATK;
	}
	public void setMagicATK(double willSet) {
		mATK = willSet;
	}
	public void addMagicATK(double willAdd) {
		mATK += willAdd;
	}
	public void lessMagicATK(double willLess) {
		mATK -= willLess;
	}
	
	// sATK
	public double getScienceATK() {
		return sATK;
	}
	public void setScienceATK(double willSet) {
		sATK = willSet;
	}
	public void addScienceATK(double willAdd) {
		sATK += willAdd;
	}
	public void lessScienceATK(double willLess) {
		sATK -= willLess;
	}
	
	// pDEF
	public double getPhysicDEF() {
		return pDEF;
	}
	public void setPhysicDEF(double willSet) {
		pDEF = willSet;
	}
	public void addPhysicDEF(double willAdd) {
		pDEF += willAdd;
	}
	public void lessPhysicDEF(double willLess) {
		pDEF -= willLess;
	}
	
	// mDEF
	public double getMagicDEF() {
		return mDEF;
	}
	public void setMagicDEF(double willSet) {
		mDEF = willSet;
	}
	public void addMagicDEF(double willAdd) {
		mDEF += willAdd;
	}
	public void lessMagicDEF(double willLess) {
		mDEF -= willLess;
	}
	
	// PE
	public double getPE() {
		return PE;
	}
	public void setPE(double willSet) {
		PE = willSet;
	}
	public void addPE(double willAdd) {
		PE += willAdd;
	}
	public void lessPE(double willLess) {
		PE -= willLess;
	}
	
	// MC
	public double getMC() {
		return MC;
	}
	public void setMC(double willSet) {
		MC = willSet;
	}
	public void addMC(double willAdd) {
		MC += willAdd;
	}
	public void lessMC(double willLess) {
		MC -= willLess;
	}
}
