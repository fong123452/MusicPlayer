# 11: Theme settings

Status: ready-for-agent
Blocked by: 01

**What to build:** A Settings screen, reached from Home, with **Theme** controls. The mode can be Light, Dark or Follow system, with Follow system as the default. The accent color comes from a fixed palette of 8. Changes apply immediately and are remembered across restarts. Each palette entry defines hand-tuned Material 3 light and dark color schemes. There's no dynamic color.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Settings defaults to Follow system and the first accent; changes persist in DataStore across instances (core test)
- [ ] Every palette entry meets 4.5:1 contrast for primary/onPrimary and surface/onSurface in both light and dark (pure JVM test over the palette definitions)
- [ ] Switching mode or accent in Settings recolors the app without a restart (UI test)
