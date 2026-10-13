# 09: Create and play Playlists

Status: ready-for-agent
Blocked by: 04

**What to build:** The Playlists tab on Home lists the user's **Playlists** with their Track counts. The user can create a named Playlist, and add a Track or a whole Album to it from any list via "Add to playlist". Adding a Track that's already in the Playlist does nothing and shows a short confirmation saying so. Playing a Playlist replaces the **Queue** with a copy of its Tracks. Editing the Queue never changes the Playlist. Playlists are stored in Room (`playlist` and `playlist_entry`, with the primary key (playlistId, trackId)) and survive restarts.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Create a Playlist and add Tracks; it persists across a new player core instance (core test with Room under Robolectric)
- [ ] Adding a Track already in the Playlist is ignored and reported as already there (core test)
- [ ] Adding an Album adds its Tracks in Album order, skipping duplicates (core test)
- [ ] Playing a Playlist, then reordering or removing Queue entries, leaves the Playlist unchanged (core test)
- [ ] Create a Playlist in the UI, add a Track, and see it in the Playlists tab with count 1 (UI test)
