# 02: Play a Track in the background

Status: ready-for-agent
Blocked by: 01

**What to build:** Tapping a Track in the Songs list replaces the **Queue** with that list and starts playing from the tapped Track. A mini player appears above the tabs with the title, Artist and play/pause. Playback keeps going when the user leaves the app or turns off the screen. It can be controlled from the notification and lock screen (play/pause), and from headset and Bluetooth buttons. It pauses when headphones unplug or Bluetooth disconnects, and it pauses or ducks for calls and other apps' audio. This adds the **PlaybackEngine** port (one Track at a time: load, play, pause, seek, plus completion and error events), its fake, and a real adapter: ExoPlayer inside a Media3 MediaSessionService, with audio focus and becoming-noisy handling turned on. When a Track finishes on its own, the Queue moves to the next one.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] Queue.replace(tracks, startIndex) sets the current Track and loads it into PlaybackEngine (core test with a fake engine)
- [ ] When a Track finishes on its own, the Queue moves to and loads the next Track (core test)
- [ ] Tapping a Track shows the mini player with that Track, and play/pause toggles its state (UI test)
- [ ] Playback runs in a foreground MediaSessionService with a media notification
- [ ] ExoPlayer's own repeat and shuffle stay off, because the Queue decides what plays next
