package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired after a player's progression is updated by a batch operation such as
 * replace, set, or merge.
 */
public final class PlayerStagesChangedEvent extends Event {

    private final ServerPlayer player;
    private final StageData previousStages;
    private final StageData currentStages;
    private final short[] addedStageIds;
    private final short[] removedStageIds;

    /**
     * Creates a new batch change event.
     *
     * @param player the affected player
     * @param previousStages the snapshot before the change
     * @param currentStages the snapshot after the change
     * @param addedStageIds stage ids that were added by this change
     * @param removedStageIds stage ids that were removed by this change
     */
    public PlayerStagesChangedEvent(
            ServerPlayer player,
            StageData previousStages,
            StageData currentStages,
            short[] addedStageIds,
            short[] removedStageIds
    ) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.previousStages = Objects.requireNonNull(previousStages, "Previous stages cannot be null");
        this.currentStages = Objects.requireNonNull(currentStages, "Current stages cannot be null");
        this.addedStageIds = Objects.requireNonNull(addedStageIds, "Added stage ids cannot be null").clone();
        this.removedStageIds = Objects.requireNonNull(removedStageIds, "Removed stage ids cannot be null").clone();
    }

    /**
     * @return the affected player
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * @return the unique identifier of the affected player
     */
    public UUID getPlayerId() {
        return player.getUUID();
    }

    /**
     * @return a snapshot of the player's stages before the batch update
     */
    public StageData getPreviousStages() {
        return previousStages;
    }

    /**
     * @return a snapshot of the player's stages after the batch update
     */
    public StageData getCurrentStages() {
        return currentStages;
    }

    /**
     * @return a copy of stage ids added by this change
     */
    public short[] getAddedStageIds() {
        return addedStageIds.clone();
    }

    /**
     * @return a copy of stage ids removed by this change
     */
    public short[] getRemovedStageIds() {
        return removedStageIds.clone();
    }
}
