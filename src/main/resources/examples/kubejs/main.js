GPFEvents.stageRegister(event => {
    event.register('stage_one')
    event.register('stage_two')
})

GPFEvents.stageRegisterEnd(event => {
    if(event.isKnown('stage_one')) {
        let stage_one_id = GPFStages.getStageId('stage_one');
    }
})