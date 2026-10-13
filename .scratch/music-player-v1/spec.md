# Spec: Android Music Player v1

Category: enhancement

Vocabulary follows the repo-root `GLOSSARY.md`: **Track**, **Library**, **Album**, **Album Artist**, **Artist**, **Playlist**, **Queue**, **Shuffle Mode**, **Repeat Mode**, **Theme**, **EQ Preset**, **Bass Boost**.

## Problem Statement

I keep music files on my Android phone, but the players I have either clutter the list with ringtones and voice notes, split compilation albums into dozens of fragments, mix up "what's playing now" with my saved playlists, or don't let me make the app look and sound the way I want. I want one offline player that plays the formats on my phone, organizes them sensibly, keeps playing when the screen is off, and lets me customize its look and sound.

## Solution

A native Android app that scans the music already on the device and presents it as a **Library** browsable by Songs, Albums and Artists, with a single search. The user can build **Playlists**, play anything into a **Queue** they can edit on the fly, and control playback from the notification and lock screen. The user customizes the **Theme** (Light, Dark or Follow system, plus an accent color) and the sound (**EQ Preset** and **Bass Boost**).

## User Stories

### Library

1. As a listener, I want the app to find the music on my phone automatically, so that I don't have to import anything.
2. As a listener, I want ringtones, notification sounds and voice notes left out, so that my Library only contains music.
3. As a listener, I want audio shorter than 30 seconds left out, so that short clips don't clutter my Songs list.
4. As a listener, I want MP3, AAC/M4A, FLAC, OGG and WAV files to play, so that my existing collection works without converting it.
5. As a listener, I want the Library to refresh every time I open or return to the app, so that songs I just copied to the phone show up.
6. As a listener, I want to pull down to refresh the Library, so that I can pick up new files without restarting the app.
7. As a listener, I want to grant the audio-file permission once, with a clear explanation, so that I understand why the app needs it.
8. As a listener who denied the permission, I want a screen that explains why the app is empty and lets me grant the permission, so that I'm not stuck.
9. As a listener with no music on the phone, I want an empty-Library message, so that I know the app works and just found nothing.

### Browsing

