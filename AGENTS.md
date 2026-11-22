# AGENTS.md — StreamLine TV handoff

This file is the working guide for anyone (human or AI agent) continuing work on
StreamLine TV. Read it before changing code, and update it when architecture or
setup changes.

---

## 1. What this project is

**StreamLine TV** is an ad-free, remote-first home-screen launcher for Amazon
Fire TV (and eventually Android TV / Google TV). It replaces the busy Fire TV
home screen with a calm surface: a large clock, local weather, bundled
photographic nature backgrounds, and one row of app tiles that launch installed
streaming apps.

Current status: **v0.1.0 — first functional build**, compiling and validated on
a TCL Fire TV for launch, focus, app tiles, settings, and exit behaviour. HOME
button replacement remains device/Fire OS dependent.

Primary constraints from the README, in priority order:

1. Simplicity
2. Performance (runs on low-end Fire TV sticks)
3. Reliability (weather/network failures must never break the launcher)
4. Customization

No ads, no analytics, no telemetry, no sponsored rows.

---

## 2. Repository layout

```
StreamLine/
├── AGENTS.md                  ← this file
├── CHANGELOG.md               ← keep-a-changelog history
├── README.md                  ← public project description
├── build.gradle.kts           ← root Gradle config
├── settings.gradle.kts
├── gradle/libs.versions.toml  ← single source of dependency versions
├── gradle/wrapper/            ← Gradle 9.7.1 wrapper
├── local.properties           ← machine-specific SDK path (git-ignored)
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/streamline/tv/
        │   ├── MainActivity.kt
        │   ├── StreamlineApplication.kt   ← AppContainer (manual DI)
        │   ├── data/
        │   │   ├── AppSettings.kt         ← settings model + enums
        │   │   ├── SettingsRepository.kt  ← Preferences DataStore
        │   │   ├── AppsRepository.kt      ← PackageManager discovery/launch
        │   │   └── weather/
        │   │       ├── Http.kt
        │   │       ├── WeatherModels.kt
        │   │       ├── WeatherRepository.kt
        │   │       └── LocationResolver.kt
        │   └── ui/
        │       ├── StreamlineApp.kt       ← screen switching, back handling
        │       ├── theme/Theme.kt
        │       ├── home/
        │       │   ├── HomeViewModel.kt   ← state orchestration
        │       │   └── HomeScreen.kt      ← clock, weather, app shelf/grid
        │       ├── background/
        │       │   └── NatureBackground.kt← photographic images + video wallpaper
        │       ├── components/            ← tiles, clock, weather, buttons
        │       └── settings/              ← settings, manage apps, location dialog
        └── res/
            ├── drawable-nodpi/            ← banner, icon, four background JPEGs
            ├── mipmap-*/                  ← launcher icons
            └── values/                    ← strings, colors, theme
```

---

## 3. Build environment (this machine)

The toolchain was installed during the first build session:

| Tool | Location |
|---|---|
| JDK 17 (Temurin) | `C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot` |
| Android SDK | `D:\Tools\android-sdk` |
| Platform / build tools | `android-35`, `build-tools;35.0.0` |
| Gradle | wrapper 9.7.1 (downloaded on first run) |
| ADB | `D:\Tools\android-sdk\platform-tools\adb.exe` |

`local.properties` points at the SDK and is **not committed**:

```properties
sdk.dir=D\:\\Tools\\android-sdk
```

### Build commands (PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot"
$env:ANDROID_HOME = "D:\Tools\android-sdk"
Set-Location D:\Coding\StreamLine

