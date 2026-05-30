package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired after a player's progression is updated by a batch operation such as
 * replace, set, or merge.
 */
public final class PlayerStagesChangedEvent extends Event {

    private final UUID playerId;
    @Nullable
    private final ServerPlayer player;
    private final StageData previousStages;
    private final StageData currentStages;
    private final short[] addedStageIds;
    private final short[] removedStageIds;

    /**
     * Creates a new batch change event.
     *
     * @param playerId the affected player id
     * @param player the affected online player, or {@code null} when offline
     * @param previousStages the snapshot before the change
     * @param currentStages the snapshot after the change
     * @param addedStageIds stage ids that were added by this change
     * @param removedStageIds stage ids that were removed by this change
     */
    public PlayerStagesChangedEvent(
            UUID playerId,
            @Nullable ServerPlayer player,
            StageData previousStages,
            StageData currentStages,
            short[] addedStageIds,
            short[] removedStageIds
    ) {
        this.playerId = Objects.requireNonNull(playerId, "Player id cannot be null");
        this.player = player;
        this.previousStages = Objects.requireNonNull(previousStages, "Previous stages cannot be null");
        this.currentStages = Objects.requireNonNull(currentStages, "Current stages cannot be null");
        this.addedStageIds = Objects.requireNonNull(addedStageIds, "Added stage ids cannot be null").clone();
        this.removedStageIds = Objects.requireNonNull(removedStageIds, "Removed stage ids cannot be null").clone();
        if (!playerId.equals(previousStages.getOwnerId()) || !playerId.equals(currentStages.getOwnerId())) {
            throw new IllegalArgumentException("Stage snapshots owner does not match provided player id");
        }
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

    /**
     * @return {@code true} when the owner is currently online
     */
    public boolean isPlayerOnline() {
        return player != null;
    }
}
