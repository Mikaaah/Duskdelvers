# Duskdelvers development roadmap

## 0.1.0-alpha — Foundation
- [x] NeoForge 1.21.1 / Java 21 project, Chaos Caverns and fixed test outpost.

## 0.2.0 — Expeditions feature test
- [x] Connected, seeded 5–9 room dungeon generation with tier-specific palettes.
- [x] Per-world persistent IDs, isolated dungeon-region allocation.
- [x] Five difficulty tiers; enemy pools, elites and final guardian.
- [x] Repeatable loot caches and XP/guardian reward.
- [x] Reusable compass and server-side configuration.
- [x] Datapack-driven encounter registry with optional-mod fallback.
- [x] Official branding and CI build.
- [ ] Player game-testing and multiplayer validation.

## 0.2.1 — Player-built tier gateways
- [x] Five player-crafted 4×5 portal frame materials and distinct portal states.
- [x] Compass activation with structural validation and frame breaking detection.
- [x] Return gateway in entry room and extraction portal on guardian defeat.
- [x] Portal travel cooldown, safe-return attempt and command debug validation.
- [ ] In-game client rendering and survival construction testing.

## 0.2.2 — Timed expeditions
- [x] Configurable timer for all five tiers plus original outpost.
- [x] Action bar countdown, warning thresholds and automatic safe extraction.
- [x] Boss-clear looting grace; no inventory loss; persistence across logout.
- [x] In-game time and server time-limits commands.
- [ ] Verify client HUD and real multiplayer expiry behavior during gameplay.

## 0.3.0-alpha — Team expeditions and optional mod content
- [x] Dimension-only vanilla inventory/XP retention upon death.
- [x] Persistent daily personal entry limits.
- [x] Native parties and opt-in FTB Teams matchmaking.
- [x] Shared SavedData sessions, countdowns and encounter progress.
- [x] Server reward claims, TNK Loot reward API, optional FTB Quest and Puffish XP command bridging.
- [x] Conditional mob pools for Iron's Spells, Alex's Caves, Cataclysm, Aether and Twilight Forest.
- [x] Create/Ars optional room accents.
- [ ] Full TNK3 modpack player tests and vendor API edge cases.
- [ ] Boss-native AI/Epic Fight patches, bespoke battle arenas and interactive mechanics.

## 0.4.x — Caverns & dungeon expansion
- [ ] True natural jigsaw structure generation, new modular rooms and custom dungeon features.
- [ ] Custom cave biomes and fluid/terrain polishing.
- [ ] Special puzzles, traps, discoveries, minimap/journal.
- [ ] Unique animated bosses and encounter objectives.

## 0.4.x — Integration & multiplayer
- [ ] Unidentified Loot + Apotheosis rewards.
- [ ] Epic Fight + Iron's Spells combat compatibility.
- [ ] Alex's Caves, Cataclysm, Twilight Forest, Aether selected encounter pools.
- [ ] Create and Ars Nouveau puzzle variants.
- [ ] FTB Teams / Quests and Puffish Skills completion integration.
- [ ] Safe party instances, expiring/recycling regions and automatic reset.
