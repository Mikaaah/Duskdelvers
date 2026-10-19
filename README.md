![Duskdelvers Expeditions banner](assets/branding/banner.png)

# Duskdelvers Expeditions

<p align="center">
  <img alt="Minecraft 1.21.1" src="https://img.shields.io/badge/Minecraft-1.21.1-462663?style=flat-square">
  <img alt="NeoForge" src="https://img.shields.io/badge/Loader-NeoForge-6B2C8F?style=flat-square">
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-88495E?style=flat-square">
  <img alt="Alpha" src="https://img.shields.io/badge/Status-Alpha-B327CF?style=flat-square">
</p>

> **Beneath the kingdoms, darkness holds its own.**

**Duskdelvers** is an exploration-focused Minecraft mod for **NeoForge 1.21.1**, designed for *Towns & Kingdoms 3*. It will introduce the **Chaos Caverns**, an underground expedition dimension with challenging encounters, modular dungeons, treasures, and optional integrations with the modpack's combat and progression systems.

## Project links

- [Official project wiki](https://github.com/Mikaaah/Duskdelvers/wiki) · [Getting started](https://github.com/Mikaaah/Duskdelvers/wiki/Getting-Started) · [Commands](https://github.com/Mikaaah/Duskdelvers/wiki/Commands)
- [Chaos Caverns](https://github.com/Mikaaah/Duskdelvers/wiki/Chaos-Caverns) · [Compatibility](https://github.com/Mikaaah/Duskdelvers/wiki/Compatibility) · [Troubleshooting](https://github.com/Mikaaah/Duskdelvers/wiki/Troubleshooting)
- [Development roadmap](https://github.com/Mikaaah/Duskdelvers/wiki/Roadmap) · [Technical roadmap](docs/ROADMAP.md)
- [Architecture and current limitations](docs/ARCHITECTURE.md) · [Compatibility plan](docs/COMPATIBILITY.md)

## Current status

**0.1.0-alpha — foundation release under development.** The initial milestone focuses on a loadable mod, explorable cave dimension, protected arrival chamber, a handcrafted starter vault, and return travel. Procedural dungeons, unique bosses, five difficulty tiers, and the twelve optional compatibilities are **planned**, not yet implemented.

## Quick start (development build)

1. Install Minecraft 1.21.1, NeoForge 21.1.x, and Java 21.
2. Build with `gradle build` (Gradle 9.2.1) or retrieve the build artifact from GitHub Actions.
3. Place the JAR into `mods/` and start a test world with cheats enabled.
4. Run `/duskdelvers enter` to explore the Chaos Caverns.
5. Run `/duskdelvers exit` to return to your saved origin.

The initial entrance is command-based for repeatable testing. In-world portals, progression, multiplayer expedition parties and instance resets are future milestones.

## Design principles

- **Standalone first:** no required content mods.
- **Datapack extensible:** dimension and generation data are resource-packaged JSON.
- **Optional compatibility:** TNK Unidentified Loot, Epic Fight, Iron's Spells, Apotheosis, Alex's Caves, Cataclysm, Twilight Forest, The Aether, Create, Ars Nouveau, FTB Teams / Quests, and Puffish Skills.
- **Progression without gates:** five intended difficulty tiers, freely selectable when implemented.
- **Dedicated-server aware:** server-authoritative teleportation, world modifications and reward generation.
- **Performance conscious:** bounded encounters and chunk loading, configurable later.

## Build

The CI workflow compiles the mod with Java 21 and publishes its JAR as a GitHub Actions artifact. This repository uses the official NeoForge ModDevGradle 1.21.1 pattern, with Gradle installed through GitHub Actions (no local wrapper binary required).

```bash
gradle build --no-daemon
```

Read [the roadmap](docs/ROADMAP.md), [architecture](docs/ARCHITECTURE.md), [compatibility plan](docs/COMPATIBILITY.md), and [official branding guidelines](docs/BRANDING.md).

## License

All rights reserved unless the repository owner adopts an explicit license.
