import mods.gpf.api.events.StageRegisterEvent;
import mods.gpf.api.events.StageRegisterEndEvent;
import mods.gpf.api.Stages;

events.register<mods.gpf.api.events.StageRegisterEvent>(event => {
    event.registerStage("stage_one");
});

events.register<mods.gpf.api.events.StageRegisterEndEvent>(event => {

    if(event.isKnown('stage_one')) {
        var stage_one_id = GPFStages.getStageId("stage_one");
    }
});