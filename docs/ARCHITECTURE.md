# Architecture

## Mod ID
`duskdelvers`, Java package `com.townsandkingdoms.duskdelvers`.

## Dimension
- `duskdelvers:chaos_caverns` is supplied as a bundled Minecraft worldgen data definition.
- `worldgen/noise_settings/chaos_caverns.json` derives from vanilla 1.21.1 Nether cave density, replacing its default material with stone and fluid with water. A custom noise generator is **not** implemented in Java yet.
- Biome source currently uses vanilla dripstone, lush and deep dark caves; custom Duskdelvers biomes are a future feature.
- Cave dimension is 128 blocks tall, covered, dark and bounded by the inherited bedrock rules.

## Travel
`/duskdelvers enter` stores the player's original dimension, XYZ and view angles in the player's persistent data, creates the starter outpost when absent and teleports the player there. `/duskdelvers exit` restores the origin, falling back to overworld spawn if the original dimension has disappeared.

## Test room
`StarterOutpost` installs a fixed sample outpost, passage and vault once per world. It uses a center-floor completion marker and does not regenerate the initial loot on every visit. This is a **test fixture**, not the planned procedural dungeon generator.

## Dependencies
Only Minecraft and NeoForge. Optional adapters are intended to be soft dependencies, with registry checks and guarded code paths.

## Known limitations
- Entry via commands only; these are intended for early test worlds.
- No unique mobs, bosses, procedural structure placement or difficulty tiers in v0.1.0-alpha.
- No automated server launch test has been asserted; check Actions builds for compiler status.
- Mod-removal save migrations, reset safety and full multiplayer balancing remain unimplemented.
