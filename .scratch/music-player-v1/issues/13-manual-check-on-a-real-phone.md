# 13: Manual check on a real phone

Status: ready-for-human
Blocked by: 07, 08, 10, 12

**What to build:** Nothing new. Check on a real Android device the behavior that the automated seams can't reach, as listed in the spec's Testing Decisions. File a bug ticket for each failure.

- [ ] Ringtones, notification sounds, voice notes and clips under 30 s don't appear in the Library
- [ ] MP3, AAC/M4A, FLAC, OGG and WAV each play
- [ ] Real album art loads on Albums, Track rows and Now Playing
- [ ] Playback continues with the screen off and the app in the background
- [ ] Notification and lock-screen controls work, and next/previous follow the 3 s rule, Shuffle Mode and Repeat Mode
- [ ] Headset and Bluetooth buttons control playback
- [ ] Unplugging headphones or disconnecting Bluetooth pauses playback
- [ ] An incoming call pauses playback, and another app's audio pauses or ducks it
- [ ] EQ Presets and Bass Boost audibly change the sound
- [ ] Each Theme accent looks right in Light and Dark
