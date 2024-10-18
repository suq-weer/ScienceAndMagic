package top.xiaosuoaa.scienceandmagic.nature.resource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

import java.util.List;
import java.util.function.Predicate;

public class SequoiaChestBoatItem extends Item {
	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

	public SequoiaChestBoatItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		HitResult hitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.ANY);
		if (hitresult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			Vec3 vec3 = pPlayer.getViewVector(1.0F);
			List<Entity> list = pLevel.getEntities(pPlayer, pPlayer.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
			if (!list.isEmpty()) {
				Vec3 vec31 = pPlayer.getEyePosition();

				for (Entity entity : list) {
					AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
					if (aabb.contains(vec31)) {
						return InteractionResultHolder.pass(itemstack);
					}
				}
			}

			if (hitresult.getType() == HitResult.Type.BLOCK) {
				SequoiaChestBoat boat = this.getBoat(pLevel, hitresult, itemstack, pPlayer);
				boat.setYRot(pPlayer.getYRot());
				if (!pLevel.noCollision(boat, boat.getBoundingBox())) {
					return InteractionResultHolder.fail(itemstack);
				} else {
					if (!pLevel.isClientSide) {
						pLevel.addFreshEntity(boat);
						pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, hitresult.getLocation());
						itemstack.consume(1, pPlayer);
					}

					pPlayer.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
				}
			} else {
				return InteractionResultHolder.pass(itemstack);
			}
		}
	}

	private SequoiaChestBoat getBoat(Level pLevel, HitResult pHitResult, ItemStack pStack, Player pPlayer) {
		Vec3 vec3 = pHitResult.getLocation();
		SequoiaChestBoat boat = new SequoiaChestBoat(NeoModRegister.SEQUOIA_CHEST_BOAT.get(), pLevel);
		boat.setPos(vec3.x, vec3.y, vec3.z);
		if (pLevel instanceof ServerLevel serverlevel) {
			EntityType.<SequoiaChestBoat>createDefaultStackConfig(serverlevel, pStack, pPlayer).accept(boat);
		}

		return boat;
	}
}
