# 12: Sound settings

Status: ready-for-agent
Blocked by: 02, 11

**What to build:** A Sound section in Settings. The user can choose an **EQ Preset** from the device's list or "Off", and toggle **Bass Boost**. The choices are remembered and applied to every Track. On a device where the equalizer or Bass Boost isn't supported, the section says so instead of failing. This adds the **SoundEffects** port and its fake, plus a real adapter using android.media.audiofx Equalizer and BassBoost attached to the player's audio session id. The adapter re-attaches when the session id changes, and reports unsupported when creating an effect fails.

**Seams:** player core (JVM), Compose UI (instrumented)

- [ ] The selected EQ Preset and Bass Boost persist and are applied through SoundEffects when playback starts (core test with a fake SoundEffects)
- [ ] Choosing "Off" disables the equalizer (core test)
- [ ] Unsupported effects are reported, and Settings shows "not supported" (core test and UI test)
- [ ] The preset list in Settings comes from SoundEffects (UI test)

## Comments

- Review decision (2026-10-13), untested adapter logic: O1. `AndroidSoundEffects` now sits on a small `EffectFactory` seam and is unit tested in `app/src/test` (session re-attach, unsupported on a failed create, dead effect dropped, unknown preset = off). Reason: that is where the crash risk is.
- Review decision (2026-10-13), `themeDataStore` name and `Thread.sleep` polling in `SoundTest`: O2, keep. Style only, no wrong behaviour.
- Still to do by a human: run `SoundSettingsTest` on a device/emulator and listen on a real phone (none available while implementing); QA click-through not run.
