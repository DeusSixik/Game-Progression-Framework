package dev.sixik.gpf.impl.compat.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import dev.sixik.gpf.api.event.StageRegisterEndEvent;
import dev.sixik.gpf.api.event.StageRegisterEvent;
import dev.sixik.gpf.impl.compat.kubejs.events.GPFKubeJSEvents;
import dev.sixik.gpf.impl.compat.kubejs.events.KubeStageRegisterEndEvent;
import dev.sixik.gpf.impl.compat.kubejs.events.KubeStageRegisterEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class GPFKubeJSEventBridge {

    @SubscribeEvent
    public void onStageRegister(StageRegisterEvent event) {
        GPFKubeJSEvents.STAGE_REGISTER.post(ScriptType.SERVER, new KubeStageRegisterEvent(event));
    }

    @SubscribeEvent
    public void onStageRegisterEnd(StageRegisterEndEvent event) {
        GPFKubeJSEvents.STAGE_REGISTER_END.post(ScriptType.SERVER, new KubeStageRegisterEndEvent(event));
    }
}
