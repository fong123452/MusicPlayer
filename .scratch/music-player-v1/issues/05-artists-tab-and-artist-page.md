# 05: Artists tab and Artist page

Status: ready-for-agent
Blocked by: 04

**What to build:** The Artists tab lists every **Artist**: the union of Track Artists and **Album Artists**, so "Various Artists" appears with its compilations. An Artist's page shows the Albums whose Album Artist is that Artist, plus every Track whose Artist is that Artist, including Tracks on compilations. Playing from the Artist page replaces the **Queue** with that Artist's Tracks.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] An Album Artist with no Tracks of their own still appears in the Artists list with their Albums (core test)
- [ ] A compilation Track is listed under its own Track Artist (core test)
- [ ] The Artists tab lists seeded Artists, and tapping one opens a page with their Albums and Tracks (UI test)
- [ ] Playing from the Artist page replaces the Queue with the Artist's Tracks (core test)
