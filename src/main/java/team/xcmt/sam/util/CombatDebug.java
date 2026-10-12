package team.xcmt.sam.util;

import team.xcmt.sam.ScienceAndMagic;

/**
 * 战斗调试日志开关。
 * <p>
 * 所有侦测日志都带统一前缀 {@value #PREFIX}，便于在日志里直接检索。
 * <b>测试期保持开启</b>；正式发布前调用 {@link #setEnabled(boolean)} 传入 {@code false}，
 * 即可让热路径退化成一次布尔判断。
 */
public final class CombatDebug {

    /** 日志前缀，方便筛选。 */
    public static final String PREFIX = "[SAM-COMBAT 系统] ";

    /** 总开关：测试期 true，发布前置 false。 */
    private static volatile boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /** 输出一条侦测日志（INFO 级，确保任何日志配置下都能看到）。 */
    public static void log(String format, Object... args) {
        if (!enabled) {
            return;
        }
        // 前缀是常量，先拼好再交给 SLF4J，避免在日志调用实参里出现字符串串联。
        String message = PREFIX + format;
        ScienceAndMagic.LOGGER.info(message, args);
    }
}
