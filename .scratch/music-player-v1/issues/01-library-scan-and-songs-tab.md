# 01: Library scan and Songs tab

Status: ready-for-agent

**What to build:** The first runnable app. On launch, the user is asked for permission to read audio with a clear explanation. If they deny it, a screen explains why the app is empty and offers to grant the permission. Once granted, the app builds the **Library** from the device's music. A **Track** is a file Android marks as music that is at least 30 seconds long. The Songs tab lists every Track alphabetically by title, and a message appears when the Library is empty. The Library refreshes whenever the app comes to the foreground, and on pull-to-refresh. This ticket also sets up the project and both test harnesses that later tickets build on: Kotlin, Compose, Material 3, Hilt, min SDK 26, the player core with its **TrackSource** port and fake, JVM tests, and instrumented Compose tests using Hilt test bindings.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] The app builds and installs on API 26+ and targets the latest stable SDK
- [ ] The TrackSource adapter queries MediaStore with IS_MUSIC = 1 and duration ≥ 30,000 ms, using READ_MEDIA_AUDIO on API 33+ and READ_EXTERNAL_STORAGE below that
- [ ] Library applies tag fallbacks: "Unknown Artist", "Unknown Album", and the file name when there's no title (core test)
- [ ] The Songs tab lists Tracks sorted by title, case-insensitively (core test and UI test with a fake TrackSource)
- [ ] The permission-denied screen shows and its grant action requests the permission again (UI test)
- [ ] The empty-Library message shows when TrackSource returns nothing (UI test)
- [ ] The Library refreshes on ON_START of the process lifecycle and on pull-to-refresh, with no ContentObserver
- [ ] Home has the tab structure ready for Songs, Albums, Artists and Playlists (the other tabs can be placeholders)
