package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Base event for a single stage mutation on a player.
 */
public abstract class PlayerStageEvent extends Event {

    private final UUID playerId;
    @Nullable
    private final ServerPlayer player;
    private final short stageId;
    private final String stageName;

    protected PlayerStageEvent(UUID playerId, @Nullable ServerPlayer player, short stageId, String stageName) {
        this.playerId = Objects.requireNonNull(playerId, "Player id cannot be null");
        this.player = player;
        if (player != null && !playerId.equals(player.getUUID())) {
            throw new IllegalArgumentException("Player instance does not match provided player id");
        }

        this.stageId = stageId;
        this.stageName = Objects.requireNonNull(stageName, "Stage name cannot be null");
    }

    /**
     * @return the affected online player, or {@code null} when the owner is offline
     */
    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * @return the unique identifier of the affected player
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * @return the stage id that changed
     */
    public short getStageId() {
        return stageId;
    }

    /**
     * @return the normalized stage name that changed
     */
    public String getStageName() {
        return stageName;
    }

    /**
     * @return {@code true} when the owner is currently online
     */
    public boolean isPlayerOnline() {
        return player != null;
    }
}
