package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;

/**
 * Fired after a player loses a single stage.
 */
public final class PlayerStageRemovedEvent extends PlayerStageEvent {

    /**
     * Creates a new single-stage remove event.
     *
     * @param player the affected player
     * @param stageId the removed stage id
     * @param stageName the removed stage name
     */
    public PlayerStageRemovedEvent(ServerPlayer player, short stageId, String stageName) {
        super(player, stageId, stageName);
    }
}
