# Music Player

An Android app that plays audio files already stored on the user's device, and lets the user customize how it looks and sounds.

## Language

### Library

**Track**:
One audio file on the device that Android marks as music and that is at least 30 seconds long, along with its tags (title, artist, album, album artist, duration). Ringtones, notification sounds and voice notes are not Tracks.
_Avoid_: Song, file, media item

**Library**:
Every Track the app found on the device the last time it scanned.
_Avoid_: Collection, catalog

**Album**:
A group of Tracks that share the same album title and Album Artist. When Tracks have no Album Artist, the Album follows the device's own album grouping.
_Avoid_: Record, release

**Album Artist**:
The artist an Album is credited to as a whole. For a compilation, this is often "Various Artists", even though each Track has its own Artist.

**Artist**:
A group of Tracks that share the same artist tag.
_Avoid_: Performer, band

**Playlist**:
A named, ordered list of Tracks that the user creates and keeps. A Track appears in it at most once.
_Avoid_: Mix, list, set

### Playback

**Queue**:
The ordered list of Tracks that will play now. Starting playback copies Tracks into it, and changes to the Queue never change a Playlist.
_Avoid_: Now Playing list, current playlist

**Shuffle Mode**:
Whether the Queue plays in random order (on) or in the order it was given (off).

**Repeat Mode**:
What happens when a Track or the Queue ends: Off, One (repeat the current Track), or All (repeat the whole Queue).
_Avoid_: Loop

### Customization

**Theme**:
The app's look: a mode (Light, Dark, or Follow system) plus one accent color from a fixed palette.
_Avoid_: Skin, style

**EQ Preset**:
One of the equalizer settings built into the device that the user can choose, such as "Rock" or "Jazz".
_Avoid_: Sound profile

**Bass Boost**:
A setting that strengthens low frequencies. It can be on or off.

## Relationships

- An **Album** and an **Artist** each contain one or more **Tracks**
- A **Track** belongs to exactly one **Album** and one **Artist**, taken from its tags
- An **Album** is listed under its **Album Artist**. Each of its **Tracks** is also listed under that Track's own **Artist**
- A **Playlist** holds Tracks from the **Library**. When a Track leaves the Library, it is removed from every Playlist
- Playing a **Playlist**, **Album**, **Artist** or the **Library** replaces the **Queue** with a copy of its Tracks
- "Play next" inserts a **Track** right after the current one in the **Queue**. "Add to queue" appends it to the end