10. As a listener, I want a Songs tab listing every Track alphabetically by title, so that I can find a specific Track.
11. As a listener, I want an Albums tab, so that I can play whole Albums.
12. As a listener, I want an Album to group its Tracks by album title and Album Artist, so that a compilation shows as one Album instead of many.
13. As a listener, I want an Album's Tracks in disc and track-number order, so that it plays the way the album was meant to be heard.
14. As a listener, I want an Artists tab, so that I can browse by who performed the music.
15. As a listener, I want each Track listed under its own Artist even when it's on a compilation, so that I can find every Track by an Artist.
16. As a listener, I want an Artist's page to show the Albums credited to them as Album Artist and every Track where they are the Track's Artist, so that I can play all of their music.
16a. As a listener, I want "Various Artists" (or any other Album Artist) to appear in the Artists tab with its Albums, so that compilations can be reached from the Artists tab.
17. As a listener, I want Tracks with missing tags to show as "Unknown Artist" or "Unknown Album" (and the file name when there's no title), so that untagged files are still reachable.
18. As a listener, I want to see the album art on Album lists, Track rows and Now Playing when the files have it, so that I can recognize music at a glance.

### Search

19. As a listener, I want one search box that matches Tracks, Albums and Artists, so that I can find anything from one place.
20. As a listener, I want search results grouped by Tracks, Albums and Artists, so that I can tell what kind of thing each result is.
21. As a listener, I want search to ignore upper- and lowercase, so that I don't have to type exactly.
22. As a listener, I want tapping a Track result to play it, and tapping an Album or Artist result to open its page, so that search leads straight to what I want.

### Playback and the Queue

23. As a listener, I want tapping a Track in a list to play it and fill the Queue with that list, so that playback continues through the rest of the list.
24. As a listener, I want playing an Album, Artist, Playlist or the whole Library to replace the Queue, so that I always know what's coming next.
25. As a listener, I want "Play next" on any Track, so that I can slot it in right after the current Track.
26. As a listener, I want "Add to queue" on any Track, so that I can append it to the end.
27. As a listener, I want to reorder the Queue by dragging in Now Playing, so that I can change what plays next.
28. As a listener, I want to remove a Track from the Queue, so that I can skip it ahead of time.
29. As a listener, I want editing the Queue never to change a Playlist, so that my saved Playlists stay as I made them.
30. As a listener, I want play, pause, next, previous and seek controls, so that I can control playback.
31. As a listener, I want "previous" to restart the current Track if it has played more than about 3 seconds, and otherwise go to the previous Track, so that it works like other players.
32. As a listener, I want a mini player above the tabs while something is playing, so that I can control playback from anywhere in the app.
33. As a listener, I want a full Now Playing screen with art, title, Artist, a seek bar and the Queue, so that I can see and control everything about the current Track.
34. As a listener, I want to turn Shuffle Mode on and off, so that I can hear the Queue in random order.
35. As a listener, I want turning Shuffle Mode on to keep the current Track playing and shuffle only the rest, so that playback isn't interrupted.
36. As a listener, I want turning Shuffle Mode off to go back to the original Queue order from the current Track, so that I can return to album order.
36a. As a listener, I want Tracks I "Play next" or "Add to queue" while shuffled to keep their place when I turn Shuffle Mode off (right after the current Track, or at the end), so that un-shuffling doesn't lose or misplace them.
37. As a listener, I want the Repeat button to cycle Off → All → One, so that I can loop a Track or the whole Queue.
37a. As a listener in Repeat One, I want pressing Next to still move to the next Track, so that I'm never stuck on one Track.
38. As a listener with Repeat Mode Off, I want playback to stop at the end of the Queue, so that it doesn't loop unexpectedly.
39. As a listener, I want music to keep playing when I leave the app or turn off the screen, so that I can use my phone normally.
40. As a listener, I want play/pause/next/previous in the notification and on the lock screen, so that I don't have to open the app.
41. As a listener, I want headset and Bluetooth buttons to control playback, so that I can control it from my headphones.
42. As a listener, I want playback to pause when I unplug headphones or Bluetooth disconnects, so that music doesn't suddenly blast from the speaker.
43. As a listener, I want playback to pause or duck for phone calls and other apps' audio, so that the app behaves politely.
44. As a listener, I want an unplayable or deleted file in the Queue to be skipped, with a short message ("Couldn't play <title>, skipped"), so that one bad file doesn't stop playback.

### Playlists

45. As a listener, I want to create a named Playlist, so that I can save groups of Tracks.
46. As a listener, I want to add a Track (or a whole Album) to a Playlist from any list, so that building Playlists is quick.
47. As a listener, I want adding a Track that's already in a Playlist to do nothing (with a short confirmation), so that Playlists never contain duplicates.
48. As a listener, I want to reorder Tracks in a Playlist by dragging, so that I control its order.
49. As a listener, I want to remove Tracks from a Playlist, so that I can curate it.
50. As a listener, I want to rename and delete Playlists, so that I can manage them.
51. As a listener, I want Playlists to survive closing the app and restarting the phone, so that my work isn't lost.
52. As a listener, I want Tracks deleted from my phone to disappear from my Playlists, so that Playlists never show Tracks that can't play.
53. As a listener, I want a Playlists tab on Home listing my Playlists with their Track counts, so that I can find and play them.

### Theme

54. As a listener, I want to choose Light, Dark or Follow system, so that the app matches my preference.
55. As a listener, I want Follow system to be the default, so that the app matches my phone's night schedule out of the box.
56. As a listener, I want to pick an accent color from a palette of about 8, so that the app feels like mine.
57. As a listener, I want every accent color to stay readable in both Light and Dark, so that no choice makes the app hard to use.
58. As a listener, I want Theme changes to apply immediately and to be remembered, so that I see the effect and don't have to set it again.

### Sound

59. As a listener, I want to choose an EQ Preset from the ones my device provides, so that I can shape the sound.
60. As a listener, I want an option to turn the equalizer off, so that I can hear the music flat.
61. As a listener, I want to turn Bass Boost on and off, so that I can add low end when I want it.
62. As a listener, I want my EQ Preset and Bass Boost choices remembered and applied to every Track, so that the sound stays consistent.
63. As a listener on a device without equalizer support, I want the Sound settings to say so instead of failing, so that I'm not confused.

## Implementation Decisions

- **Platform**: Kotlin with Jetpack Compose and Material 3. Min SDK 26, target and compile against the latest stable SDK. Single-activity app with Compose Navigation. Build with Gradle Kotlin DSL and a version catalog.
- **Architecture**: the UI layer (Compose screens and ViewModels) talks only to the **player core**. The player core owns every domain rule and depends on the Android world only through three ports, each with a real adapter and a fake for tests:
  - **TrackSource**: returns the current set of raw Track records from the device. The real adapter queries MediaStore audio with `IS_MUSIC = 1` and duration ≥ 30,000 ms, reading id, content URI, title, artist, album, album artist, album id, disc/track number, duration and display name. It needs `READ_MEDIA_AUDIO` on API 33+ and `READ_EXTERNAL_STORAGE` below that.
  - **PlaybackEngine**: plays one Track at a time: load(uri), play, pause, seek. It reports position, playing state, natural completion and playback errors. The real adapter wraps a Media3 ExoPlayer that lives in a Media3 `MediaSessionService`, which provides the foreground notification, lock-screen controls, headset buttons, audio focus (`handleAudioFocus = true`) and becoming-noisy pause (`setHandleAudioBecomingNoisy(true)`). The Queue alone decides what plays next. The session's player is a `ForwardingPlayer` around ExoPlayer that intercepts next and previous (from the notification, lock screen and headset) and routes them to the Queue's next() and previous(). This way the 3 s previous rule, Shuffle Mode and Repeat Mode apply the same way to every control. ExoPlayer's own repeat and shuffle stay off.
  - **SoundEffects**: lists EQ Presets, applies a preset or none, toggles Bass Boost, and reports whether each is supported. The real adapter uses `android.media.audiofx.Equalizer` and `BassBoost` attached to the player's audio session id. It re-attaches when the session id changes and reports unsupported when constructing the effect fails.
- **Player core modules** (pure Kotlin, no Android types in their interfaces):
  - **Library**: built from TrackSource. It applies the tag fallbacks (Unknown Artist, Unknown Album, file name as title), groups Albums by (album title, Album Artist). When a Track has no Album Artist tag, it uses MediaStore's album id as the grouping key instead, so untagged compilations still form one Album. The Artists list is the union of Track Artists and Album Artists. An Artist's page holds the Albums whose Album Artist is that Artist, plus every Track whose Artist is that Artist. It sorts Tracks by title, and an Album's Tracks by disc then track number. It exposes `refresh()` and an observable snapshot of Tracks, Albums and Artists. Each refresh also prunes Tracks that left the Library from every Playlist.
  - **Search**: a case-insensitive substring match over Track title, Album title and Artist name, returning grouped results from the current Library snapshot.
  - **Queue**: the ordered Track list, the original order (for un-shuffling), the current index, Shuffle Mode and Repeat Mode. Operations: replace(tracks, startIndex), playNext(track), addToEnd(track), move(from, to), remove(index), next(), previous(positionMs), setShuffle(on), cycleRepeat(). Its rules:
    - Shuffling keeps the current Track first and shuffles the rest. Un-shuffling restores the original relative order and stays on the current Track.
    - While shuffled, playNext inserts right after the current Track in both the shuffled and the original order, and addToEnd appends to both. move and remove change the shuffled order. remove also removes the Track from the original order.
    - previous restarts the Track if position > 3 s.
    - cycleRepeat goes Off → All → One → Off. Repeat One repeats on natural completion only, and an explicit next still advances. Repeat All wraps. Off stops at the end.
    - The Queue drives PlaybackEngine. A playback error advances to the next Track and emits a one-shot "skipped" event carrying the Track's title, which the UI shows as a snackbar.
  - **Playlists**: create, rename, delete, add tracks (ignoring duplicates and reporting which were already there), remove, move, and prune missing Tracks. Stored in Room: a `playlist(id, name, createdAt)` table and a `playlist_entry(playlistId, trackId, position)` table with primary key (playlistId, trackId), which enforces no duplicates. Playlists reference Tracks by MediaStore id.
  - **Settings**: Theme mode (Light, Dark, FollowSystem; default FollowSystem), accent (one of a fixed enum of 8 palette entries), EQ Preset (a device preset index or none) and Bass Boost (on/off). Stored in Jetpack DataStore (Preferences).
- **Theme**: each of the 8 accent entries defines a hand-tuned Material 3 light and dark color scheme. Each scheme must meet WCAG AA contrast (4.5:1) for primary/onPrimary and surface/onSurface. No Material You dynamic color.
- **Album art**: loaded with Coil from the Track's MediaStore album-art URI (`ContentResolver.loadThumbnail` on API 29+, the `albumart` content URI below that). A placeholder icon shows when there's no art.
- **Library refresh**: runs every time the app comes to the foreground (the process lifecycle's ON_START, once the permission is granted) and on pull-to-refresh. No ContentObserver.
- **Screens**: Permission gate; Home with four tabs (Songs, Albums, Artists, Playlists) plus a mini player; Album detail; Artist detail; Playlist detail (reorder, remove, rename, delete); Search; Now Playing (controls, Shuffle, Repeat, Queue reorder and remove); Settings (Theme mode, accent, EQ Preset, Bass Boost).
- **Dependency injection**: Hilt, so the tests can swap in the fake ports.
- **Persistence scope**: only Playlists and Settings persist. The Queue and playback position are in memory only.

## Testing Decisions

- Good tests check behavior through a seam's public interface, using inputs a user could produce, and assert on observable outcomes (what's in the Queue, what plays next, what a Playlist contains, what's on screen). They never assert on private state, call order, or how Compose builds its tree.
- **Seam 1: the player core (JVM unit tests)**. Exercise Library, Search, Queue, Playlists and Settings through the player core interface, with a fake TrackSource (an in-memory list of raw Track records), a fake PlaybackEngine (records what it was asked to load and lets the test fire "completed" and "error" events) and a fake SoundEffects (configurable presets and support). Playlists use an in-memory Room database (run under Robolectric), and Settings use a DataStore in a temp file. Filtering by IS_MUSIC and duration is the TrackSource adapter's job, so the core trusts its input. Cases to cover:
  - tag fallbacks
  - compilation grouping by Album Artist, and the MediaStore album-id fallback when Album Artist is missing
  - an Album Artist appearing in Artists, and a compilation Track listed under its own Artist
  - Album Track ordering
  - search matching and grouping
  - replace, play next and add to end
  - move and remove, including removing the current Track
  - shuffle on and off keeping the current Track
  - play next and add to end while shuffled surviving un-shuffle
  - previous with the 3 s rule
  - the Repeat cycle order
  - Repeat Off, One and All at the end of the Queue, and explicit next under Repeat One
  - error skip and its "skipped" event
  - Playlist create, rename and delete
  - Playlist duplicate add ignored
  - Playlist reorder and remove
  - prune on refresh
  - Queue edits never touching a Playlist
  - Settings default and persistence
  - equalizer unsupported reported
  - every accent palette entry meeting 4.5:1 contrast in both light and dark (a pure computation over the palette definitions)
- **Seam 2: Compose UI (instrumented tests on an emulator)**. Launch the app's screens with Hilt test bindings that inject the same fakes. Cover:
  - the permission-denied screen
  - an empty Library
  - Songs, Albums and Artists tabs showing seeded data
  - tapping a Track starts the mini player
  - Play next / Add to queue reflected in Now Playing
  - search showing grouped results
  - creating a Playlist and adding a Track to it
  - Theme mode and accent switching applied immediately
  - the Settings equalizer section showing "not supported"
  - a playback error showing the "skipped" snackbar
  - the art placeholder showing for a Track without art
- **Not automated, verified by a manual QA pass on a real device**: the MediaStore filter with real ringtones and voice notes present, real album art loading, notification and headset next/previous following the Queue's rules, background playback with the screen off, notification and lock-screen controls, headset buttons, becoming-noisy pause, audio focus during a call, audible effect of EQ Presets and Bass Boost, and playback of each listed format.
- **Prior art**: none, because this is a new repository. The first tests on each seam set the pattern.

## Out of Scope

- Streaming or any online source, accounts, cloud sync
- Folder browsing and excluded-folder settings
- Manual equalizer bands and custom saved presets
- Sleep timer
- Restoring the Queue and position after an app restart
- Material You dynamic color and a free color picker
- Playlist import/export (.m3u)
- Live Library updates via a ContentObserver
- Formats beyond Android's native decoders (APE, WMA, etc.)
- Lyrics, tag editing, home-screen widgets, Android Auto, Chromecast, iOS

## Further Notes

- Playlists reference Tracks by MediaStore id. If MediaStore re-indexes and assigns new ids (for example after a factory reset or some SD card remounts), those entries get pruned, as Q10 decided ("drop silently"). This is an accepted limitation for v1.
- Because the Queue isn't persisted, killing the app clears it. That's an accepted limitation, and restoring it is a likely v2 item.
