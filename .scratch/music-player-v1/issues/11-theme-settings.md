# 11: Theme settings

Status: ready-for-agent
Closed: 2026-10-13
Blocked by: 01

**What to build:** A Settings screen, reached from Home, with **Theme** controls. The mode can be Light, Dark or Follow system, with Follow system as the default. The accent color comes from a fixed palette of 8. Changes apply immediately and are remembered across restarts. Each palette entry defines hand-tuned Material 3 light and dark color schemes. There's no dynamic color.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Settings defaults to Follow system and the first accent; changes persist in DataStore across instances (core test)
- [ ] Every palette entry meets 4.5:1 contrast for primary/onPrimary and surface/onSurface in both light and dark (pure JVM test over the palette definitions)
- [ ] Switching mode or accent in Settings recolors the app without a restart (UI test)

## Comments

- Review decision (2026-10-13), FollowSystem instrumented test: O1, accept as is. It selects Dark first so it catches a stuck-on-Dark bug; `isDark` is unit-tested, so the remaining gap (always-light on a light-mode device) is small.
- Review decision (2026-10-13), unrequested extras (`enableEdgeToEdge` with Theme-driven system bars; derived Material roles such as `outline = onSurface`): O1, keep. Removing them brings back unreadable status-bar icons and baseline-violet leaks under non-violet accents.
- Still to do by a human: run `ThemeSettingsTest` on a device/emulator (none available while implementing); QA click-through not run.

- Closed 2026-10-13: Merged in https://github.com/fong123452/MusicPlayer/pull/16.
