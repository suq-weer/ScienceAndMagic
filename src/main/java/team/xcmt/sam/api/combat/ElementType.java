package team.xcmt.sam.api.combat;

import javax.annotation.Nullable;

import net.minecraft.util.StringRepresentable;

import org.jspecify.annotations.NonNull;

/**
 * 六种元素附着。
 * <p>
 * 注册名与贴图一一对应（{@code element_fire} → {@code textures/mob_effect/element_fire.png}），
 * 中文名复用 {@code tooltip.science_and_magic.element_<id>_short}。
 * 实现 {@link StringRepresentable} 以便作为数据组件序列化。
 */
public enum ElementType implements StringRepresentable {

    FIRE("fire"),
    ICE("ice"),
    WOOD("wood"),
    WATER("water"),
    LIGHTING("lighting"),
    STONE("stone");

    private final String id;
    private final String effectPath;
    private final String shortNameKey;

    ElementType(String id) {
        this.id = id;
        this.effectPath = "element_" + id;
        this.shortNameKey = "tooltip.science_and_magic.element_" + id + "_short";
    }

    /** 元素短名。 */
    public String id() {
        return this.id;
    }

    /** 对应 MobEffect 的注册路径。 */
    public String effectPath() {
        return this.effectPath;
    }

    /** 对应 MobEffect 的中文名键。 */
    public String effectTranslationKey() {
        return "effect.science_and_magic." + this.effectPath;
    }

    /** 元素短名翻译键。 */
    public String shortNameKey() {
        return this.shortNameKey;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.id;
    }

    /** 按短名反查，未命中返回 {@code null}。 */
    @Nullable
    public static ElementType byId(String id) {
        for (ElementType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
