package dev.sixik.gpf.impl.network;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.impl.client.ClientStageData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SendPlayerStagesToClientPacket(UUID ownerId, long[] rawStages) implements CustomPacketPayload {

    public static final Type<SendPlayerStagesToClientPacket> TYPE = new Type<>(ResourceLocation.tryBuild(GameProgressionFramework.MODID, "player_stages_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SendPlayerStagesToClientPacket> STREAM_CODEC = StreamCodec.of(
            SendPlayerStagesToClientPacket::encode,
            SendPlayerStagesToClientPacket::decode
    );

    private static SendPlayerStagesToClientPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SendPlayerStagesToClientPacket(buffer.readUUID(), buffer.readLongArray());
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SendPlayerStagesToClientPacket packet) {
        buffer.writeUUID(packet.ownerId);
        buffer.writeLongArray(packet.rawStages);
    }

    public static void handle(SendPlayerStagesToClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientStageData.applySync(packet.ownerId, packet.rawStages));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
