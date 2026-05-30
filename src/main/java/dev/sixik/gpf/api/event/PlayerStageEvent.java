package dev.sixik.gpf.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.Objects;
import java.util.UUID;

/**
 * Base event for a single stage mutation on a player.
 */
public abstract class PlayerStageEvent extends Event {

    private final ServerPlayer player;
    private final short stageId;
    private final String stageName;

    protected PlayerStageEvent(ServerPlayer player, short stageId, String stageName) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.stageId = stageId;
        this.stageName = Objects.requireNonNull(stageName, "Stage name cannot be null");
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
}
