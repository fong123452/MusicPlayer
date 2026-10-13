# 10: Edit Playlists and drop deleted Tracks

Status: ready-for-agent
Closed: 2026-10-13
Blocked by: 09

**What to build:** On a **Playlist**'s page, the user can drag to reorder Tracks, remove Tracks, rename the Playlist and delete it. When the **Library** refreshes and a Track has left it (the file was deleted from the phone), that Track is removed from every Playlist.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Reorder and remove change the Playlist's stored order (core test)
- [ ] Rename and delete Playlists (core test)
- [ ] A Library refresh where a Track is missing from TrackSource removes it from every Playlist (core test)
- [ ] Rename and delete from the Playlist page are reflected in the Playlists tab (UI test)

## Comments

- Review decisions (user, 2026-10-13):
  - Q1 prune guard: keep. `Playlists.pruneOnRefresh` skips a Library that scanned as empty, so a glitchy scan cannot wipe Playlists. Accepted trade-off: a partial scan (e.g. an ejected SD card) still permanently removes the entries of the Tracks it missed.
  - Q2 duplicated row in `PlaylistDetailScreen`/`NowPlayingScreen`: leave as is, since the rows differ slightly and the UI refactor cannot be verified without a device.
  - Q3 pruning started from the Hilt provider in `AppModule`: leave as is.
  - Q4 `HomeScreen` open-detail state: leave as is.
- Instrumented tests (`PlaylistsTabTest` additions, the `ReorderableList` extraction behind `QueueEditingTest`/`NowPlayingTest`) compile but were not run: no emulator or device was available. QA in a running app is outstanding.
- Not covered by an automated test: the Hilt wiring of `pruneOnRefresh`, and the drag gesture itself.

Closed: merged in https://github.com/fong123452/MusicPlayer/pull/14.
