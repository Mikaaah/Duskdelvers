# Changelog

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
