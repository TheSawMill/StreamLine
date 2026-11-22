# Changelog

All notable changes to StreamLine TV are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- README deployment guide for Fire TV (including TCL Fire TV Edition),
  Android TV / Google TV, ADB and Downloader installation, optional Home app
  setup, rollback steps, and a supported/unsupported platform table.
- Plex, YouTube, YouTube TV, and MLB TV are the default favourites when those
  packages are installed. Existing installs receive the defaults once per
  version without overwriting later user changes.
- Bundled themed tile glyphs for the four default services and a redesigned
  16:9 Fire TV banner, square launcher icons, and adaptive icon so the
  StreamLine logo reads cleanly in the Fire TV app picker.
- `THIRD_PARTY_NOTICES.md` with photographer and license details for the
  bundled background images.

### Changed
- Home now shows only favourites by default; **Settings → Show all apps**
  restores every launchable app. Manage apps remains the full catalogue.
- The exit dialog now opens Amazon's Fire TV launcher explicitly, so you can
  leave StreamLine even after setting it as the default Home app.
- Added a boot receiver so StreamLine starts after a TV restart even on Fire OS
  builds that ignore default-launcher changes.
- README project status, supported platforms, architecture, and roadmap now
  reflect the v0.1.0 functional build.
- Replaced procedural Canvas nature scenes with bundled photorealistic landscape
  JPEGs: Aurora Peaks, Alpine Lake, Coastal Dusk, and Forest Night. The images
  work offline and keep a subtle drift animation unless reduced motion is on.

### Fixed
- Apps that declare only `CATEGORY_LEANBACK_LAUNCHER` are now launched using the
  activity discovered during app enumeration, instead of failing when
  `getLaunchIntentForPackage()` returns null.
- Default tiles now recognize Amazon Fire TV package variants for YouTube and
  YouTube TV, and the defaults migration re-runs once to seed them on existing
  installs.
- MLB TV is recognised on Fire TV (`com.bamnetworks.mobile.android.gameday.atbat`)
  and replaces Spotify as the fourth default tile.

## [0.1.0] — 2026-11-22

First functional build. Compiles as a debug APK and is ready for Fire TV
hardware testing.

### Added
- Android project scaffold: Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, Jetpack
  Compose for TV (`androidx.tv:tv-material` 1.0.0), min SDK 25, target SDK 34.
- Fire TV manifest with `LEANBACK_LAUNCHER` and `HOME` intent filters, banner,
  launcher icons, landscape-only activity, keep-screen-on.
- Home screen with a large clock, date, weather card, settings entry point, and
  app shelf. Three layouts: single row, two rows, and adaptive grid.
- App discovery through `PackageManager` using both launcher categories, with
  144 px icon caching and launch handling.
- Favourites and hidden-app support. Favourites sort first; hidden apps remain
  installed but leave the home screen.
- Procedural 4K nature backgrounds rendered with Compose Canvas:
  - Aurora Peaks, Alpine Lake, Coastal Dusk, and Forest Night scenes.
  - Time-of-day palettes that interpolate through night, dawn, day, and dusk.
  - Sun/moon arcs, stars, aurora ribbons, drifting clouds, layered parallax
    ridges, snow caps, water reflections, forest silhouettes, fireflies, birds,
    haze, and vignette scrims.
  - Precomputed geometry and a 30 fps frame loop that redraws without
    recomposition.
- Optional video wallpaper via `files/wallpapers/background.mp4` or
  `res/raw/wallpaper.mp4`, with a graceful fallback to Aurora Peaks.
- Weather widget powered by Open-Meteo (no API key): current temperature,
  feels-like, high/low, condition, humidity, wind, and day/night icons drawn in
  Compose. Refreshes every 15 minutes.
- Automatic location from public IP (ipinfo.io) plus manual city search using
  Open-Meteo geocoding. No runtime location permission is requested.
- Settings screen covering background scene, time-of-day mode, layout, reduced
  motion, app labels, 12/24-hour clock, seconds, weather visibility, temperature
  unit, location, auto-detect, and manual weather refresh.
- Manage apps screen with favourite and visibility toggles for every launchable
  app.
- Persistent preferences in a single Preferences DataStore with safe enum
  fallbacks.
- Remote-first focus handling: focus restoration on launch/resume, bring-into-
  view scrolling, focus rings, and an exit confirmation dialog.
- Fire TV banner (320×180) and launcher icons generated for all densities.
- `AGENTS.md` handoff guide covering architecture, build environment, device
  deployment, conventions, limitations, and next steps.
- Development environment: Windows Terminal defaulted to Git Bash with a custom
  colour scheme and JetBrainsMono Nerd Font; oh-my-posh powerline prompt; faster
  Git Bash startup by removing the `ng completion` hook; readline prefix history
  search; Git performance settings.

### Changed
- README roadmap updated to reflect the first functional build and new
  development instructions.

### Fixed
- Resolved initial Kotlin compile errors in the background renderer (visibility
  of internal scene types, cloud palette reference) and missing focus imports.
