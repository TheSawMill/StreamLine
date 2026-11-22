# Streamline TV

**A clean, modern, lightweight, and ad-free launcher for your television.**

Streamline TV is an open-source project focused on creating a faster, simpler, and more enjoyable smart TV experience.

Modern smart TV interfaces have become increasingly cluttered with advertisements, sponsored content, recommendations, and unnecessary features. Streamline TV aims to bring the focus back to what matters: **your apps, your content, and your television.**

The goal is to provide a beautiful, responsive home screen that makes accessing your favorite streaming services effortless.

> **Project Status:** v0.1.0 — first functional build 🚧  
> The project builds as a debug APK and its core flows have been verified on a TCL Fire TV. Home-button and default-launcher behaviour still varies by device and Fire OS version.

---

## ✨ Features & Goals

### Clean, Minimal Interface
- Modern, visually appealing user interface.
- No advertisements, sponsored content, or unnecessary recommendations within Streamline.
- Large, easy-to-navigate application tiles.
- Customizable backgrounds, themes, and layouts.
- Designed specifically for television screens and remote controls.

### Fast & Lightweight
- Minimal resource consumption.
- Fast startup and responsive navigation.
- Smooth, lightweight animations.
- Minimal background processing.
- Optimized for lower-powered smart TV hardware.

### Application Management
- Automatically discover compatible installed applications.
- Launch streaming applications directly from the home screen.
- Customize application order and favorites.
- Home shows only favourites by default. Turn on **Settings → Show all apps** to
  see every launchable app again; **Manage apps** always lists everything.
- New installs start with Plex, YouTube, YouTube TV, and MLB TV first, when
  those apps are installed. Existing installs receive the same defaults once.
- Support popular services, including:
  - Plex
  - YouTube
  - YouTube TV
  - MLB TV
  - Prime Video
  - Other installed streaming applications

### Customization
- Dark-first design.
- Four bundled photographic nature backgrounds that work offline.
- Optional video wallpaper from a local MP4.
- Configurable application layouts.
- User preferences stored locally.
- Optional additional features without compromising simplicity.

### Future Possibilities
- Plex integration for recently watched content.
- Continue Watching functionality where supported by available APIs.
- Recently launched applications.
- Custom widgets such as weather and time.
- Multiple themes and appearance presets.
- Additional refinements for Android TV / Google TV devices.

These features represent potential development directions rather than guaranteed functionality.

---

## 📺 Supported Platforms

| Platform | Supported? | Notes |
|---|---|---|
| Amazon Fire TV Stick, Fire TV Cube, Fire TV Edition TVs (including **TCL Fire TVs**) | ✅ | Primary target. Requires Fire OS 6 or newer (Android 7.1 / API 25+). Install by sideloading the APK. |
| Android TV / Google TV devices and TVs | ✅ | Same APK. Includes NVIDIA Shield, Chromecast with Google TV, Google TV Streamer, and Android/Google TV televisions. |
| TCL Roku TV and other Roku TVs | ❌ | Roku TV OS cannot run Android APKs or load a third-party launcher. |
| Samsung Tizen, LG webOS, Hisense VIDAA, Apple TV (tvOS) | ❌ | Different app platforms; no StreamLine build exists and the system launcher cannot be replaced in the way an Android launcher can. |

### Amazon Fire TV (primary development target)

Streamline TV is built for Amazon Fire TV devices running Fire OS 6+, including Fire TV Sticks, Fire TV Cube, and Fire TV Edition televisions such as **TCL Fire TVs**.

To confirm what your TCL TV runs, open **Settings → My Fire TV → About** and look for "Fire TV" or a Fire OS version. If the TV says **Roku TV** or **Google TV**, use the corresponding guidance below instead.

Implemented capabilities:
- Native Kotlin / Jetpack Compose application with a Fire TV banner and launcher icon.
- D-pad and remote-key navigation with large, focusable app tiles.
- Discovery and launching of installed apps, including TV apps that declare only `LEANBACK_LAUNCHER`.
- Customizable home screen: clock, weather, backgrounds, layouts, favorites, and hidden apps.
- A `HOME` intent filter so StreamLine can be offered as a Home app where Fire OS allows it.

**Important:** Amazon restricts third-party launcher replacement on many Fire OS versions. StreamLine cannot guarantee that the Home button will open it or that it will survive a reboot. Sideloading and opening the app manually always work when the device permits sideloading. Keep Amazon's launcher installed as your safety net.

