# Team cave expeditions — 0.4.0

The structure-free Chaos Caverns is now freely explorable with your team. A run consists of a **safe cavern arrival, shared timer, exploration, natural world loot and extraction**. No boss or dungeon structure is required.

- Default **3 entries per player per UTC day**. Each group member consumes one entry on entry. Set \`entriesPerDay=0\` for unlimited.
- Default **4 team members** within 16 blocks of the leader.
- \`/duskdelvers team invite <player>\`, \`team accept\`, \`team leave\`, \`team status\` for native parties.
- \`/duskdelvers team ready\` toggles FTB Teams matching, if installed. Matching is mutual opt-in.
- Team gets one arrival and one countdown, independent of how many members are online; countdown pauses while everyone is offline.
- Biome danger follows each individual's current location; teams are free to split up, but higher-tier caves become harder.
- **First discovered natural cache** is one shared milestone with one-time per-member optional rewards. Each found barrel's items are physical loot, so players coordinate who takes them.
- All players can keep vanilla inventory/experience if they die inside Chaos Caverns. A death ends the dead player's run, but teammates can continue.
- Time expiration extracts all online teammates. Members returning from offline sessions of an expired run are extracted safely.
- No region reset, private team dimension or automatic cache respawn in this version.

## Commands

| Command | Action |
| --- | --- |
| \`/duskdelvers start <1-5>\` | Leader starts an expedition to naturally generated caverns |
| \`/duskdelvers entries\` | Check daily attempts |
| \`/duskdelvers entries reset <player>\` | Operator-only testing reset |
| \`/duskdelvers team invite <player>\` | Invite nearby players |
| \`/duskdelvers team accept\` | Accept an invitation |
| \`/duskdelvers team ready\` | FTB Teams matchmaking opt-in |
| \`/duskdelvers status\` | Shows local cave danger, team size, timer and attempts |
| \`/duskdelvers exit\` | Safely extract |

Native invitations are held in server memory, while active run metadata, discovery claims and daily quota live in persistent world SavedData.
