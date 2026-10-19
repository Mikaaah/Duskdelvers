# Team Expeditions and Entry Limits (0.3.0-alpha)

This version introduces **server-owned shared expedition sessions** rather than cloning one player's encounters for everyone.

## Solo entries and quotas

- Every new dungeon expedition spends **one entry per participating player**, including the leader.
- Default: **3 entries per player per UTC calendar day**. Unlimited with `[entryLimits] entriesPerDay=0`.
- Every run costs an entry **whether cleared, failed, or abandoned**. The original 0.1.0 test outpost (`/duskdelvers enter`) doesn't cost entries.
- A group may not enter unless **every participant** has at least one entry left; charges are atomic after successful room construction.
- Limits and consumed entries are persisted with the world as **overworld SavedData**, independent of player logout/server restart.
- New UTC day = new allowance; personal progress is not reset.
- `/duskdelvers entries` shows your remaining entries; `/duskdelvers daily` displays server quota/team settings.

## Native parties (FTB not required)

| Command | Effect |
| --- | --- |
| `/duskdelvers team invite <player>` | Invite a player to your expedition party |
| `/duskdelvers team accept` | Accept a received invitation |
| `/duskdelvers team leave` | Leave or dissolve the party |
| `/duskdelvers team status` | Show your party size and readiness |
| `/duskdelvers start <1-5>` | Leader starts a shared expedition |
| `/duskdelvers entries` | Check personal daily attempts |
| `/duskdelvers entries reset <player>` | Operator-only reset for an online player's quota |

Team members must be **online, in the same dimension, and within 16 blocks** of their leader to start. Configurable cap: **4 players**. The leader can activate a player-built tier gateway with the Delver's Compass; everyone eligible travels to the same room. Each member retains their own origin and can leave independently.

Party invitations/membership are currently **session-memory state**; reconnecting requires inviting again for a *new* run. Already-running shared expeditions, participant IDs, room progress, reward claims and countdowns persist across server restarts.

## FTB Teams integration

When FTB Teams is present, **every participating FTB teammate must opt in** using:

```mcfunction
/duskdelvers team ready
```

The command toggles expedition matchmaking. The starting leader must also be opted in. Only players with the **same FTB Team ID, within the gathering radius, and opted in** are added automatically; remote teammates do not block the trip. If the API is missing or changes, matchmaking falls back to native parties.

## Shared run mechanics

- **One** generated dungeon and run ID for the entire team.
- **One** room activation, enemy wave and guardian-clear record.
- **One** server-owned countdown, decrementing once per second regardless of player count; offline parties pause.
- Scaling: additional monsters (bounded by `maxRoomEnemies`) and moderate health/damage scaling with party size.
- Successful room clears share experience to online participants in the expedition dimension.
- Boss drops a shared treasure chest; individual TNK Loot rewards and quest/skill progression are **once per eligible participant**, recorded in persistent claim state.
- If one player dies, their expedition ends, but surviving teammates may continue. A death still consumes an entry.
- On expiry, online party members extract to their own entry locations. Members who reconnect into an expired run are extracted.

## Death protection

Deaths that occur **inside Chaos Caverns** preserve the player's **vanilla inventory (main/armor/offhand)** and, optionally, vanilla experience. This never modifies the global `keepInventory` gamerule. Outside the dimension, the world's normal death settings continue to apply.

Other mods' proprietary inventory slots and items are not all proven compatible; test accessories, curios and backpacks with their respective mods. The mod cancels standard player death drops and restores a pre-death inventory snapshot, with the active expedition cleared.

## Relevant world server config

```toml
[entryLimits]
entriesPerDay = 3
maxPartySize = 4
gatherRadius = 16
ftbTeams = true

[deathProtection]
keepInventory = true
keepExperience = true
```

> Important: A shared run is currently an allocated **area** of one Chaos Caverns dimension, not a separate Minecraft dimension per team. There is no automatic old-region deletion yet. Full multiplayer tests with all external mods are still required.