### Android TV / Google TV

The same APK also targets Android TV and Google TV devices running Android 7.1 (API 25) or newer. This includes set-top boxes such as NVIDIA Shield and Chromecast with Google TV / Google TV Streamer, as well as Android TV and Google TV televisions from manufacturers such as Sony, TCL, Hisense, and Philips.

Installation is the same ADB sideload described below. Whether you can select StreamLine as the default launcher depends on the manufacturer: some devices expose a launcher picker, some accept `cmd package set-home-activity`, and some ignore both.

### Roku and other smart TV operating systems

Roku, Samsung Tizen, LG webOS, Hisense VIDAA, and Apple TV tvOS are **not supported**. They cannot install an Android APK, and they do not allow a third-party app to replace the system launcher through standard developer tools. A separate implementation would be required for any of these platforms.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Primary Language | Kotlin |
| Platform | Android / Fire OS / Android TV OS |
| User Interface | Jetpack Compose for TV |
| Development Environment | Android Studio |
| Build System | Gradle (wrapper included) |
| Preferences | Android DataStore |
| Application Discovery | Android PackageManager |
| Remote Navigation | Android TV focus and key event handling |
| Weather | Open-Meteo (no API key) |

---

## 🏗️ Project Architecture

The app lives in a single `app` module:

```text
app/src/main/java/com/streamline/tv/
├── MainActivity.kt            # Compose host activity
├── StreamlineApplication.kt   # AppContainer (manual dependency injection)
├── data/                      # Settings, app discovery, weather repositories
└── ui/                        # Home, settings, components, theme, backgrounds
```

Development priorities:

1. **Modularity:** Separate UI components, application discovery, navigation, settings, and platform-specific functionality.
2. **Maintainability:** Readable, well-organized code with clear responsibilities.
3. **Performance:** Minimize startup time, unnecessary rendering, and background resource consumption.
4. **Compatibility:** Isolate Fire OS-specific functionality from general Android TV functionality where practical.
5. **Extensibility:** Allow new themes, layouts, integrations, and features without major architectural changes.
6. **Reliability:** Keep the launcher functional when optional integrations or services are unavailable.

---

## 🎨 Design Philosophy

Streamline TV follows a simple principle:

**Your television should open your apps, not advertise to you.**

The interface should feel modern, polished, and intuitive while remaining lightweight enough to run smoothly on typical smart TV hardware.

Core design principles:

- **Content First:** Prioritize applications and user-selected content.
- **Minimalism:** Avoid unnecessary interface elements.
- **Consistency:** Maintain predictable navigation and visual behavior.
- **Accessibility:** Support clear focus indicators, readable text, and remote-friendly controls.
- **Performance:** Prefer responsiveness over excessive visual effects.
- **Customization:** Allow users to personalize their experience without introducing complexity.

The default interface should remain simple, with advanced functionality available only when explicitly enabled.

---

## 🗺️ Development Roadmap

### Phase 1 — Foundation
- [x] Initialize Android project.
- [x] Establish application architecture.
- [x] Implement initial Streamline TV interface.
- [x] Implement remote-control navigation.
- [x] Discover installed launchable applications.
- [x] Launch selected applications.
- [ ] Validate installation and operation on Fire TV hardware.

### Phase 2 — Customization
- [x] Application favorites.
- [ ] Explicit application ordering.
- [x] Wallpaper selection (built-in photographic scenes and optional video wallpaper).
- [ ] Theme configuration.
- [x] Persistent user preferences.
- [x] Additional application layout options.

### Phase 3 — Platform Integration
- [ ] Investigate supported default-launcher behavior.
- [ ] Evaluate Fire OS version-specific restrictions.
- [ ] Implement supported home-screen integration where possible.
- [ ] Improve device compatibility.
- [ ] Add recovery and fallback mechanisms.

### Phase 4 — Enhanced Experience
- [ ] Explore Plex integration.
- [ ] Optional Continue Watching functionality.
- [ ] Recently used applications.
- [ ] Optional widgets.
- [ ] Performance optimization and accessibility improvements.

Roadmap priorities may change based on device compatibility, technical feasibility, and community feedback.

---

## 🚀 Build & Deploy

### 1. Build the APK

Requirements:
- **JDK 17**
- **Android SDK Platform 35** with build tools
- **Android Studio** (optional; the Gradle wrapper builds without it)
- **Android platform-tools / ADB** for installation (included with Android Studio)

