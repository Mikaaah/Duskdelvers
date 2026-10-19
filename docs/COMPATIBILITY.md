# Duskdelvers 0.4.0 — Optional Mod Compatibility

Duskdelvers remains **standalone**. These integrations activate only when installed and enabled, with vanilla fallbacks.

| Optional mod | Cave-exploration integration |
| --- | --- |
| **Twilight Forest** | Twilight Thicket and Enchanted Grove cave biomes (original Duskdelvers biomes). Registry-checked Twilight Oak ground accents and optional native forest monsters. No original Twilight dimension generation is transplanted. |
| **The Aether** | Aether Glades, Golden Sanctuary and Glowing Highlands cave biomes (original Duskdelvers biomes). Optional Holystone natural patches and native creature pool. The world remains a sealed cave system, not floating islands. |
| **Alex's Caves** | Registry-validated optional creature encounters in appropriate difficulty levels. |
| **Cataclysm** | Selected registered normal enemies; huge scripted bosses are not automatically spawned in unsuitable spaces. |
| **Iron's Spells** | Optional registered spellcaster encounters; retain their native AI when valid. |
| **TNK Unidentified Loot** | One-time milestone tokens for each member of a team when its first natural cache is discovered, using player-unlocked loot ranks and valid category pools. |
| **Apotheosis** | TNK Loot retains responsibility for affixes. Duskdelvers doesn't duplicate affix rolls. |
| **Epic Fight** | Native combat patches are not overridden; custom patches are not implemented yet. |
| **FTB Teams** | Opt-in matching of nearby team members sharing the real FTB team ID. Native parties remain available without it. |
| **FTB Quests** | Per-tier optional configured quest completion upon first cave-cache discovery. |
| **Puffish Skills** | Configured category XP when the team discovers its first cache, once per player per run. |
| **Create** | No forced puzzles; Create blocks are no longer used in deleted dungeon structures. |
| **Ars Nouveau** | No forced puzzles; Ars blocks are no longer used in deleted dungeon structures. |

No mod version is made a hard dependency to render a cave biome. Fallback blocks/creatures are used when optional registry entries are unavailable. Exact third-party runtime combinations require testing in the Towns & Kingdoms pack; the no-optional-mod dedicated server is tested automatically.

The optional encounter profiles reside under \`data/duskdelvers/expedition_encounters/\`. Every entity is validated against actual loaded registry IDs; invalid entries are skipped. A very small ambient roaming spawn budget prevents overcrowding.

## Configurable progression IDs

~~~toml
[progression]
tier1QuestId = ""
tier2QuestId = ""
tier3QuestId = ""
tier4QuestId = ""
tier5QuestId = ""
skillsCategory = ""
skillsXpPerTier = 50
~~~

Blank values intentionally disable tier quest completion or skill XP until the modpack's real IDs are supplied.

## Cave biome difficulty is always independent of optional mods

Biome difficulty is set by \`CavernBiomes\` and is compatible with a completely vanilla NeoForge installation. The fog, cave colors, ground materials and natural loot caches still work without Twilight Forest, The Aether or any other optional mod.
