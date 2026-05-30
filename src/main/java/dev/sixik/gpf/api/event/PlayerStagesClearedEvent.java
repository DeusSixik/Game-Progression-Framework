package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired after all stages are removed from a player.
 */
public final class PlayerStagesClearedEvent extends Event {

    private final ServerPlayer player;
    private final StageData previousStages;

    public PlayerStagesClearedEvent(ServerPlayer player, StageData previousStages) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.previousStages = Objects.requireNonNull(previousStages, "Previous stages cannot be null");
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
     * @return the player's stages before they were cleared
     */
    public StageData getPreviousStages() {
        return previousStages;
    }
}
