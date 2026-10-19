# Duskdelvers optional compatibility — 0.3.0-alpha

**All integrations are soft dependencies:** no other content mod is needed for the jar to compile, launch, generate a dungeon, or provide vanilla loot. Each integration checks actual installed mod IDs or registry entries and has a per-provider server toggle. A headless server test without the optional mods must always pass.

| Mod | This version | Depth / limitation |
| --- | --- | --- |
| **TNK Unidentified Loot** (`tnkloot`) | Generates valid unidentified category tokens from **the user's current progression rank**, checking that the relevant TNK Loot category pool isn't empty. Boss rewards are per participant, once per saved session. | Uses the current TNK Loot static API through runtime-checked reflection, not a hard dependency. |
| **Apotheosis** (`apotheosis`) | TNK Loot's existing identification/affix logic is preserved; Duskdelvers does not add a second automatic affix roll to avoid duplicate gear. | No independent Apotheosis affix-boss spawning API yet. |
| **Epic Fight** (`epicfight`) | Leaves mob entity types, native combat patches and animations alone so compatible combat remains intact. | No custom Epic Fight entity patches or bespoke boss animations implemented. |
| **Iron's Spells 'n Spellbooks** (`irons_spellbooks`) | Conditionally adds validated caster entity IDs to dungeon encounter pools. | Relies on native spellcasting AI; no custom spells/routines. |
| **Alex's Caves** (`alexscaves`) | Cave creatures in conditional encounter pools. | Actual available 1.21.1 entity IDs are checked at datapack reload; complex creature mechanics need playtesting. |
| **L_Ender's Cataclysm** (`cataclysm`) | Selected non-boss enemy candidates in hard-tier rooms. | Bosses intentionally excluded until dedicated arena support is tested. |
| **Twilight Forest** (`twilightforest`) | Selected dungeon creatures in conditional encounter pools. | Original Twilight Forest structures/dimensions remain separate. |
| **The Aether** (`aether`) | Selected creatures in conditional encounter pools. | Original Aether dungeon mechanics remain separate. |
| **Create** (`create`) | Resolves and places Create andesite casing as an optional dungeon room accent. | Machinery puzzles are not yet implemented; no Create dependency. |
| **Ars Nouveau** (`ars_nouveau`) | Resolves and places Arcane Stone accents where registered. | Spell-controlled puzzles are future work. |
| **FTB Teams** (`ftbteams`) | Optional team ID resolver with mutual opt-in, nearby team gathering and shared expedition instances. | Uses the reflective API lookup tested in the TNK Loot source. If unavailable, native parties remain functional. |
| **FTB Quests** (`ftbquests`) | On completed dungeon, optionally marks the configured tier-specific quest ID complete for each participant. | You must supply existing **quest IDs**; not hardcoded, and none is invented. |
| **Puffish Skills** (`puffish_skills`) | Awards category experience using the documented `puffish_skills experience add` command for the configured skill category. | Requires an actual category ID from the TNK3 radial tree; blank disables rewards. |

## Boss completion integration

1. Duskdelvers commits its shared run clear state and creates one boss treasure chest.
2. Each eligible player's reward is checked against the SavedData per-member claim ledger.
3. If TNK Loot exists and the config allows it, generate **one** unidentified token (default) using its `UnidentifiedWeaponItem.create(Category, rank, sourceItem, sourceType)` API. A category is selected only when the TNK Loot registry pool for that rank is nonempty.
4. Optionally award configured FTB Quests completion and Puffish Skills experience.
5. Emit `ExpeditionCompat.ExpeditionCompletedEvent` on the NeoForge event bus for addons.

This does **not** replace independent loot rolled by Apotheosis or TNK Loot on natural mob death. Those mods retain ownership of their own loot systems.

## Datapack encounter pools

`data/duskdelvers/expedition_encounters/` includes sample entries for Iron's Spells, Alex's Caves, Cataclysm, Twilight Forest and Aether. **Each entry is validated against the live entity registry and owning mod; invalid IDs are skipped.** These sample paths are best-effort candidates and should be tested against the exact JAR versions in Towns & Kingdoms 3.

To disable modded encounters without disabling the actual mod, use:

```toml
[compatibility]
optionalCreaturePools = false
```

Or switch off a specific provider with:

```toml
[providers]
cataclysm = false
alexscaves = false
```

If all optional mobs are absent, the vanilla pool remains available.

## Configured FTB Quests and Puffish XP

```toml
[progression]
tier1QuestId = ""
tier2QuestId = ""
tier3QuestId = ""
tier4QuestId = ""
tier5QuestId = ""
skillsCategory = ""
skillsXpPerTier = 50
```

Fill these IDs with your existing quest IDs and category ID. For example, an actual quest ID can be selected in FTB Quests' editor; **don't use the placeholder as a quest ID**. The XP reward is `skillsXpPerTier * tier`.

All vendor reward commands are executed server-side only after boss completion, and only when the provider is installed and enabled. Unknown/invalid IDs do not become dependencies and do not block normal rewards.

## Compatibility verification still needed

Full third-party runtime tests are separate from a successful no-dependency server smoke test. In particular, review FTB team membership on dedicated servers, additional death inventory slot support, Epic Fight's specific combat configurations, modded mob AI outside their native dimension, and TNK Loot's 0.11+ reward/category configuration.
