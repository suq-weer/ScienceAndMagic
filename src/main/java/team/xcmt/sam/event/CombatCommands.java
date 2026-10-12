package team.xcmt.sam.event;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.ElementType;
import team.xcmt.sam.content.ElementEffects;
import team.xcmt.sam.util.CombatDebug;

/**
 * 测试用调试命令 {@code /samcombat}（需要游戏管理员权限）。
 * <ul>
 *     <li>{@code /samcombat debug <true|false>}：开关 {@code [SAM-COMBAT]} 侦测日志；</li>
 *     <li>{@code /samcombat element <元素> <强度> [目标]}：给目标附着元素（不给目标时作用于自己）。
 *         走本模组的叠加规则，便于验证强度叠加、3 秒衰减与克制降级。</li>
 * </ul>
 * 元素列表：fire / ice / wood / water / lighting / stone。
 */
@EventBusSubscriber(modid = ScienceAndMagic.MOD_ID)
public final class CombatCommands {

    private static final SuggestionProvider<CommandSourceStack> ELEMENTS = (context, builder) -> {
        for (ElementType type : ElementType.values()) {
            builder.suggest(type.id());
        }
        return builder.buildFuture();
    };

    private CombatCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("samcombat")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("debug")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    CombatDebug.setEnabled(enabled);
                                    context.getSource().sendSuccess(() -> Component.literal(
                                            "[SAM-COMBAT] 侦测日志已" + (enabled ? "开启" : "关闭")),
                                            false);
                                    return 1;
                                })))
                .then(Commands.literal("element")
                        .then(Commands.argument("element", StringArgumentType.word())
                                .suggests(ELEMENTS)
                                .then(Commands.argument("levels", IntegerArgumentType.integer(1, 256))
                                        .executes(context -> applyElement(context, false))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> applyElement(context, true)))))));
    }

    private static int applyElement(CommandContext<CommandSourceStack> context,
            boolean explicitTargets) throws CommandSyntaxException {
        ElementType type = ElementType.byId(StringArgumentType.getString(context, "element"));
        if (type == null) {
            context.getSource().sendFailure(
                    Component.literal("未知元素，可用: fire/ice/wood/water/lighting/stone"));
            return 0;
        }
        int levels = IntegerArgumentType.getInteger(context, "levels");
        Collection<? extends Entity> targets = explicitTargets
                ? EntityArgument.getEntities(context, "targets")
                : List.of(context.getSource().getPlayerOrException());

        int applied = 0;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity living) {
                ElementEffects.attach(living, type, levels);
                applied++;
            }
        }
        int total = applied;
        context.getSource().sendSuccess(() -> Component.literal(
                "[SAM-COMBAT] 已为 " + total + " 个实体附着 " + type.id() + " +" + levels + " 级"),
                false);
        return total;
    }
}