On this development machine, `local.properties` already points to the SDK. On a fresh checkout, Android Studio generates this file, or you can create it:

```properties
sdk.dir=D\:\\Tools\\android-sdk
```

Build from the repository root:

```powershell
# Windows PowerShell (adjust these paths for your machine)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot"
$env:ANDROID_HOME = "D:\Tools\android-sdk"
Set-Location D:\Coding\StreamLine
.\gradlew.bat :app:assembleDebug --console=plain
```

```bash
# macOS / Linux
./gradlew :app:assembleDebug
```

The installable debug APK is written to:

```text
app\build\outputs\apk\debug\app-debug.apk        # Windows
app/build/outputs/apk/debug/app-debug.apk        # macOS / Linux
```

Debug builds use the application ID **`com.streamline.tv.debug`**. Use that ID in all ADB commands below.

`assembleRelease` also builds successfully, but it produces an **unsigned** APK (`app-release-unsigned.apk`) until a signing configuration is added. For now, sideload the debug APK; it is signed with Android's debug key and installs normally.

### 2. Deploy to a TCL Fire TV (or any Fire TV device)

These steps apply to a **TCL Fire TV Edition** television, Fire TV Stick, Fire TV Cube, or any other Fire OS device.

1. Connect the TV and your computer to the **same network**.
2. On the TV, open **Settings → My Fire TV → Developer Options**:
   - Turn **ADB debugging** **On**.
   - Turn **Apps from Unknown Sources** **On**.
   - If Developer Options is missing, go to **My Fire TV → About** and press **Build** several times.
3. Find the TV's IP address: **Settings → My Fire TV → About → Network**.
4. From your computer, connect and install:

```powershell
$adb = "D:\Tools\android-sdk\platform-tools\adb.exe"   # or just "adb" if platform-tools is on PATH

& $adb connect <TV_IP>:5555
& $adb devices
& $adb install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb shell am start -n com.streamline.tv.debug/com.streamline.tv.MainActivity
```

5. On the TV, open **Apps → StreamLine TV** if it did not launch automatically.

If `adb connect` fails, confirm ADB debugging is enabled, confirm the IP address, and make sure no guest-network isolation is active. If the TV asks to allow the debugging connection, approve it. A USB cable also works with `adb devices` instead of `adb connect`.

### 3. Optional: make StreamLine the Home app (Fire TV / Android TV)

StreamLine declares a `HOME` intent filter, so some devices let you choose it as the Home app. First save the current Home app so you can restore it:

```powershell
& $adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Copy the returned component (for example `com.amazon.tv.launcher/com.amazon.tv.launcher.ui.HomeActivity_vNext`), then set StreamLine:

```powershell
& $adb shell cmd package set-home-activity com.streamline.tv.debug/com.streamline.tv.MainActivity
```

Restore the original launcher at any time:

```powershell
& $adb shell cmd package set-home-activity <saved-component>
```

When accepted, this makes StreamLine the app that starts after boot and the one the **Home** button returns to. Press **Back** on Home, then choose **Exit to Fire TV** to open Amazon's launcher directly — this works even while StreamLine is the default Home app.

On some Fire OS versions you can also press **Home**, highlight **StreamLine TV**, and choose **Always**. Other versions ignore third-party Home apps entirely; the ADB command may appear to succeed but the Home button still opens Amazon's interface. This is an OS restriction, not a launcher bug.

**Do not disable or uninstall the Amazon launcher.** It is the recovery path if a sideloaded launcher stops working.

If you are using a signed release build instead of the debug build, replace `com.streamline.tv.debug` with `com.streamline.tv` in the commands above.

### 4. Deploy to other Android TV / Google TV devices

The same debug APK works on Android TV and Google TV boxes and televisions:

1. Enable developer options on the device (usually **Settings → Device Preferences → About → Build**, pressed repeatedly).
2. Enable **USB debugging** and, if supported, **Network / ADB debugging**. Some devices require a USB connection once before network debugging becomes available.
3. Find the device IP address in its network settings.
4. Install with ADB:

```powershell
& $adb connect <DEVICE_IP>:5555
& $adb install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb shell am start -n com.streamline.tv.debug/com.streamline.tv.MainActivity
```

To make StreamLine the default launcher, try the device's **Settings → Home / Launcher** picker first. If none exists, use the `cmd package set-home-activity` commands from the previous section. Google TV and some OEM builds may refuse to change the Home app.

Android Studio users can also select the TV device in the device picker and click **Run**.

### 5. Install without a PC

If you do not want to use ADB:

1. Install the **Downloader** app from the Amazon Appstore (Fire TV) or a file manager/browser from the Google Play Store (Android TV).
2. Enable **Apps from Unknown Sources** in developer options.
3. Host `app-debug.apk` at a URL you control (a GitHub release, a local web server, etc.).
4. Open Downloader, enter that URL, and install the APK when prompted.
5. Open StreamLine from the Apps row.

There is no official hosted release URL yet, so someone must host the APK for you. ADB is the most reliable method.

### 6. Update, troubleshoot, or remove

```powershell
# Update while keeping preferences
& $adb install -r .\app\build\outputs\apk\debug\app-debug.apk

