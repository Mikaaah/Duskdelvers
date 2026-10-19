# Chaos Caverns time limits (0.2.2)

Duskdelvers limits **each player's visit** to Chaos Caverns, including the original arrival outpost. Each new expedition receives a new tier-specific countdown.

## Default time limits

| Area / tier | Default | Generated dungeon rooms |
| --- | ---: | ---: |
| Original arrival outpost | 30 minutes | Fixed test outpost |
| Tier I — Forgotten Depths | 20 minutes | 5 |
| Tier II — Shattered Hollows | 25 minutes | 6 |
| Tier III — Resonant Abyss | 30 minutes | 7 |
| Tier IV — Ancient Dominion | 35 minutes | 8 |
| Tier V — Chaos Unbound | 40 minutes | 9 |

Higher tiers get more time because their expeditions contain more rooms and tougher encounters. There are no hard progression locks.

## In-game behavior

- The countdown starts after the player enters the dimension by portal, compass or command.
- A **MM:SS countdown appears in the action bar** once per second, using purple, yellow and red urgency colors.
- Players receive chat warnings at **5:00**, **1:00** and **0:10** remaining.
- At **0:00**, the player is **automatically returned** to their saved entry position. When no valid origin exists, the fallback is the overworld spawn.
- **Inventory and collected items are preserved.** Expiry cancels the player's active expedition; a boss chest is not awarded unless the boss was already cleared.
- On **boss defeat**, a minimum of **3 minutes** is guaranteed for looting and extraction, as long as the timer is enabled.
- The timer pauses during **logout**, server shutdown and time spent **outside** Chaos Caverns. The remaining seconds are saved in player persistent NBT, including player clone handling for respawns.
- Teleporting into Chaos Caverns by external commands or mods also initializes a timer at the next server player tick if none exists.
- Leaving with the return gateway, compass or `/duskdelvers exit` removes the current countdown. Starting another run issues a new tier timer.

## Server configuration

Edit the generated world-specific server config, usually `<world>/serverconfig/duskdelvers-server.toml`, and restart or reload as appropriate for your server setup.

```toml
[timeLimits]
enabled = true
actionBarCountdown = true
outpostMinutes = 30
tier1Minutes = 20
tier2Minutes = 25
tier3Minutes = 30
tier4Minutes = 35
tier5Minutes = 40
extractionGraceMinutes = 3
```

Durations can be configured from **1 to 240 minutes**, with **0–30 minutes** for post-boss grace. Set `enabled=false` for unlimited time in the dimension.

## Commands

- `/duskdelvers time` — display your remaining time inside Chaos Caverns.
- `/duskdelvers limits` — list currently configured server time limits; also works from the server console.
- `/duskdelvers status` — see expedition progress together with the countdown.

## Multiplayer scope

The current timer is **per-player**, not synchronized by party. In a future FTB Teams cooperative mode, the party should share a single authoritative timer. Automatic instance cleanup is still out of scope for 0.2.2.

## Testing

The CI headless server checks that the dimension loads, the compass recipe parses, portal frames activate correctly and default tier durations are reported. **Player-driven expiry, cross-dimension extraction, logout/resume and client HUD appearance still require game testing.**