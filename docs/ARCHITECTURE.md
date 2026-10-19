# Architecture

## Mod ID

`duskdelvers`, Java package `com.townsandkingdoms.duskdelvers`.

## Dimension

- `duskdelvers:chaos_caverns` is supplied as bundled Minecraft worldgen data.
- The noise settings derive from vanilla 1.21.1 Nether cave density, replacing the default material with stone and fluid with water. A custom noise generator is not implemented in Java.
- The biome source uses vanilla dripstone, lush and deep-dark caves; custom Duskdelvers biomes are future work.
- The cave dimension is 128 blocks tall and uses the inherited bedrock rules.

## Travel and expeditions

The foundation commands are `/duskdelvers enter`, `/duskdelvers exit` and `/duskdelvers status`. The current source adds `/duskdelvers start <1-5>`, a Delver's Compass, saved expedition IDs, route planning, room construction, room-scoped encounters, a final guardian, completion rewards and server configuration.

These are early 0.2 implementation work. The latest Actions build failed; do not treat the source as a verified or released playable loop until it builds and is tested in-game. The documented 0.1.0-alpha version has not been changed.

## Starter area

`StarterOutpost` creates a fixed sample arrival outpost, passage and vault once per world. It is a foundation/test area separate from the newer expedition route generation.

## Encounter data

Encounter profiles are loaded from datapack JSON under `data/<namespace>/expedition_encounters/*.json`. The loader filters missing mod IDs and invalid entity IDs rather than linking optional mods as hard Java dependencies. Optional compatibility is not yet a verified support matrix.

## Dependencies

Minecraft and NeoForge are the required runtime. Optional adapters are intended to remain soft dependencies.

## Current validation limits

- Latest GitHub Actions build for the current source commit failed.
- Complete client, dedicated-server and multiplayer in-game validation is outstanding.
- Save migration, run cleanup/reset safety, and balancing still require review.
- Do not install current development source in a valuable world or describe it as a released feature.
