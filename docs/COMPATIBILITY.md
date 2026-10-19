# Compatibility matrix — 0.2.0

**The base mod requires only NeoForge 1.21.1.** The following are future API integrations; datapacks can already contribute modded mobs using real entity IDs and `requires_mod`.

| Mod | 0.2.0 status | Planned feature |
| --- | --- | --- |
| TNK Unidentified Loot | Not integrated | Unidentified boss rewards, affixes and collections |
| Epic Fight | Not integrated | Supported mob combat patches |
| Iron's Spells | Generic mob pool only | Spellcaster templates and themed rewards |
| Apotheosis | Not integrated | Coordinated affix loot and elite rewards |
| Alex's Caves | Generic mob pool only | Selected mobs, materials and cave environments |
| Cataclysm | Generic mob pool only | Compatible special encounters and custom arenas |
| Twilight Forest | Generic mob pool only | Themed enemies and loot |
| The Aether | Generic mob pool only | Themed sanctuaries and loot |
| Create | Not integrated | Optional mechanical puzzles |
| Ars Nouveau | Not integrated | Magic puzzle alternatives |
| FTB Teams / Quests | Not integrated | Parties, objectives and quest events |
| Puffish Skills | Not integrated | Configurable skill experience |

Never add an external mob to pools until its 1.21.1 entity ID and spawn behavior are verified in an actual modpack. An absent mod or invalid entity ID is skipped at reload and normal vanilla encounters remain available.
