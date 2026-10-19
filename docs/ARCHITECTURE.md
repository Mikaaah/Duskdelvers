# Architecture — 0.3.0-alpha

The Chaos Caverns is a bundled noise-based cave dimension. Its three biome regions use vanilla cave biome definitions. True custom cavern biomes and native worldgen jigsaw structures are planned.

**Expeditions:** `ExpeditionIndex` allocates a unique, monotonic run ID in overworld SavedData. `ExpeditionLayout` creates a seeded connected route on a 4×4 grid. `DungeonBuilder` constructs safe stone chambers, corridors and loot caches in a reserved region, entirely server-side.

**Combat:** `ExpeditionRuntime` activates rooms based on nearby player position every 20 ticks. Enemies are tagged with run/room IDs so ambient mobs do not count toward encounter completion. The boss reward chest appears when the final encounter clears.

**Content:** `EncounterProfiles` reloads external datapack JSON, validates installed mods and entity registry IDs, and chooses weighted enemies by tier. `DuskConfig` exposes per-world server settings.

**Portals:** `PortalFrames` finds complete 4×5 frames made of a specific vanilla block, fills their 2×3 interiors with tier-specific `GatewayBlock` states, validates frame integrity and creates return gateways. `GatewayBlock` handles server-authoritative walk-through travel with cooldowns. Entry/exit share the existing `DuskCommands` destination logic; an operator-only command supports test automation.

**Timer:** `ExpeditionTimer` counts one second per 20 active player ticks while the player is in Chaos Caverns, persists its state in player NBT, sends action-bar and chat warnings, and uses the common `DuskCommands.returnPlayer` extraction path on expiry. `DuskConfig` controls durations and post-boss grace. Outside-dimension time and offline sessions do not consume the countdown.

**Travel:** `DelversCompass` starts a run or returns to the saved external origin. `DuskCommands` offers equivalent commands and preserves the 0.1 starter outpost.

## Known limitations
- One player owns each run. No secure co-op party ownership/claim system.
- No automatic chunk cleanup/reset. Old regions persist.
- Some foreign modded entities require their native environment or bespoke AI. They must be tested before adding to loot/encounter pools.
- Bosses currently use vanilla mob entities with stronger attributes, not custom models and animations.
- The generic JSON pool is not full API integration with TNK Loot or other third-party mods.
- No claim of in-game client/server validation until tests are performed.

## New shared-sessions architecture

- `ExpeditionQuota`: world-persistent per-player UTC-day run limits, charged atomically for all team members after successful dungeon generation.
- `ExpeditionParty`: transient invitations, accepted native parties, optional mutual opt-in FTB team ID resolution, radius and party cap validation.
- `ExpeditionSessions`: world-persistent party membership, one room state, one timer, boss completion and per-member reward claim ledger. The tick director processes rooms once regardless of players.
- `CavernsDeathProtection`: death-time inventory/experience snapshot, dimension-only drop suppression and respawn restore. Never changes global gamerules.
- `ExpeditionCompat`: runtime-checked TNK Loot token generation, FTB Quests completion, Puffish category XP commands, provider registry checks, and NeoForge completion events.
- `DungeonBuilder`: optional Create/Ars room detail accents resolved against installed block registries.
- `EncounterProfiles`: provider-conditional mob entries validated during reload. Incompatible mob constructors fall back to vanilla mobs.

Current cave layout and dungeon architecture remains the fixed-size prototype. The session/party layer is not yet a true separate dimension instance or safe reset manager.
