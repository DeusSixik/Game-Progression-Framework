package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired after all stages are removed from a player.
 */
public final class PlayerStagesClearedEvent extends Event {

    private final UUID playerId;
    @Nullable
    private final ServerPlayer player;
    private final StageData previousStages;

    /**
     * Creates a new clear event.
     *
     * @param playerId the affected player id
     * @param player the affected online player, or {@code null} when offline
     * @param previousStages the stage snapshot before clearing
     */
    public PlayerStagesClearedEvent(UUID playerId, @Nullable ServerPlayer player, StageData previousStages) {
        this.playerId = Objects.requireNonNull(playerId, "Player id cannot be null");
        this.player = player;
        this.previousStages = Objects.requireNonNull(previousStages, "Previous stages cannot be null");
        if (!playerId.equals(previousStages.getOwnerId())) {
            throw new IllegalArgumentException("Previous stages owner does not match provided player id");
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
     * @return the player's stages before they were cleared
     */
    public StageData getPreviousStages() {
        return previousStages;
    }

    /**
     * @return {@code true} when the owner is currently online
     */
    public boolean isPlayerOnline() {
        return player != null;
    }
}
