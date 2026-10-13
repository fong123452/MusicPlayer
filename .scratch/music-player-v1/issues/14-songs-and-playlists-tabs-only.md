# 14: Songs and Playlists tabs only

Status: ready-for-agent
Closed: 2026-10-13
Category: enhancement

**What to build:** The Home screen shows only the **Songs** and **Playlists** tabs. Remove the Albums and Artists tabs (this also ends the wrapped "Son gs" tab labels). Search is unchanged: it still returns Album and Artist sections, and their pages stay reachable from search results. Core Library grouping code stays.

**Seams:** Compose UI (instrumented)

- [ ] Home shows exactly two tabs, Songs and Playlists
- [ ] Search still lists Albums and Artists and opens their pages
- [ ] Tab labels fit on one line at normal phone width
- [ ] Tests that reached the Albums/Artists tabs are moved to the search path or removed

## Comments

- Decision (user, 2026-10-13): tabs only. Search sections and Album/Artist pages stay; GLOSSARY.md unchanged.

- Merged in https://github.com/fong123452/MusicPlayer/pull/21.
