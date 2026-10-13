# 04: Albums tab and Album page

Status: ready-for-agent
Blocked by: 02

**What to build:** The Albums tab lists every **Album** with its art, and tapping one opens its page. Albums are grouped by album title plus **Album Artist**, so a compilation shows as one Album. When Tracks have no Album Artist tag, the Album uses MediaStore's album id as its grouping key instead. An Album's Tracks are listed by disc number, then track number. Playing the Album, or tapping one of its Tracks, replaces the **Queue** with the Album's Tracks. Album art is loaded with Coil from MediaStore's album art (loadThumbnail on API 29+, the albumart content URI below that), with a placeholder when there's none. Track rows and the mini player show art too.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Tracks with the same album title and Album Artist but different Track Artists form one Album (core test)
- [ ] Tracks with no Album Artist group by MediaStore album id (core test)
- [ ] An Album's Tracks sort by disc, then track number (core test)
- [ ] The Albums tab lists seeded Albums, and tapping one opens its page (UI test)
- [ ] Playing from the Album page replaces the Queue with the Album's Tracks in order (core test)
- [ ] A placeholder shows for a Track or Album without art (UI test)
