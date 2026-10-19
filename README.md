<div align="center">

![Duskdelvers Expeditions](assets/branding/banner.png)

**Beneath the kingdoms, darkness holds its own.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-462663?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-6B2C8F?style=flat-square)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-88495E?style=flat-square)](https://adoptium.net/)
[![Build](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml/badge.svg)](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml)
[![Version](https://img.shields.io/badge/Version-0.4.0-B327CF?style=flat-square)](CHANGELOG.md)

</div>

# Duskdelvers — Explore the Chaos Caverns

**Duskdelvers** is a standalone, cave-exploration mod for Minecraft **1.21.1 / NeoForge**, created for Towns & Kingdoms. Journey through enormous connected caverns, discover short-lived biome regions with their own danger levels, find natural treasure caches, and survive your expedition before time runs out.

**0.4.0 completely removes the generated dungeon structures introduced in 0.2/0.3.**

## What's new?

- **16 cave biomes**, from limestone hollows and fungal grottos to Twilight-inspired thickets, Aether-inspired sanctuaries, crystalline fields and chaos rifts.
- **Preserved enormous cave terrain.** The original cavern-shaping noise density is unchanged. Only biome climate noise scales upward (\`0.25 → 0.82\`), meaning regions change more frequently without shrinking the caves.
- **Difficulty is determined by your current biome (Tier I–V)** instead of a sequence of artificial rooms or forced dungeon encounters.
- **Worldgen natural treasure caches**: rare barrels on cave floors with tier-specific loot tables. No castles, box-shaped chambers, corridors, dungeon rooms, jigsaw modules, or boss vaults.
- **Biome-specific natural rock, moss, crystal and soil patches**, plus conditional Aether Holystone and Twilight Forest blocks when available.
- **Natural hostile mobs** with modest biome-sensitive vanilla scaling and infrequent optional modded creatures.
- **First cache discovered** unlocks once-per-run per-player optional TNK Loot, FTB Quests and Puffish Skills milestone rewards.
- Retains **five player-built gateways**, **daily entries**, **co-op parties**, **shared timer**, **keep-inventory within Chaos Caverns** and safe extraction.

## Play a cave expedition

1. Install NeoForge 21.1.x (tested against 21.1.262) and Java 21.
2. Download the newest successful [GitHub Actions build](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml). Extract the JAR into \`mods/\`.
3. **Use a new test world** to see all sixteen biomes and the new loot generation.
4. Craft or obtain a Delver's Compass: \`/give @s duskdelvers:delvers_compass\`.
5. Build a tier gateway using the [portal guide](docs/PORTALS.md), activate its frame with the compass, and enter. Right-clicking the compass in the air is an optional shortcut.
6. Explore freely, find natural caches, move between biome difficulty levels, and return through the compass or \`/duskdelvers exit\` before the shared time limit expires.

There is **no artificial boss-clear requirement**. Exploration and loot hunting are the gameplay loop.

## Gameplay defaults

| Setting | Default |
| --- | --- |
| Entrances | Five player-built gateway types, Tier I–V |
| Biomes | 16 unique cave biomes, 5 danger levels |
| Time | 20 / 25 / 30 / 35 / 40 minutes by gateway tier |
| Daily attempts | 3 per player (UTC reset; configurable, 0 = unlimited) |
| Party size | 4 nearby members, native parties or opted-in FTB Teams |
| Death in dimension | Keep vanilla inventory and experience |
| Loot | Naturally generated world caches, Tier I–V tables |
| Custom structures | **None** |
| Required content mods | **None** |

## Commands

| Command | Description |
| --- | --- |
| \`/duskdelvers start 1\` through \`/duskdelvers start 5\` | Begin a cave expedition at the chosen gateway tier |
| \`/duskdelvers enter\` | Alias for a Tier I expedition (consumes an entry) |
| \`/duskdelvers exit\` | Return to your saved entry location |
| \`/duskdelvers status\` | Show current biome danger, team, time and daily entries |
| \`/duskdelvers entries\` | View your daily allowance |
| \`/duskdelvers team invite <player>\` | Invite another player |
| \`/duskdelvers team accept\` | Join the party |
| \`/duskdelvers team ready\` | Opt into FTB Teams matching |
| \`/duskdelvers compat\` | Show detected optional mods |
| \`/duskdelvers entries reset <player>\` | Operator-only quota reset for testing |

## Optional compatibility

Standalone operation is the default. Optional content includes **Twilight Forest, The Aether, Alex's Caves, Cataclysm, Iron's Spells, TNK Unidentified Loot, FTB Teams, FTB Quests, Puffish Skills, Create and Ars Nouveau**. Apotheosis and Epic Fight keep their native mechanics; no unverified custom combat patches are applied.

**Twilight- and Aether-inspired biomes are genuine new Duskdelvers cave biomes**, not copies of the mods' original aboveground/sky biomes. Their optional native blocks and creatures are added only if the provider and IDs exist; vanilla replacements are always available. See [compatibility details](docs/COMPATIBILITY.md).

## Documentation

[Biome atlas](docs/BIOMES.md) · [Team expeditions](docs/TEAM_EXPEDITIONS.md) · [Portals](docs/PORTALS.md) · [Time limits](docs/TIME_LIMITS.md) · [Architecture](docs/ARCHITECTURE.md) · [Roadmap](docs/ROADMAP.md) · [Changelog](CHANGELOG.md)

## Building

Java 21 and Gradle 9.2.1:

~~~sh
gradle clean build --no-daemon
~~~

## License

All rights reserved.
