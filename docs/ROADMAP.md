# Development roadmap

## 0.1.0-alpha — Foundations
- [x] NeoForge 1.21.1 / Java 21 project
- [x] Chaos Caverns dimension definition using modified vanilla cave noise
- [x] Three vanilla cave-biome regions (dripstone, lush, deep dark)
- [x] Server-side enter, exit and status commands
- [x] One-time arrival outpost, corridor and sample treasure vault
- [ ] End-to-end in-game validation (requires a Minecraft runtime)

## 0.2.x — Procedural dungeons
- Datapack-defined rooms, connected jigsaw templates, structure sets
- Configurable treasure pools and room objectives
- Cave environment polish, hazards and dimension travel item

## 0.3.x — Progressive combat
- Five selectable tiers, encounter director and room budgets
- Elite variants, server-safe population caps, minibosses
- Dungeon victory and failure events

## 0.4.x — TNK integration
- Unidentified Loot, Apotheosis, Epic Fight, Iron's Spells
- Alex's Caves and Cataclysm, then Twilight Forest and Aether

## 0.5.x — Expeditions
- Create / Ars Nouveau puzzle integrations
- FTB Teams / Quests and Puffish Skills
- Instanced or region-reset expeditions after safe state handling

## Release criteria
- Green Gradle build, successful dedicated server start and dimension registry resolution
- Enter/exit with origin restoration, survive restart and death scenarios
- No missing-class crash when optional mods are absent
- Reproducible dungeon/loot tests without duplication
