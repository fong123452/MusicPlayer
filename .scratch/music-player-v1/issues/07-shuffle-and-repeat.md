# 07: Shuffle and Repeat

Status: ready-for-agent
Blocked by: 06

**What to build:** Now Playing gets a **Shuffle Mode** toggle and a **Repeat Mode** button. Turning Shuffle on keeps the current Track playing and shuffles the rest. Turning it off goes back to the original order and stays on the current Track. Tracks added with "Play next" or "Add to queue" while shuffled keep their place (right after the current Track, or at the end) when Shuffle is turned off. The Repeat button cycles Off → All → One. Under One, a Track repeats when it finishes on its own, but pressing Next still moves on. All wraps from the last Track to the first. Off stops at the end. The notification and headset controls follow the same rules.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Shuffle on keeps the current Track first and shuffles the rest; shuffle off restores the original order and stays on the current Track (core test, with a seeded random)
- [ ] playNext and addToEnd while shuffled keep their place after un-shuffle; remove while shuffled also removes from the original order (core test)
- [ ] cycleRepeat goes Off → All → One → Off (core test)
- [ ] Repeat One reloads the same Track when it finishes on its own; explicit next() still advances (core test)
- [ ] Repeat All wraps from the last Track to the first; Off stops at the end (core test)
- [ ] The Shuffle and Repeat controls show their current state in Now Playing (UI test)

## Comments

2026-10-13, code review decisions (user chose O1 on all three):
- Removing the playing last Track under Repeat All keeps the existing behavior (pause on the previous Track, per ticket 06's `remove` doc). Reason: not asked for by this ticket.
- `playNext`/`addToEnd` duplication left as is. Reason: only two callers.
- `repeatLabel` keeps the enum's `toString`. Reason: not worth an explicit mapping yet.

Open: the Compose UI test (`NowPlayingTest`) and QA have not run on a device; none was available.
