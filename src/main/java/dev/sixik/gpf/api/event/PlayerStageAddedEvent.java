package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fired after a player gains a single stage.
 */
public final class PlayerStageAddedEvent extends PlayerStageEvent {

    /**
     * Creates a new single-stage add event.
     *
     * @param playerId the affected player id
     * @param player the affected online player, or {@code null} when offline
     * @param stageId the added stage id
     * @param stageName the added stage name
     */
    public PlayerStageAddedEvent(UUID playerId, @Nullable ServerPlayer player, short stageId, String stageName) {
        super(playerId, player, stageId, stageName);
    }
}
