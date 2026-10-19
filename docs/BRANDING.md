# Duskdelvers — Official Branding

The project uses the owner-supplied **Duskdelvers Expeditions** purple / moonlight artwork.

## Asset placement

| File | Purpose | Resolution |
| --- | --- | --- |
| `assets/branding/banner.png` | **Single** banner at the top of GitHub README and CurseForge mod description | 2036 × 350 |
| `assets/branding/logo.png` | Square logo for CurseForge and other profiles | 1050 × 1050 |
| `src/main/resources/duskdelvers.png` | NeoForge mod-list `logoFile` | 512 × 512 |
| `assets/branding/icon-128.png` | Small UI icon | 128 × 128 |
| `assets/branding/icon-64.png` | Tiny UI icon | 64 × 64 |
| `assets/branding/github-social-preview.png` | GitHub repository social preview | 1280 × 640 |

**Important:** Use **only the banner at the top** of the README, not both the logo and banner stacked together. Preserve the owner's provided artwork and typography. The assets are supplied separately in the branding package until committed to their paths.

### Brand colors

| Token | Hex |
| --- | --- |
| Deep night | `#22182E` |
| Twilight purple | `#462663` |
| Royal violet | `#6B2C8F` |
| Arcane magenta | `#B327CF` |
| Dusky mauve | `#88495E` |
| Moonlight | `#F5EDF8` |

### Integration checklist

- [ ] Add `banner.png` and `logo.png` to `assets/branding/` from the official branding pack
- [ ] Add 512px `duskdelvers.png` to `src/main/resources/`
- [ ] Reference `logoFile="duskdelvers.png"` in `src/main/templates/META-INF/neoforge.mods.toml`
- [ ] Place the wide banner at the beginning of `README.md` (only once)
- [ ] Set GitHub social preview through repository settings
- [ ] Rebuild and verify the mod-list icon and README rendering
