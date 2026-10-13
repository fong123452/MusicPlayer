# 08: Search

Status: ready-for-agent
Blocked by: 05

**What to build:** A search screen, reached from Home, with one box that matches Track titles, Album titles and Artist names. The match is a case-insensitive substring match over the current **Library**. Results are grouped under Tracks, Albums and Artists. Tapping a Track result plays it, and tapping an Album or Artist result opens its page.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Search matches substrings case-insensitively across Tracks, Albums and Artists, and returns grouped results (core test)
- [ ] An empty query returns no results; a query with no matches shows a "no results" message (UI test)
- [ ] Typing shows grouped results; tapping an Album result opens the Album page (UI test)
- [ ] Tapping a Track result starts playback of that Track
