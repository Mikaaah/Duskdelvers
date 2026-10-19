<div align="center">

![Duskdelvers Expeditions](assets/branding/banner.png)

**Beneath the kingdoms, darkness holds its own.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-462663?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-6B2C8F?style=flat-square)](https://neoforged.net/)
[![Java](https://img.shields.io/badge/Java-21-88495E?style=flat-square)](https://adoptium.net/)
[![Build](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml/badge.svg)](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml)
[![Version](https://img.shields.io/badge/Version-0.2.0-B327CF?style=flat-square)](CHANGELOG.md)

</div>

# Duskdelvers

**Duskdelvers** is an exploration-focused Minecraft mod for **NeoForge 1.21.1**, developed for *Towns & Kingdoms 3*. Explore the **Chaos Caverns**, conquer generated dungeons, overcome increasingly dangerous encounters and collect treasure.

- **Project links:** [Wiki](https://github.com/Mikaaah/Duskdelvers/wiki) · [Status](https://github.com/Mikaaah/Duskdelvers/wiki/Status) · [Issues](https://github.com/Mikaaah/Duskdelvers/issues) · [Towns & Kingdoms 3 on CurseForge](https://www.curseforge.com/minecraft/modpacks/towns-kingdoms-3)
- **Planned optional integrations:** TNK Unidentified Loot ([CurseForge page pending approval](https://www.curseforge.com/minecraft/mc-mods/unidentified-loot-a-towns-kingdoms-mod)) and Resonant Arcana ([CurseForge page pending approval](https://www.curseforge.com/minecraft/mc-mods/resonant-arcana)). These are separate projects, not current Duskdelvers features.

## What's included in 0.2.0

| System | Feature |
| --- | --- |
| **Chaos Caverns** | Underground dimension with dripstone, lush cave and deep-dark zones |
| **Generated expeditions** | Per-run connected layouts of 5–9 dungeon rooms with corridors and decorated arenas |
| **Progressive difficulty** | Five tiers with increasing room count, enemy quantity, mob attributes and treasure |
| **Encounters** | Datapack-configurable mob pools, named elite encounters and a guardian in the final room |
| **Rewards** | Interior treasure caches, a guardian reward chest and completion XP |
| **Delver's Compass** | Craftable entry/exit item with sneak-right-click tier selection |
| **Commands** | Start at any tier, inspect status, visit the old outpost and leave |
| **Configuration** | Optional mob scaling, encounter caps and guardian reward toggle |

> **0.2.0 is a playable test build, not a finished content release.** The guardian uses a scaled vanilla creature, and dungeon rooms are generated on demand rather than placed by natural jigsaw world generation. Deep compatibility adapters and multiplayer instance resets are not included yet.

## Get started

1. Install Minecraft **1.21.1**, NeoForge **21.1.262+** within 21.1.x, and Java **21**.
2. Download the newest successful artifact from [GitHub Actions](https://github.com/Mikaaah/Duskdelvers/actions/workflows/build.yml), extract it and put the JAR in `mods/`.
3. Use a **new disposable test world**. The current version leaves old expedition regions in world saves.
4. Craft a **Delver's Compass** (iron ingots, vanilla compass and amethyst shard) or `/give @s duskdelvers:delvers_compass`.
5. Outside the dimension, **sneak-right-click** to cycle Tier I–V, then **right-click** to start.
6. Defeat encounters, clear the last guardian, retrieve the boss treasure and use the compass to return.

### Useful commands

| Command | Function |
| --- | --- |
| `/duskdelvers start 1` through `/duskdelvers start 5` | Create a new expedition at the requested tier |
| `/duskdelvers enter` | Open the fixed 0.1.0 test outpost |
| `/duskdelvers status` | Show active run and encounter pool information |
| `/duskdelvers exit` | Return to your saved origin |

Commands require cheats/operator access as dictated by the game/server command setup.

## Datapack-driven encounters

Files in `data/<namespace>/expedition_encounters/*.json` can add mobs from installed mods without requiring a direct code dependency.

~~~json
{
  "entries": [
    {"entity": "minecraft:skeleton", "weight": 30, "min_tier": 1, "max_tier": 5},
    {"entity": "examplemod:cave_beast", "weight": 10, "min_tier": 3, "max_tier": 5, "requires_mod": "examplemod"}
  ]
}
~~~

Invalid entity IDs and missing optional mods are ignored, with vanilla fallback encounters.

## Development

Build with **Java 21** and **Gradle 9.2.1**:

~~~bash
gradle clean build --no-daemon
~~~

[Roadmap](docs/ROADMAP.md) · [Architecture](docs/ARCHITECTURE.md) · [Compatibility](docs/COMPATIBILITY.md) · [Branding](docs/BRANDING.md) · [Changelog](CHANGELOG.md)

## License

All rights reserved.
