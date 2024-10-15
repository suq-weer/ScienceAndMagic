package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ElementFunctions {
	/**
	 * 检测水、火、冰元素附着之间的反应。
	 *
	 * @return {@link Integer}：
	 *         <p><code>0</code>为火克冰；
	 *         <p><code>1</code>冰克水；
	 *         <p><code>2</code>为水克火；
	 *         <p><code>-1</code>则没有上述情况。
	 */
	public static int scanElementRestrainFIW(MobEffect effect1, MobEffect effect2) {
		if (effect1 instanceof FireElementMobEffect && effect2 instanceof IceElementMobEffect) {
			return 0;
		}
		if (effect1 instanceof IceElementMobEffect && effect2 instanceof WaterElementMobEffect) {
			return 1;
		}
		if (effect1 instanceof WaterElementMobEffect && effect2 instanceof FireElementMobEffect) {
			return 2;
		}
		return -1;
	}
	/**
	 * 检测木、石、电元素附着之间的反应。
	 *
	 * @return {@link Integer}：
	 *         <p><code>0</code>为木自克；
	 *         <p><code>1</code>为石自克；
	 *         <p><code>2</code>为电自克；
	 *         <p><code>3</code>为木、石互相减免；
	 *         <p><code>-1</code>则没有上述情况。</p>
	 */
	public static int scanElementRestrainWSL(MobEffect effect1, MobEffect effect2) {
		if (effect1 instanceof WoodElementMobEffect && effect2 instanceof WoodElementMobEffect) {
			return 0;
		}
		if (effect1 instanceof StoneElementMobEffect && effect2 instanceof StoneElementMobEffect) {
			return 1;
		}
		if (effect1 instanceof LightingElementMobEffect && effect2 instanceof LightingElementMobEffect) {
			return 2;
		}
		if ((effect1 instanceof StoneElementMobEffect && effect2 instanceof WoodElementMobEffect) || (effect1 instanceof WoodElementMobEffect && effect2 instanceof StoneElementMobEffect)) {
			return 3;
		}
		return -1;
	}

	/**
	 * 元素附着生物效果检测。
	 * @param event 即{@link MobEffectEvent.Added}事件。
	 */
	public static void scanElementEffect(MobEffectEvent.Added event) {
		MobEffectInstance willAddEffect = event.getEffectInstance();
		LivingEntity eventEntity = event.getEntity();
		Collection<MobEffectInstance> activeEffects = eventEntity.getActiveEffects();
		for (int i = 0; i < activeEffects.size(); i++) {
			MobEffectInstance effectInstance = (MobEffectInstance) activeEffects.toArray()[i];
			if (willAddEffect != null && ElementFunctions.scanElementRestrainFIW(willAddEffect.getEffect().value(), effectInstance.getEffect().value()) != -1) {
				ScienceAndMagic.info("俘获到元素附着生物效果，进行反应……");
				eventEntity.removeEffect(effectInstance.getEffect());
			}
		}
	}

	public static int scanElementComponent(DataComponentMap componentMap) {
		if (componentMap.has(NeoModRegister.ELEMENT_COMPONENT.get())) {
			List<String> record = Objects.requireNonNull(componentMap.get(NeoModRegister.ELEMENT_COMPONENT.get())).element();

		}
		return -1;
	}

	//TODO：元素附着伤害检测。
	public static void itemGiveElement(ItemStack itemStack, LivingEntity hurtEntity) {
		DataComponentMap componentMap = itemStack.getComponents();
		int result = scanElementComponent(componentMap);
	}
}
