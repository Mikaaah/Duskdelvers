# Chaos Caverns — Biome Atlas (0.4.0)

The entire dimension is a **huge enclosed cave system**. None of the following biomes replaces the cave terrain with a surface world, island dimension or normal Overworld landscape.

## Biome danger chart

| Biome | Danger | Theme / natural surfaces |
| --- | :---: | --- |
| Limestone Hollows | I | Pale calcite limestone, easy monsters |
| Mosslit Grotto | I | Moss carpets, green cave foliage |
| Fungal Glades | I | Mycelium, mushroom pockets |
| Twilight Thicket | II | Twilight Forest–inspired dark woodland cave, rooted earth |
| Dripstone Chasms | II | Jagged dripstone and warm mineral beds |
| Glimmering Depths | II | Glowing humid clay caverns |
| Enchanted Grove | III | Mystic Twilight-inspired roots and moss |
| Aether Glades | III | Holystone-inspired pale floating-world color palette, **still entirely underground** |
| Frostbound Caverns | III | Snow and sparse natural ice deposits |
| Glowing Highlands | III | Bright glowing cave ecology with basalt/calcite ground |
| Resonant Crystalfields | IV | Calcite beds, rare amethyst pockets, lavender haze |
| Golden Sanctuary | IV | Aether-inspired gold-lit caverns, yellow stone and Holystone accents |
| Ashen Abyss | IV | Black basalt volcanic caves |
| Chaos Rift | V | Dark blackstone/sculk, dangerous cave creatures |
| Shattered Void | V | Broken deepslate, deep-violet ambience |
| Soulbound Crypts | V | Soul-soil caverns, hostile atmosphere |

The danger tier is derived from the actual **worldgen biome at the player's position**. It controls vanilla mob attribute scaling and optional encounter weights. **The portal Tier I–V determines the entry time allowance and a preferred starting biome danger; you may walk across the world into harder or easier biomes.**

## Smaller regions, same enormous caves

- Previous temperature/humidity 2D climate noise \`xz_scale\`: **0.25**.
- New \`xz_scale\`: **0.82**, resulting in quicker climate transitions and more small/medium regions.
- Cave density, dimension height, roof/bedrock envelope and overall large cavern formation are **unchanged**.
- \`noise_router.final_density\` remains exactly the same.
- Surface rules change only **visible rock surfaces**, not the cave's underlying carving.
- Scattered micro-patches add natural detail without allocating chunk tickets or generating structures.

## Twilight Forest and Aether compatibility

These are cave-adapted Duskdelvers biomes, not transplanted original biomes. An Aether biome cannot simply be copied into a closed subterranean dimension without losing much of its native sky/surface behavior.

With **Twilight Forest** installed, its Twilight Oak logs may appear as small natural root/log accents. With **The Aether** installed, Holystone may appear in the pale biome patches. Missing blocks automatically fall back to safe vanilla rock. Modded creatures can also appear via optional, registry-validated encounter pools; no dependency is required to create or load a world.

## World loot instead of structures

- Rare **barrels naturally embedded on cave floors** (no frame, hallway or artificial building around them).
- Five datapack loot tables: \`duskdelvers:chests/cavern_cache_tier_1\` through \`..._tier_5\`.
- Loot tier is calculated from the biome **where the cache is generated**.
- Basic resources, gems, XP bottles and other valuables become more likely as difficulty rises. Netherite scrap is extremely rare and confined to Tier V.
- Cache generation is **one worldgen rarity attempt every four chunks on average**, with an additional check for a valid cave floor; many attempts fail without placing anything.
- Each cache has a normal loot table rolled upon first opening and stays looted afterward.
- The first found cache in each timed expedition unlocks **one separate team milestone**. Additional caches yield their ordinary, physical container loot but cannot duplicate the milestone.
- No artificial combat waves, room clears, guardian bosses or generated dungeon layouts remain.

## Modpack balance

All biome block variants are editable through datapacks, but modifying worldgen definitions affects **newly generated terrain**. Resource balance avoids making valuable amethyst or glowstone blocks into entire cave floors.

**Important for updating:** existing 0.2/0.3 world chunks keep their generated dungeon rooms and original biome palette. For the clean new design, create a **new test world** or back up and regenerate the Duskdelvers dimension's chunks. Do not delete arbitrary region files from a valuable save without a verified backup.
