package dev.sixik.gpf.impl.compat.kubejs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.sixik.gpf.api.event.StageRegisterEvent;

public final class KubeStageRegisterEvent implements KubeEvent {

    private final StageRegisterEvent internal;

    public KubeStageRegisterEvent(StageRegisterEvent internal) {
        this.internal = internal;
    }

    @Info("Registers a stage name in the progression registry.")
    public void registerStage(String stageName) {
        internal.registerStage(stageName);
    }

    @Info("Registers a stage name in the progression registry.")
    public void register(String stageName) {
        internal.registerStage(stageName);
    }
}