# Remove the app (preferences are deleted too)
& $adb uninstall com.streamline.tv.debug

# Restore the stock launcher if you changed it
& $adb shell cmd package set-home-activity <saved-component>
```

Common issues:
- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE` / signature mismatch:** You previously installed a different signing key. Uninstall first, then install again.
- **"Could not launch this app":** The app is installed but its launch activity is unavailable. Reinstall it, then try again.
- **No apps on the home screen:** Make sure the streaming apps are installed and marked as favourites in **Settings → Manage apps**. Turn on **Settings → Show all apps** if you want every launchable app on Home.
- **Weather unavailable:** StreamLine needs internet access for weather and location. It stays fully usable offline.
- **Home button still opens the stock interface:** Expected on restricted Fire OS and Google TV builds. Open StreamLine from the Apps row instead.

### 7. Verify a successful install

- [ ] App opens in landscape with no action bar.
- [ ] Clock updates every second; date and settings appear.
- [ ] Weather loads, or shows a clean "Weather unavailable" state offline.
- [ ] D-pad moves between tiles; the focused tile scales and scrolls into view.
- [ ] Press OK opens an app; returning restores focus.
- [ ] Settings, Manage apps, and the location search are reachable with the remote.
- [ ] Back on Home shows the exit dialog; **Exit to Fire TV** opens Amazon's launcher.
- [ ] Home button returns to StreamLine when it is set as the default Home app.
- [ ] Only favourites appear on Home by default; **Show all apps** restores the full list.
- [ ] Plex, YouTube, YouTube TV, and MLB TV appear first when installed.
- [ ] Favorites sort first and hidden apps disappear from Home.

---

## 🔒 Privacy & Security

Streamline TV aims to provide a privacy-conscious television experience.

Project principles include:

- No built-in advertising or behavioral tracking.
- No unnecessary analytics or telemetry.
- No collection of personal information unless explicitly required for an optional feature.
- Local storage of application preferences wherever practical.
- Minimal Android permissions.
- Transparent handling of optional external integrations.

Weather and automatic location use Open-Meteo and ipinfo.io over HTTPS. If you disable automatic location detection or choose a city manually, no IP-based lookup is performed at startup. The launcher does not request Android location permission.

The launcher does not control data collection performed by third-party streaming applications.

Any feature requiring network access, authentication, or additional permissions should be documented clearly.

Bundled background photographs and their licenses are listed in
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

## 🤝 Contributing

Contributions, feature suggestions, bug reports, and improvements are welcome as the project develops.

Contribution guidelines, coding standards, and development workflows will be added as the repository matures. See `AGENTS.md` for the current architecture and verification checklist.

For now, development should follow the project's core priorities:

**Simplicity. Performance. Reliability. Customization.**

---

## ⚠️ Disclaimer

Streamline TV is an independent, community-driven project and is not affiliated with, endorsed by, or sponsored by Amazon, Roku, Google, Plex, YouTube, or any other streaming platform.

All product names and trademarks belong to their respective owners.

Platform restrictions may limit certain launcher features, including automatic startup, Home-button behavior, and replacement of manufacturer-provided interfaces.

Streamline TV does not modify third-party streaming applications or remove advertisements displayed within those applications.

---

## 📄 License
StreamLine TV is open-source software licensed under the
[MIT License](LICENSE).

This software is provided "AS IS", without warranties of any kind.
The authors and contributors disclaim liability for damages arising
from its use, to the extent permitted by applicable law.

---
**Streamline TV — Your TV. Your Apps. Your Experience.**
