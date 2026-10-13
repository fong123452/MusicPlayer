# 10: Edit Playlists and drop deleted Tracks

Status: ready-for-agent
Blocked by: 09

**What to build:** On a **Playlist**'s page, the user can drag to reorder Tracks, remove Tracks, rename the Playlist and delete it. When the **Library** refreshes and a Track has left it (the file was deleted from the phone), that Track is removed from every Playlist.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Reorder and remove change the Playlist's stored order (core test)
- [ ] Rename and delete Playlists (core test)
- [ ] A Library refresh where a Track is missing from TrackSource removes it from every Playlist (core test)
- [ ] Rename and delete from the Playlist page are reflected in the Playlists tab (UI test)
