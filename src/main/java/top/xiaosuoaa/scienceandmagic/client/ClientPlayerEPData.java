package top.xiaosuoaa.scienceandmagic.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerEPData {
    private static int playerThirst;

    public static void set(int thirst) {
        ClientPlayerEPData.playerThirst = thirst;
    }

    public static int getPlayerThirst() {
        return playerThirst;
    }
}
