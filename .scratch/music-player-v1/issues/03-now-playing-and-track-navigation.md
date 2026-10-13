# 03: Now Playing and track navigation

Status: ready-for-agent
Closed: 2026-10-13
Blocked by: 02

**What to build:** Tapping the mini player opens the full Now Playing screen. It shows album art (or a placeholder), the title, the Artist, a seek bar, play/pause, next and previous, and the upcoming **Queue**. Previous restarts the current Track if it has played more than 3 seconds, and otherwise goes to the previous Track. When the last Track finishes, playback stops. If a file fails to play, the Queue skips to the next Track and a snackbar says "Couldn't play <title>, skipped". Next and previous from the notification, lock screen and headset go through the Queue's rules, via a ForwardingPlayer that intercepts them.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] previous() at position > 3 s restarts the current Track; at ≤ 3 s it moves to the previous Track (core test)
- [ ] next() on the last Track with nothing to repeat stops playback (core test)
- [ ] A playback error moves to the next Track and emits a one-shot "skipped" event carrying the Track's title (core test)
- [ ] The "skipped" snackbar shows when the fake engine reports an error (UI test)
- [ ] Now Playing shows the current Track, a working seek bar and the Queue list (UI test)
- [ ] The session player intercepts next and previous and routes them to Queue.next() and Queue.previous()

## Comments

Implementation on branch `feat/03-now-playing` (commits a683d02, 4a0a5cb, a7d0612). Instrumented UI tests are compiled but not run (no emulator); QA on a real device still outstanding. Code review decisions (user chose O1 on all four):

- **Album art**: stays a placeholder here; real art loading is deferred to the Albums work (ticket 04), where Coil is introduced.
- **Queue list**: shows the whole Queue with the current Track in bold, so reorder/remove (ticket 06) works on the full list.
- **Stale error attribution**: accepted. A late ExoPlayer error arriving right after a manual next can skip a healthy Track; the window is tiny.
- **Adapter tests**: `QueueForwardingPlayer` and `ControllerPlaybackEngine.currentPositionMs` get no automated test; covered by the manual real-device check (ticket 13).

Closed 2026-10-13: merged in fong123452/MusicPlayer#3 (squash), merged at the owner's explicit request while the `gate` CI check was still in progress. Instrumented tests and real-device QA are still outstanding (see ticket 13).
