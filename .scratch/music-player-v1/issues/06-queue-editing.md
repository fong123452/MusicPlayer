# 06: Queue editing

Status: ready-for-agent
Closed: 2026-10-13
Blocked by: 03

**What to build:** Every Track row (in Songs, Album and Artist pages) has "Play next", which inserts the Track right after the current one, and "Add to queue", which appends it to the end. In Now Playing, the user can drag to reorder the **Queue** and remove Tracks, including the one that's playing.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] playNext inserts right after the current Track; addToEnd appends (core test)
- [ ] move(from, to) reorders the Queue and keeps the current Track playing (core test)
- [ ] Removing a Track before the current one keeps the same Track playing (core test)
- [ ] Removing the current Track moves playback to the next Track, or stops if none is left (core test)
- [ ] Play next and Add to queue from a Track row show up in Now Playing's Queue (UI test)

## Comments

- Review decisions (user): drag-reorder has no auto-scroll and no Compose drag test; extract the offset-to-index maths into a unit-tested function and defer auto-scroll (candidate for ticket 13, manual check on a real phone). Cosmetic P3s left as is: bundling onPlayNext/onAddToQueue, duplicated guards in playNext/addToEnd, mixed-behaviour tests, no Album/Artist-page UI test for Play next.
- Instrumented tests (QueueEditingTest) were built but never run: no emulator/device was available.
- Follow-up review fixes: Queue now tracks play intent (engine.isPlaying is false while buffering) so removing the current Track right after a skip keeps playing; removing the current last Track now leaves the last remaining Track current and paused instead of an out-of-range index, so the Queue stays reachable (appendWhileStopped dropped). Queue-flag-reset UI test added (built, not run: no device).
- Review decisions (user, round 3): Q3 leave Queue's wantsPlay/reportedPlaying ordering against an asynchronous engine untested with the fake; verify on a real phone in ticket 13. Q4 leave skipCurrent starting playback when a paused Queue's next Track is unreadable (fix risks stopping normal broken-Track skips; needs a device to verify). Also left: the "last remaining Track stays current when nothing follows" rule is recorded here only, with no UI-level test.

- Closed 2026-10-13: Merged in https://github.com/fong123452/MusicPlayer/pull/6.
