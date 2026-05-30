package dev.sixik.gpf.impl.compat.crafttweaker.events;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.event.ZenEvent;
import com.blamejared.crafttweaker.api.event.bus.IEventBus;
import com.blamejared.crafttweaker.api.event.bus.NeoForgeEventBusWire;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import dev.sixik.gpf.api.event.StageRegisterEndEvent;

@ZenRegister
@ZenEvent
@NativeTypeRegistration(value = StageRegisterEndEvent.class, zenCodeName = "mods.gpf.api.events.StageRegisterEndEvent")
public class GPFStageRegisterEndEvent {

    @ZenEvent.Bus
    public static final IEventBus<StageRegisterEndEvent> BUS = IEventBus.direct(
            StageRegisterEndEvent.class,
            NeoForgeEventBusWire.of()
    );
}
