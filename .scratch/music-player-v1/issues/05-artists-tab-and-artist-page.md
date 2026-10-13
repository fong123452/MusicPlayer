# 05: Artists tab and Artist page

Status: ready-for-agent
Blocked by: 04

**What to build:** The Artists tab lists every **Artist**: the union of Track Artists and **Album Artists**, so "Various Artists" appears with its compilations. An Artist's page shows the Albums whose Album Artist is that Artist, plus every Track whose Artist is that Artist, including Tracks on compilations. Playing from the Artist page replaces the **Queue** with that Artist's Tracks.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] An Album Artist with no Tracks of their own still appears in the Artists list with their Albums (core test)
- [ ] A compilation Track is listed under its own Track Artist (core test)
- [ ] The Artists tab lists seeded Artists, and tapping one opens a page with their Albums and Tracks (UI test)
- [ ] Playing from the Artist page replaces the Queue with the Artist's Tracks (core test)

## Comments

- 2026-10-13, review decisions (user): (1) Album row duplicated between ArtistsScreen and AlbumsScreen stays as is until a third copy. (2) Play button stays hidden for an Artist with no Tracks. (3) No UI tests for stale-open-Artist clearing or Album-to-Artist back navigation; covered by the real-device pass. Fixed in 93c1a2a: Artist name independent of Track order, pluralised counts, glossary entry. Instrumented tests (ArtistsTabTest) not run: no emulator.
