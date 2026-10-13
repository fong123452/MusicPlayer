# 03: Now Playing and track navigation

Status: ready-for-agent
Blocked by: 02

**What to build:** Tapping the mini player opens the full Now Playing screen. It shows album art (or a placeholder), the title, the Artist, a seek bar, play/pause, next and previous, and the upcoming **Queue**. Previous restarts the current Track if it has played more than 3 seconds, and otherwise goes to the previous Track. When the last Track finishes, playback stops. If a file fails to play, the Queue skips to the next Track and a snackbar says "Couldn't play <title>, skipped". Next and previous from the notification, lock screen and headset go through the Queue's rules, via a ForwardingPlayer that intercepts them.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] previous() at position > 3 s restarts the current Track; at ≤ 3 s it moves to the previous Track (core test)
- [ ] next() on the last Track with nothing to repeat stops playback (core test)
- [ ] A playback error moves to the next Track and emits a one-shot "skipped" event carrying the Track's title (core test)
- [ ] The "skipped" snackbar shows when the fake engine reports an error (UI test)
- [ ] Now Playing shows the current Track, a working seek bar and the Queue list (UI test)
- [ ] The session player intercepts next and previous and routes them to Queue.next() and Queue.previous()
