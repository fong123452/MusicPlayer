# 08: Search

Status: ready-for-agent
Blocked by: 05

**What to build:** A search screen, reached from Home, with one box that matches Track titles, Album titles and Artist names. The match is a case-insensitive substring match over the current **Library**. Results are grouped under Tracks, Albums and Artists. Tapping a Track result plays it, and tapping an Album or Artist result opens its page.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Search matches substrings case-insensitively across Tracks, Albums and Artists, and returns grouped results (core test)
- [ ] An empty query returns no results; a query with no matches shows a "no results" message (UI test)
- [ ] Typing shows grouped results; tapping an Album result opens the Album page (UI test)
- [ ] Tapping a Track result starts playback of that Track

## Comments

Review decisions (2026-10-13):
- Search runs on the main thread: left as is (O2); revisit if jank is measured on a large library.
- Closing search clears the query (O1), so reopening starts fresh.
- Duplicated album/artist rows across Search, Albums and Artists screens: left (O2); extraction is out of scope.
- Trimmed query, Play next / Add to queue on search rows, and a Track tap queuing all matching Tracks: kept (O1), consistent with the other lists.
- Instrumented tests compile but were not run: no emulator or device was available. QA in a running app is still outstanding.
