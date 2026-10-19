# Architecture — Duskdelvers 0.4.0

## Terrain and biome source

- Custom datapack noise-based dimension: \`duskdelvers:chaos_caverns\`.
- The previous **huge-cave \`noise_router.final_density\` is unchanged**.
- Multi-noise biome source expanded from 3 vanilla cave biomes to 16 custom Duskdelvers cave biomes.
- Temperature/vegetation sampling runs at \`xz_scale=0.82\` vs \`0.25\` previously; this reduces average climate region scale without altering cavern dimensions.
- Biome-specific surface rules and natural floor accents; two worldgen Feature types: \`cavern_cache\` and \`cavern_accent\`.
- Natural caches use lazy Minecraft chest loot tables, assigned from the local biome danger.
- No structure registries, jigsaw pieces, artificial dungeon rooms or boss arenas.

## Expedition sessions

- \`ExpeditionLayout\` retains a compact, saved one-position **arrival anchor** so older saved session formats can still be parsed. It **does not** assemble dungeon rooms.
- \`CavernArrival\` samples a bounded number of cave columns and selects a safe naturally carved floor, preferring the gateway's tier; only creates a tiny air pocket as a rare safety fallback.
- \`ExpeditionIndex\`: durable monotonic run IDs; spawn areas are widely separated so teams generally begin independently.
- \`ExpeditionQuota\`: 3 per UTC day by default, charged once per entering team member.
- \`ExpeditionParty\`: optional FTB Teams resolving plus native invitations.
- \`ExpeditionSessions\`: player membership, one countdown, discovery marker and per-member reward claims; all persisted using SavedData.

## Biome encounters and world rewards

- \`CavernBiomes\`: name and danger level for all sixteen biomes.
- \`ExpeditionRuntime\`: checks biome and time once per second per player; scales eligible vanilla mobs once, and may add infrequent optional registered cave creatures without forced chunks or boss AI overrides.
- \`EncounterProfiles\`: datapack registry ID validation, conditional on installed optional mod IDs.
- \`ExpeditionRuntime.onCacheOpened\`: discovers a natural worldgen cache before loot is rolled; records one shared discovery per run, grants each member a reward only once and restores a three-minute minimum looting grace.
- \`ExpeditionCompat\`: optional TNK Loot tokens, FTB Quests and Puffish Skills milestones, guarded by real loaded-mod tests and configurable IDs. Emit \`CavernCacheDiscoveredEvent\` on first discovery per eligible player.
- \`CavernsDeathProtection\`: vanilla inventory/experience recovery on dimension deaths only.

## Performance limits

- All world loot and accents are generated in worldgen, not endlessly scanned by a player tick or global chunk listener.
- No indefinite forced chunks, custom dimensions per party, or old expedition region deletion.
- Optional roaming fauna: one candidate every 30 seconds per player, capped and spawned only in already loaded chunks.
- Biome danger updates once per second, and mobs receive biome scaling only once.
- Original cave noise density, height, and generator sampler remain intact.

## Migration

v0.4 does not automatically erase old authored rooms from existing chunks or forcibly rewrite old saves. Test in a fresh world. Back up valuable worlds before changing the dimension's biome/noise settings.
