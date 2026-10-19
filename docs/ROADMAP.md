# Development roadmap

## Foundation — 0.1.0-alpha

- [x] NeoForge 1.21.1 / Java 21 project
- [x] Chaos Caverns dimension definition using modified vanilla cave noise
- [x] Three vanilla cave-biome regions (dripstone, lush, deep dark)
- [x] Server-side enter, exit and status commands
- [x] One-time arrival outpost, corridor and sample treasure vault
- [ ] End-to-end in-game validation

## Expedition implementation — current 0.2 development

The current source includes early implementation work for:

- [x] Persistent expedition ID allocation and route planning
- [x] Room construction and five selectable tiers with room-scoped encounters
- [x] Final guardian and completion reward logic
- [x] Delver's Compass and server configuration

These items are present in source, but **are not validated or released**. The GitHub Actions build for the 0.2 implementation commit [failed](https://github.com/Mikaaah/Duskdelvers/actions/runs/39238546032). Next gate: fix the build, then test the complete loop in-game before changing release/version status.

## Remaining validation and polish

- [ ] Pass a Gradle build on the current source
- [ ] Load on Minecraft 1.21.1 / NeoForge 21.1.x
- [ ] Verify repeatable expeditions, room bounds and allocation across restarts
- [ ] Test room activation, encounter caps, guardian completion and reward behavior
- [ ] Test exit/origin restoration, death and reconnect cases
- [ ] Validate dedicated-server and multiplayer behavior
- [ ] Review performance and save cleanup/reset safety

## Optional integrations

Optional integrations remain a separate milestone and must not become required dependencies:

- TNK Unidentified Loot, Apotheosis, Epic Fight and Iron's Spells
- Alex's Caves and Cataclysm, then Twilight Forest and Aether
- Create, Ars Nouveau, FTB Teams / Quests and Puffish Skills

## Release criteria

- Green Gradle build for the exact release commit
- Successful dedicated-server start and dimension registry resolution
- Verified entry/exit, room generation and rewards in a test world
- Restart, death and missing-optional-mod checks
- No known duplication or critical progression blockers
