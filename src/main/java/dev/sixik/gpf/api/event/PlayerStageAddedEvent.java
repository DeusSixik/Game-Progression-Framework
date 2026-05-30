package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;

/**
 * Fired after a player gains a single stage.
 */
public final class PlayerStageAddedEvent extends PlayerStageEvent {

    /**
     * Creates a new single-stage add event.
     *
     * @param player the affected player
     * @param stageId the added stage id
     * @param stageName the added stage name
     */
    public PlayerStageAddedEvent(ServerPlayer player, short stageId, String stageName) {
        super(player, stageId, stageName);
    }
}