.\gradlew.bat :app:assembleDebug --console=plain
```

APK output:

```
app\build\outputs\apk\debug\app-debug.apk
```

Debug builds use the application id **`com.streamline.tv.debug`** because of the
`applicationIdSuffix`. Release uses `com.streamline.tv`.

Other useful tasks:

```powershell
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat clean
```

### Install on a Fire TV

User-facing deployment instructions live in the README's **Build & Deploy**
section. The short developer form is:

```powershell
& "D:\Tools\android-sdk\platform-tools\adb.exe" connect <fire-tv-ip>:5555
& "D:\Tools\android-sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
& "D:\Tools\android-sdk\platform-tools\adb.exe" shell am start -n com.streamline.tv.debug/com.streamline.tv.MainActivity
```

To offer StreamLine as a Home app (only where Fire OS allows it):

```powershell
adb shell cmd package set-home-activity com.streamline.tv.debug/com.streamline.tv.MainActivity
```

Do **not** disable or uninstall the Amazon launcher by default. It is the user's
safety net if a sideloaded launcher misbehaves. Mention the command in docs only
as an advanced, reversible option.

---

## 4. Architecture decisions

### Manual DI, no Hilt/Koin

`StreamlineApplication` creates a single `AppContainer` with the four
repositories. Everything is cheap to construct. This keeps startup fast and the
dependency graph obvious. Add Hilt only if the graph grows well beyond this.

### State flows in one ViewModel

`HomeViewModel` combines four flows into `HomeUiState`:

```
DataStore(settings) ─┐
installed apps ──────┼── combine ──> HomeUiState ──> Compose
weather state ───────┤
clock ticker ────────┘
```

- Clock ticks once per second, aligned to the wall clock.
- Weather refreshes every 15 minutes and whenever location/unit changes.
- App list is loaded once per process (and on resume); filtering/sorting happens
  reactively from settings, so toggling favourites/hidden is instant.
- Default favourites are Plex, YouTube, YouTube TV, and MLB TV. Google TV,
  mobile, and Amazon Fire TV package candidates are recognised;
  `DEFAULT_APP_GROUPS` picks one tile per service in that order.
  `ensureDefaultFavorites()` seeds them once per defaults version and removes
  the retired Spotify defaults.
- Home is favourites-only unless `showAllApps` is enabled in Settings. Hidden
  apps are always excluded, and Manage apps still lists every launchable app.
  The four default services use bundled themed glyphs so their tiles match.
- UI screens are switched with a private `Screen` enum in `StreamlineApp.kt`.
  No Navigation library: there are only three screens and back handling is
  explicit.

### App discovery

`AppsRepository` queries both `CATEGORY_LAUNCHER` and `CATEGORY_LEANBACK_LAUNCHER`
because Fire TV apps often declare only the TV category. It records the resolved
activity/component and launches it explicitly when `getLaunchIntentForPackage()`
cannot resolve a leanback-only app. It excludes StreamLine itself, loads labels,
and rasterises icons to 144 px `ImageBitmap`s once. On Android 11+ this requires
`QUERY_ALL_PACKAGES`; the permission is declared with a lint suppression and is
justified for a launcher.

### Weather and location

- Forecast: **Open-Meteo** (`api.open-meteo.com`), no API key, WMO weather codes.
- City search: **Open-Meteo geocoding** (`geocoding-api.open-meteo.com`).
- Auto-location: approximate city from public IP via **ipinfo.io**. This avoids
  the `ACCESS_COARSE_LOCATION` runtime prompt, which is awkward on a TV and
  often unavailable on Fire TV sticks.
- Default location is Uniontown, Ohio (the first developer's area) so the widget
  is useful before auto-detection completes.
- All network calls use `HttpURLConnection` with 12 s timeouts and `Result`
  wrapping. Weather failure never crashes the launcher; the widget shows an
  unavailable state.

### Nature background

`NatureBackground.kt` displays bundled photographic landscapes with Compose
`Image`:

- Four presets: Aurora Peaks, Alpine Lake, Coastal Dusk, Forest Night.
- JPEGs live in `res/drawable-nodpi/` at 1920 px wide (~2 MB total) and are
  cropped with `ContentScale.Crop`. No network request or runtime file decode
  from external storage is required.
- A single `withFrameNanos` loop drives a slow scale/drift via
  `mutableFloatStateOf`, capped at ~30 fps. Composables do not recompose per
  frame; the graphics layer reads the state directly.
- Time-of-day mode applies a light colour wash, and a vertical scrim keeps the
  clock/weather readable over bright photographs.
- `reducedMotion` freezes the drift for low-power use.
- Photographer and license details are in `THIRD_PARTY_NOTICES.md`.

**Video wallpaper** is the escape hatch for real footage: drop an MP4 at
`files/wallpapers/background.mp4` in the app's data directory, or add
`res/raw/wallpaper.mp4`. `VideoWallpaperSource` prefers the raw resource, then
the file. Missing video falls back to the Aurora Peaks photograph. Video
playback uses the platform `VideoView` (no Media3 dependency) with looping,
muted playback.

### TV focus and input

- Every interactive element is focusable and driven by `clickable`, which
  handles D-pad centre/Enter on Android TV.
- App tiles animate scale/border on focus and call `BringIntoViewRequester` so
  lazy rows scroll to the focused tile.
- Home requests focus for the first tile on launch and after returning from an
  app (via `LifecycleResumeEffect`). If no apps are installed, focus goes to the
  Settings button.
- Back on Home opens an exit confirmation. Its Exit action starts the Amazon
  launcher component explicitly, so leaving StreamLine still works when it is
  the default Home app. Back on Settings/Manage apps returns to the previous
  screen.

### Persistence

One Preferences DataStore named `streamline_settings`. Enum values are stored by
name with safe fallbacks, so adding enum entries or renaming keys will not crash
existing installs. Add new settings in three places:

1. `AppSettings` data class (with a default)
2. `SettingsRepository.Keys` + `toSettings()` + `write()`
3. The relevant settings row in `SettingsScreen`

---

## 5. Code conventions

- Package root: `com.streamline.tv`.
- Kotlin official style, 4-space indentation, trailing commas in multi-line
  argument lists.
- Compose functions are `PascalCase`; helpers are `camelCase`.
- Hoist state: leaf components take values + callbacks. Only screen-level
  composables read the ViewModel.
- Keep `androidx.tv.material3` for theme primitives; most surfaces are custom so
  focus visuals stay consistent.
- Do not add image-loading, navigation, or DI libraries for small features.
  Current dependency count is deliberately tiny.
- Comments explain **why**, not what. The background drift loop is the one
  place where extra detail is useful because frame timing is non-obvious.
- User-facing strings currently live inline in Compose (v0.1.0). Moving them to
  `strings.xml` is a good first cleanup task.
- Never commit `local.properties`, keystores, or signing passwords.

### Performance rules

- No allocations or `Random` inside per-frame draw code; precompute in
  `SceneGeometry`.
- No network or disk work on the main thread.
- Keep the app list icon cache bounded (144 px per app).
- Test any new animation on the slowest target device available.

---

## 6. Verification checklist

Before handing off a change:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Then, on a device or emulator:

- [ ] App launches to Home in landscape, no action bar.
- [ ] Clock updates every second; 12/24-hour and seconds settings apply.
- [ ] Weather loads (or shows a clean unavailable state offline).
- [ ] D-pad left/right moves through tiles; focused tile scales and scrolls into view.
- [ ] OK launches an app and returning restores focus.
- [ ] Settings opens from the header; every row is reachable with the D-pad.
- [ ] Background scene changes apply immediately; reduced motion freezes motion.
- [ ] Home shows favourites only by default; **Show all apps** restores the full list.
- [ ] Favourites sort first; hidden apps disappear from Home but remain in Manage apps.
- [ ] Location search updates the weather city.
- [ ] Back on Home shows the exit dialog; Back elsewhere goes up one level.

There are no automated tests yet. High-value first tests: WMO code mapping,
`visibleApps` sorting/filtering, and DataStore enum fallbacks.

---

## 7. Known limitations / gotchas

- **Fire OS launcher replacement is not guaranteed.** Amazon restricts
  third-party launchers on some Fire OS versions; the HOME intent filter may be
  ignored. Never promise Home-button interception.
- **`QUERY_ALL_PACKAGES`** is required to enumerate apps on API 30+. Removing it
  will silently hide most apps on newer Fire OS.
- **IP geolocation** is city-level and may be wrong behind VPNs or CGNAT. Manual
  city search is the override; `autoDetectLocation=false` stops overwrites.
- **Open-Meteo** has no SLA. Keep the graceful failure path intact.
- **Video wallpaper** uses a `SurfaceView`-backed `VideoView`. On some devices it
  can composite oddly with Compose overlays. If that happens, switch to a
  `TextureView` + `MediaPlayer` implementation.
- **Debug application id** is `com.streamline.tv.debug`; adb commands and
  `set-home-activity` must use the debug id when testing debug builds.
- **4K rendering** scales 1920 px photographs to the panel with a slow GPU
  graphics-layer transform. On weak GPUs, `reducedMotion` is the first
  mitigation; pre-scaled 4K JPEGs or a static image are next if profiling shows
  fill-rate limits.
- **The app does not request location permission.** Do not add it without a
  clear UX reason; it breaks the "no prompts on a TV" design.
- **No release signing config yet.** `assembleRelease` produces an unsigned APK.

---

## 8. Prioritized next steps

1. **Validate on real Fire TV hardware** (focus order, 4K frame rate, HOME
   behaviour, app launch list). This is the biggest unknown.
2. **Recently used apps** — record launch timestamps in DataStore and add a
   second row ("Jump back in").
3. **Explicit app ordering** — remote-friendly "move left/right" actions in
   Manage apps (drag-and-drop is hostile to D-pad).
4. **Ambient/screensaver mode** — full-screen clock over the nature scene after
   inactivity, like a TV screensaver.
5. **Theme accents** — let users pick the mint/aqua/amber accent; wire it through
   `StreamlineTheme` and `StreamlineColors`.
6. **Plex integration** — needs user token + server discovery; keep it optional
   and isolated in a new `data/plex` package.
7. **Tests + CI** — unit tests for the pure logic, GitHub Actions for
   `assembleDebug` and `lintDebug`.
8. **Accessibility** — content descriptions for icons, larger-text option,
   high-contrast focus ring.
9. **Release pipeline** — signing config from environment variables, version
   code automation, changelog enforcement.

---

## 9. Development machine setup (already applied)

These are outside the repo but relevant if the environment is rebuilt:

- **Windows Terminal** default profile is Git Bash:
  `"C:\Program Files\Git\bin\bash.exe" -i -l`, custom "StreamLine" colour
  scheme, JetBrainsMono Nerd Font 11 pt, no acrylic, hidden scrollbar.
  Settings: `%LOCALAPPDATA%\Packages\Microsoft.WindowsTerminal_8wekyb3d8bbwe\LocalState\settings.json`.
- **oh-my-posh** 33.x at
  `%LOCALAPPDATA%\Programs\oh-my-posh\bin\oh-my-posh.exe`, theme at
  `~/.config/oh-my-posh/streamline.omp.json`.
- **JetBrainsMono Nerd Font** installed per-user for prompt glyphs.
- **`~/.bashrc`** no longer sources `ng completion` (that was the main startup
  cost). Warm shell startup is ~340 ms, down from ~1.0–1.1 s.
- **`~/.inputrc`** enables prefix history search (Up/Down), case-insensitive
  completion, coloured completions.
- **Git performance config**: `core.fscache`, `core.preloadindex`,
  `core.untrackedCache`, `feature.manyFiles`, `gc.auto=256`, protocol v2,
  histogram diff, `pull.ff=only`.
- Backups of the original dotfiles and terminal settings are in
  `~/.streamline-backup/`.

If you change the shell setup, measure before/after with:

```powershell
1..4 | ForEach-Object { Measure-Command { & "C:\Program Files\Git\bin\bash.exe" -lic "exit" 2>$null } }
```

---

## 10. Changelog discipline

Every user-visible or architectural change gets an entry in `CHANGELOG.md` under
`## [Unreleased]`, using Keep a Changelog categories (Added / Changed / Fixed /
Removed / Security). When cutting a version, move entries under a new version
heading with the date and bump `versionName` / `versionCode` in
`app/build.gradle.kts`.
