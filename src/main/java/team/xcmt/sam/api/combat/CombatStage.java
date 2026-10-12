package team.xcmt.sam.api.combat;

/**
 * 伤害 / 防御结算的阶段，与机制文档中的「伤害 7 步」「防御 8 步」一一对应。
 * <p>
 * 武器 / 魂骨 / 装备 / 元素附着四类贡献者通过这些阶段插入自己的附加量，
 * 目前除元素附着（核心内置）外均为空实现，供后续填充。
 */
public enum CombatStage {

    // ---- 伤害 7 步 ----
    /** 1. 玩家属性计算最终伤害。 */
    ATTACK_PLAYER(true),
    /** 2. 武器附加伤害。 */
    ATTACK_WEAPON(true),
    /** 3. 魂骨附加伤害。 */
    ATTACK_SOUL_BONE(true),
    /** 4. 装备附加伤害。 */
    ATTACK_EQUIPMENT(true),
    /** 5. 元素附着加成。 */
    ATTACK_ELEMENT(true),
    /** 6. 原版暴击 / 原版生物效果。 */
    ATTACK_VANILLA(true),
    /** 7. 发出伤害。 */
    ATTACK_EMIT(true),

    // ---- 防御 8 步 ----
    /** 1. 接收伤害。 */
    DEFENSE_RECEIVE(false),
    /** 2. 玩家防御属性计算。 */
    DEFENSE_PLAYER(false),
    /** 3. 武器防御加成。 */
    DEFENSE_WEAPON(false),
    /** 4. 装备防御加成。 */
    DEFENSE_EQUIPMENT(false),
    /** 5. 魂骨防御加成。 */
    DEFENSE_SOUL_BONE(false),
    /** 6. 元素附着加成 / 减益。 */
    DEFENSE_ELEMENT(false),
    /** 7. 原版效果加成 / 减益。 */
    DEFENSE_VANILLA(false),
    /** 8. 最终受到伤害。 */
    DEFENSE_FINAL(false);

    private final boolean attack;

    CombatStage(boolean attack) {
        this.attack = attack;
    }

    /** 是否为伤害流程的阶段。 */
    public boolean isAttack() {
        return this.attack;
    }
}
