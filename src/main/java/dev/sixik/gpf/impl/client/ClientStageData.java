package dev.sixik.gpf.impl.client;

import dev.sixik.gpf.data.BaseBitStageData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientStageData {

    private static final UUID EMPTY_OWNER = new UUID(0L, 0L);
    public static BaseBitStageData INSTANCE = new BaseBitStageData(EMPTY_OWNER);

    public static void applySync(UUID ownerId, long[] rawStages) {
        INSTANCE = new BaseBitStageData(rawStages, ownerId);
    }

    public static void clear() {
        INSTANCE = new BaseBitStageData(EMPTY_OWNER);
    }
}
