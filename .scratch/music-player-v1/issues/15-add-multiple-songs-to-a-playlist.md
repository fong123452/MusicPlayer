# 15: Add multiple songs to a Playlist

Status: ready-for-agent
Closed: 2026-10-13
Category: enhancement
Blocked by: 14

**What to build:** On a **Playlist**'s page, an "Add songs" button opens a checklist of every Track in the **Library**, with search. The user ticks several Tracks and taps Done; they are appended to the Playlist in the order they appear in the list. Tracks already in the Playlist show ticked and locked, so a Track can't be added twice. The existing per-Track "Add to playlist" menu stays.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Adding a set of Tracks appends the ones not yet present, in order, and ignores duplicates (core test)
- [ ] Add songs opens a searchable checklist of the whole Library (UI test)
- [ ] Tracks already in the Playlist are ticked and cannot be unticked (UI test)
- [ ] After Done, the Playlist page and the Playlists tab song count show the new Tracks (UI test)
- [ ] Done with nothing ticked changes nothing

## Comments

- Decisions (user, 2026-10-13): entry point is a button on the Playlist page (not Songs-tab multi-select); already-added Tracks shown ticked and locked. The user's word "folder" means Playlist.
- Closed 2026-10-13: Merged in https://github.com/fong123452/MusicPlayer/pull/23.
