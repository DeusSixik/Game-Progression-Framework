package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.impl.network.SendPlayerStagesToClientPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PlayerStageDataService {

    private PlayerStageDataService() {
    }

    public static StageData getOrCreate(ServerPlayer player) {
        return getStorage(player.server).getOrCreate(player.getUUID());
    }

    public static boolean addStage(ServerPlayer player, short stageId) {
        boolean changed = getStorage(player.server).addStage(player.getUUID(), stageId);
        if (changed) {
            sync(player);
        }

        return changed;
    }

    public static boolean removeStage(ServerPlayer player, short stageId) {
        boolean changed = getStorage(player.server).removeStage(player.getUUID(), stageId);
        if (changed) {
            sync(player);
        }

        return changed;
    }

    public static void replace(ServerPlayer player, long[] rawStages) {
        getStorage(player.server).replace(player.getUUID(), rawStages);
        sync(player);
    }

    public static SendPlayerStagesToClientPacket createSyncPacket(ServerPlayer player) {
        StageData snapshot = getStorage(player.server).copyForSync(player.getUUID());
        return new SendPlayerStagesToClientPacket(snapshot.getOwnerId(), snapshot.toRawDataCopy());
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, createSyncPacket(player));
    }

    private static PlayerStageDataSavedData getStorage(MinecraftServer server) {
        return PlayerStageDataSavedData.get(server);
    }
}
