# 16: Launcher icon

Status: ready-for-agent
Closed: 2026-10-13
Category: enhancement

**What to build:** Replace the default launcher icon with a white music note on the app's default purple accent, as an adaptive icon (foreground + background layers, so round and squircle masks work) with a monochrome layer for Android 13+ themed icons. In-app icons are out of scope.

**Seams:** none automated; checked on a device or emulator

- [ ] The manifest points at the new adaptive icon (and round icon, if declared)
- [ ] Foreground, background and monochrome layers exist
- [ ] Icon looks right on the home screen under round and squircle masks, and as a themed icon

## Comments

- Decision (user, 2026-10-13): music note on purple, launcher icon only.
- Closed 2026-10-13: Merged in https://github.com/fong123452/MusicPlayer/pull/25. The on-device look (round/squircle masks, themed icon) was not checked.
