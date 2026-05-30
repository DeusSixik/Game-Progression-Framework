package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fired after a player loses a single stage.
 */
public final class PlayerStageRemovedEvent extends PlayerStageEvent {

    /**
     * Creates a new single-stage remove event.
     *
     * @param playerId the affected player id
     * @param player the affected online player, or {@code null} when offline
     * @param stageId the removed stage id
     * @param stageName the removed stage name
     */
    public PlayerStageRemovedEvent(UUID playerId, @Nullable ServerPlayer player, short stageId, String stageName) {
        super(playerId, player, stageId, stageName);
    }
}
