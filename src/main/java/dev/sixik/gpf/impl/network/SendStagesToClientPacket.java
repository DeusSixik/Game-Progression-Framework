package dev.sixik.gpf.impl.network;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SendStagesToClientPacket(List<StageEntry> stages, short[] activeStageIds) implements CustomPacketPayload {

    public static final Type<SendStagesToClientPacket> TYPE = new Type<>(ResourceLocation.tryBuild(GameProgressionFramework.MODID, "stage_registry_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SendStagesToClientPacket> STREAM_CODEC = StreamCodec.of(
            SendStagesToClientPacket::encode,
            SendStagesToClientPacket::decode
    );

    private static SendStagesToClientPacket decode(RegistryFriendlyByteBuf buffer) {
        int stageCount = buffer.readVarInt();
        List<StageEntry> stages = new ArrayList<>(stageCount);

        for (int i = 0; i < stageCount; i++) {
            stages.add(new StageEntry(buffer.readShort(), buffer.readUtf()));
        }

        int activeCount = buffer.readVarInt();
        short[] activeStageIds = new short[activeCount];
        for (int i = 0; i < activeCount; i++) {
            activeStageIds[i] = buffer.readShort();
        }

        return new SendStagesToClientPacket(List.copyOf(stages), activeStageIds);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SendStagesToClientPacket packet) {
        buffer.writeVarInt(packet.stages.size());
        for (StageEntry stage : packet.stages) {
            buffer.writeShort(stage.stageId());
            buffer.writeUtf(stage.stageName());
        }

        buffer.writeVarInt(packet.activeStageIds.length);
        for (short activeStageId : packet.activeStageIds) {
            buffer.writeShort(activeStageId);
        }
    }

    public static void handle(SendStagesToClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> StagesRegistry.INSTANCE.applySyncedStages(packet.stages, packet.activeStageIds));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record StageEntry(short stageId, String stageName) {
    }
}
