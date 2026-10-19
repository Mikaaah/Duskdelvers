# Compatibility plan

All integrations are **planned** and **optional**. None are implemented in 0.1.0-alpha.

| Integration | Planned responsibilities |
| --- | --- |
| TNK Unidentified Loot | Exclusive expedition rewards, discovery, rarity |
| Epic Fight | Validated entity combat patches, no indiscriminate replacement |
| Iron's Spells | Casters and magic bosses |
| Apotheosis | Coordinated affix gear and elite rewards; avoid double drops |
| Alex's Caves | Selected creatures, cave blocks and environments |
| Cataclysm | Designated arena encounters only |
| Twilight Forest | Themed dungeon mob pools |
| The Aether | Themed sanctuaries and rewards |
| Create | Optional mechanical puzzles |
| Ars Nouveau | Optional magical interactions |
| FTB Teams / Quests | Party objectives and quest completion events |
| Puffish Skills | Skill XP on verified expedition completion |

Each adapter should self-disable when its provider is absent, datapacks must validate registry IDs, and loot/XP rewards must be awarded server-side. The underlying cave dimension remains functional without any optional content mod.
