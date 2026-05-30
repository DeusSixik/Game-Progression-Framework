# Game Progression Framework

Game Progression Framework (GPF) is a NeoForge 1.21.1 library for deterministic, high-performance player progression.

It is designed for large modpacks and progression-heavy content where many systems need to hide, unlock, or gate content by stage without turning every check into expensive server-side logic.

## Goals

- Deterministic stage ids that never reshuffle between reloads.
- Fast runtime checks for player progression.
- UUID-first player API, with `ServerPlayer` overloads as convenience wrappers.
- Script integration for CraftTweaker and KubeJS.
- A clean split between:
  - stage name registration
  - finalized stage id usage

## Where to find the API

Main public API packages:

- `src/main/java/dev/sixik/gpf/api/Stages.java`
  - Main entry point for stage lookups and player stage operations.
  - Use this for `getStageId`, `getStageName`, `hasStage`, `addStage`, `removeStage`, `snapshot`, and more.
- `src/main/java/dev/sixik/gpf/api/StageData.java`
  - Mutable stage container interface used for snapshots, merging, replacing, and raw bit data access.
- `src/main/java/dev/sixik/gpf/api/progression/PlayerProgression.java`
  - Per-player progression facade.
  - Works with `UUID` first and can optionally expose the online `ServerPlayer`.
- `src/main/java/dev/sixik/gpf/api/event/`
  - Public NeoForge events such as `StageRegisterEvent`, `StageRegisterEndEvent`, and player stage mutation events.
- `src/main/java/dev/sixik/gpf/api/script/StageScriptApi.java`
  - Script-facing registry helper used during stage registration.

Script compat layers:

- CraftTweaker wrappers:
  - `src/main/java/dev/sixik/gpf/impl/compat/crafttweaker/`
  - `src/main/java/dev/sixik/gpf/impl/compat/crafttweaker/events/`
- KubeJS wrappers:
  - `src/main/java/dev/sixik/gpf/impl/compat/kubejs/`
  - `src/main/java/dev/sixik/gpf/impl/compat/kubejs/events/`

## Registration lifecycle

GPF uses a two-step registration lifecycle.

### 1. `StageRegisterEvent`

Use `StageRegisterEvent` only to register stage names.

At this point you should only declare stages such as:

- `stage_one`
- `stage_two`
- `my_mod:advanced_progress`

Do not build dependent logic here.
Do not resolve ids here.
Do not register child module restrictions here.

### 2. `StageRegisterEndEvent`

Use `StageRegisterEndEvent` after the registry has been finalized.

At this point:

- all stage names are collected
- persistent ids are assigned
- ids are stable and safe to query
- dependent systems can now bind to those ids

## Important rule

All child modules that depend on finalized stage ids must be registered in `StageRegisterEndEvent`.

That includes systems such as:

- `RecipeMachineStages`
- recipe restrictions
- research bindings
- visibility filters
- any module that stores or uses stage ids internally

In short:

- `StageRegisterEvent` = declare stage names
- `StageRegisterEndEvent` = build everything that depends on those stage ids

This rule prevents race conditions, avoids querying unfinished ids, and keeps the registry deterministic across reloads and world restarts.

## Stage naming rules

Valid stage names use lowercase characters and follow this format:

- lowercase letters
- digits
- `_`
- `-`
- `.`
- `/`
- optional namespace, for example `my_mod:stage_name`

Examples:

- `bronze_age`
- `machines/tier_2`
- `my_mod:research/automation`

## Java usage

### Resolve ids

```java
short bronzeAge = Stages.getStageId("bronze_age");
String name = Stages.getStageName(bronzeAge);
```

### Work with player progression by UUID

```java
UUID playerId = player.getUUID();
short stageId = Stages.getStageId("bronze_age");

if (!Stages.hasStage(playerId, stageId)) {
    Stages.addStage(playerId, stageId);
}
```

### Progression facade

```java
PlayerProgression progression = Stages.player(player.getUUID());

if (progression.hasStage("bronze_age")) {
    // unlock content
}
```

### Snapshot data

```java
StageData snapshot = Stages.snapshot(player.getUUID());
long[] raw = snapshot.toRawDataCopy();
```

## CraftTweaker scripting

Example script location:

- `src/resources/examples/crafttweaker/test.zs`

### Register stages

```zenscript
import mods.gpf.api.events.StageRegisterEvent;
import mods.gpf.api.events.StageRegisterEndEvent;
import mods.gpf.api.Stages;

events.register<mods.gpf.api.events.StageRegisterEvent>(event => {
    event.registerStage("stage_one");
});

events.register<mods.gpf.api.events.StageRegisterEndEvent>(event => {

    if(event.isKnown('stage_one')) {
        var stage_one_id = Stages.getStageId("stage_one");
        // Some code...
    }
});
```

### CraftTweaker rules

- Register stage names in `StageRegisterEvent`.
- Query stage ids only in `StageRegisterEndEvent`.
- Register child modules only in `StageRegisterEndEvent`.
- If a system stores stage ids internally, never initialize it during `StageRegisterEvent`.

## KubeJS scripting

Example script location:

- `src/resources/examples/kubejs/main.js`

### Register stages

```js
GPFEvents.stageRegister(event => {
    event.register('stage_one')
    event.register('stage_two')
})
```

### Use finalized ids

```js
GPFEvents.stageRegisterEnd(event => {
    if (event.isKnown('stage_one')) {
        const stageOneId = GPFStages.getStageId('stage_one')
        // Some code...
    }
})
```

### KubeJS rules

- `GPFEvents.stageRegister(...)` is only for declaring stage names.
- `GPFEvents.stageRegisterEnd(...)` is the correct place for dependent registrations.
- Use `GPFStages.getStageId(...)` only after the registry is finalized.
- Modules like `RecipeMachineStages` must hook their restrictions in `stageRegisterEnd`.

## Recommended integration pattern

If you are writing a GPF-powered addon or child module, follow this pattern:

1. Register every stage name in `StageRegisterEvent`.
2. Wait for `StageRegisterEndEvent`.
3. Resolve finalized stage ids.
4. Register restrictions, filters, recipe locks, research links, or machine rules.

This keeps the whole stack deterministic and prevents stored player data from breaking when the stage list grows over time.

## Notes about persistence and performance

- Player stage operations are designed around cached per-player data.
- Persistent storage remains internal to the framework.
- Public API no longer requires `ServerPlayer` and can work with only `UUID`.
- `ServerPlayer` overloads still exist for convenience when you already have an online player.

## Summary

Use GPF as a staged pipeline:

- define names first
- finalize registry
- bind ids into child systems
- query and mutate progression through the public API

If you follow the `StageRegisterEvent` -> `StageRegisterEndEvent` split strictly, stage ids remain predictable and safe for long-term saved data.
