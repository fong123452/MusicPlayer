# 06: Queue editing

Status: ready-for-agent
Blocked by: 03

**What to build:** Every Track row (in Songs, Album and Artist pages) has "Play next", which inserts the Track right after the current one, and "Add to queue", which appends it to the end. In Now Playing, the user can drag to reorder the **Queue** and remove Tracks, including the one that's playing.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] playNext inserts right after the current Track; addToEnd appends (core test)
- [ ] move(from, to) reorders the Queue and keeps the current Track playing (core test)
- [ ] Removing a Track before the current one keeps the same Track playing (core test)
- [ ] Removing the current Track moves playback to the next Track, or stops if none is left (core test)
- [ ] Play next and Add to queue from a Track row show up in Now Playing's Queue (UI test)
