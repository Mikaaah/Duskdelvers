# Player-built Expedition Portals (0.2.1)

Build **one portal per difficulty tier** with ordinary survival blocks. There are no naturally generated portal hubs in this version.

## Frame layout

The **external frame** is 4 blocks wide × 5 blocks tall. Its **air-filled opening** is 2 × 3. Build the frame vertically either along the X axis or Z axis.

```text
F F F F
F . . F
F . . F
F . . F
F F F F
```

`F` = the matching tier's frame block. `.` = air. All 14 frame blocks must match. The opening must be clear.

| Tier | Expedition | Frame block | Portal color |
| --- | --- | --- | --- |
| I | Forgotten Depths | Mossy Stone Bricks | Lime green |
| II | Shattered Hollows | Prismarine Bricks | Light blue |
| III | Resonant Abyss | Block of Amethyst | Violet |
| IV | Ancient Dominion | Polished Blackstone Bricks | Amber |
| V | Chaos Unbound | Crying Obsidian | Crimson |

### Activation

1. Build the 4×5 rectangular frame with its 2×3 opening. **No special portal frame item is required.**
2. Craft or obtain a **Delver's Compass** (`duskdelvers:delvers_compass`).
3. **Right-click any frame block with the compass** to light the portal. An incomplete frame displays an error message instead.
4. **Walk through the portal** to start a newly generated expedition at the frame's tier.

You may build multiple portals at your base, and all five portals can be used immediately if you have the materials. Every successful entry allocates a new dungeon region in the same **Chaos Caverns** dimension. This is deliberately not five separate dimensions.

### Returning

Every generated dungeon has a glowing **Return Gateway** in its entrance chamber. After defeating the final guardian, another extraction gateway appears in the boss chamber. Walk into either to go back to your saved origin.

**Gateway cooldown:** 100 server ticks to prevent instantaneous travel loops. After returning, the mod attempts to place you safely beside the frame rather than in the portal itself.

### Frame integrity

Breaking any block of an active frame extinguishes the interior gateway blocks. Fix the frame, then right-click again with the compass to reactivate it.

### Debugging

In test worlds with cheats enabled, use `/duskdelvers portal activate <x> <y> <z>` to activate a complete frame by supplying the position of any frame block. This is an operator-only debugging command, not needed in survival.

The original compass entry/exit controls and `/duskdelvers start <tier>` commands remain available as backup.

### Limitations

- Visuals currently use themed, translucent vanilla glass textures; animated rune/particle visuals can follow once portal gameplay is tested.
- Party sharing and instance resets are not implemented. Each triggering player currently starts their own expedition.
- Player-return safe-placement is best effort and should be tested with frames embedded in different base designs.
