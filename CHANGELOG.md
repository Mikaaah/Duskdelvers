# Changelog

## 0.4.0 — The Natural Caverns Overhaul

**Duskdelvers is now a cave exploration mod, not a procedural dungeon generator.**

### World generation
- Replaced the three-biome climate with **16 distinct cave biomes**, including Twilight Forest and Aether-inspired subterranean habitats.
- Introduced **five biome danger tiers**; encountered mobs are balanced by the biome players explore.
- Increased horizontal climate noise sampling from `0.25` to `0.82`, producing **smaller biome patches**.
- **Preserved the original huge-cave `final_density` noise unchanged**, so cavern size, shape, heights, and enclosed cave identity remain intact.
- Added tier-themed cave surface materials and scattered natural ground patches.
- Added optional, registry-checked Twilight Oak and Aether Holystone accents when their respective mods exist.

### Exploration and loot
- **Removed** generated rectangular dungeon rooms, corridors, starter outpost, and artificial guardian/boss arenas.
- **Removed** room-wave spawning, artificial dungeon clearing, and end-room mandatory boss reward.
- Added naturally occurring **cave-floor treasure caches** with five biome-tier loot tables, including basic minerals, rare treasure, and higher-tier valuables.
- Added sparse optional-mod wildlife roaming with a configurable population cap; naturally spawning vanilla mobs scale modestly with cave danger.
- Changed optional quest/skill/TNK Loot rewards to a **first natural-cache discovery milestone**, once per team expedition per player, with persistent claim protection.
- First cache discovery preserves a minimum three-minute extraction/looting window.

### Existing gameplay retained
- All five player-buildable expedition gateways still work and prefer a natural starting area matching the selected tier.
- Party expeditions, optional FTB Teams grouping, per-player daily entry limits, shared time limits, inventory protection on death, safe extraction, and optional mod fallbacks remain.
- `/duskdelvers enter` now behaves like starting a Tier I expedition and consumes a daily entry.
- New biome status information appears during exploration and on the action-bar.

### Compatibility
- Twilight Forest, The Aether, Alex's Caves, Cataclysm and Iron's Spells: conditional content only when installed and registry-valid.
- TNK Unidentified Loot, FTB Quests and Puffish Skills: conditional discovery rewards; no external mod is mandatory.
- Epic Fight and Apotheosis: native behavior remains intact; bespoke combat patches/boss AI are not claimed.

### Important migration note
- Use **a newly created test world** for the full 16-biome distribution. Existing generated chunks are not overwritten or deleted; old rectangular rooms from 0.2/0.3 remain in previously generated chunks.
- The automated NeoForge Java 21 build and headless dedicated-server biome and loot-table tests pass, but full TNK3 modpack gameplay testing remains necessary.

## 0.3.0-alpha — Protected, limited team expeditions

- Dimension-local death inventory protection without changing global gamerules; vanilla experience optional.
- Configurable daily expedition entry quota (default 3 per player per UTC day), persistent across restarts.
- Native expedition party invitations and shared multiplayer dungeons (default maximum 4 nearby players).
- Optional FTB Teams membership matching with mutual opt-in, retaining native parties when unavailable.
- Persistent server-owned party sessions: one room progress and one countdown, one guardian, one shared chest.
- Per-member reward claim records and party-scaled encounters.
- Optional TNK Loot unidentified reward generation using actual pool/category/progression methods.
- Optional FTB Quests tier completion via configured quest IDs.
- Optional Puffish Skills experience via a configured skill category ID.
- Conditional registry-checked creature encounter profiles for Iron's Spells, Alex's Caves, Cataclysm, Twilight Forest and Aether.
- Create and Ars Nouveau themed room details when their blocks exist.
- Provider toggles, updated diagnostics and full compatibility documentation.
- See [Team Expeditions](docs/TEAM_EXPEDITIONS.md) and [Compatibility](docs/COMPATIBILITY.md).

### Known limitations
- Only vanilla inventory slots are specifically snapshotted for dimension death protection; external accessory inventories require separate validation.
- Native party invitation state isn't yet saved across a server restart; active sessions are saved.
- Modded bosses, unique animations, direct Epic Fight patches and Create/Ars interactive puzzles remain future work.
- No live full-TNK3 cross-mod player test has been asserted.

## 0.2.2 — Timed expeditions
- Configurable tier countdowns, action bar, warnings and automatic extraction.

## 0.2.1 — Player-built tier gateways
- Five survival portal frames and return/extraction gateways.

## 0.2.0 — Expeditions feature-test build
- Seeded room layouts, five difficulty tiers, vanilla guardian, loot chests and craftable compass.

## 0.1.0-alpha — Dimension foundation
- Custom Chaos Caverns dimension, outpost and safe return travel.
