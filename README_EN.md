<div align="center">

# 📺 Vestel Remote — Smart Android TV Controller

**A hardware-verified native Android remote control application for the Vestel 55QG9960 4K QLED Android TV and compatible Android TV / Google TV devices over local Wi-Fi.**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Target Phone](https://img.shields.io/badge/Verified%20Phone-Samsung%20Galaxy%20S24%20Ultra-1428A0?logo=samsung&logoColor=white)](#4-supported-and-verified-hardware)
[![Target TV](https://img.shields.io/badge/Verified%20TV-Vestel%2055QG9960%20(GoogleTV7288)-E30613)](#4-supported-and-verified-hardware)
[![Release](https://img.shields.io/badge/Release-v1.0.0--beta.2-orange)](https://github.com/Terabithia1572/Vestel-Remote-Android/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Developed and adapted for Android TV by [Yunus İNAN](https://github.com/Terabithia1572)**  
Original macOS menu bar reference project: [LGwebOS-menubar — Gabriel Greco](https://github.com/gabrielgreco1/LGwebOS-menubar)

🌐 **Language / Dil:** **[🇹🇷 Türkçe (README.md)](README.md)** | **🇬🇧 English**

---

### 📱 Home Dashboard (Samsung Galaxy S24 Ultra & Vestel 55QG9960 `GoogleTV7288`)

<img src="docs/screenshots/dashboard.jpg" alt="Vestel Remote Home Dashboard" width="320"/>

<p><sub><b>Home Dashboard (Panel):</b> Live <code>GoogleTV7288</code> session, stabilized volume slider, quick actions, active input card, and real-time protocol log</sub></p>

### ✨ Redesigned Interface Gallery

<table>
  <tr>
    <td align="center" width="25%">
      <img src="docs/screenshots/remote.jpg" alt="Remote Controller" width="210"/><br/>
      <b>Remote Controller</b><br/>
      <sub>Circular tactile D-Pad, vertical volume/channel rockers & media keys</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/applications.jpg" alt="Applications" width="210"/><br/>
      <b>Applications</b><br/>
      <sub>DIAL (Port 8008) verification, quick launch grid & custom Deep Link</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/inputs.jpg" alt="Inputs" width="210"/><br/>
      <b>Inputs</b><br/>
      <sub>HDMI 1, HDMI 2 (eARC), HDMI 3, Live TV & AV 2-column selection grid</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/settings.jpg" alt="Settings" width="210"/><br/>
      <b>Settings</b><br/>
      <sub>Grouped preferences, device management, diagnostics & disconnect action</sub>
    </td>
  </tr>
</table>

</div>

---

## 📋 Table of Contents

1. [Project Introduction](#1-project-introduction)
2. [Features](#2-features)
3. [Screenshots](#3-screenshots)
4. [Supported and Verified Hardware](#4-supported-and-verified-hardware)
5. [Installation](#5-installation)
6. [Pairing Instructions](#6-pairing-instructions)
7. [Remote-Control Functionality](#7-remote-control-functionality)
8. [Application Launcher & DIAL Discovery](#8-application-launcher--dial-discovery)
9. [HDMI & Input Source Controls](#9-hdmi--input-source-controls)
10. [Volume Control Stabilization](#10-volume-control-stabilization)
11. [Technical Architecture](#11-technical-architecture)
12. [Android Permissions](#12-android-permissions)
13. [Security and Privacy](#13-security-and-privacy)
14. [Build Instructions](#14-build-instructions)
15. [Testing](#15-testing)
16. [Known Limitations](#16-known-limitations)
17. [Credits and Open-Source Notices](#17-credits-and-open-source-notices)
18. [License and Developer Information](#18-license-and-developer-information)

---

## 1. Project Introduction

**Vestel Remote** is a native Android smartphone application engineered to discover, pair with, and control **Vestel 55QG9960 4K QLED Android TV** sets and compatible Android TV / Google TV devices over a local Wi-Fi network.

Inspired by the product concepts of Gabriel Greco's macOS [LGwebOS-menubar](https://github.com/gabrielgreco1/LGwebOS-menubar) project, this application was built from the ground up for Android using **Kotlin**, **Jetpack Compose**, **Material 3**, and the **Android TV Remote Service v2 (mTLS / Protobuf)** wire protocol. It is **developed and adapted for Android TV by Yunus İNAN**.

This release has been verified end-to-end on physical hardware using a **Samsung Galaxy S24 Ultra** connected to a physical **Vestel 55QG9960 (`GoogleTV7288`)** television.

---

## 2. Features

- **🖤 Premium AMOLED Dark UI:** Deep black (`#06070B`) background, charcoal rounded cards with subtle borders, bright red (`#E5192A`) accents, and a single-line **5-tab bottom navigation bar** (`Dashboard`, `Remote`, `Apps`, `Inputs`, `Settings`).
- **🔍 Automatic TV Discovery (mDNS & SSDP):** Discovers `_androidtvremote2._tcp` (Android TV Remote v2) and SSDP/DIAL services on the local network, alongside manual IPv4 and MAC address configuration.
- **🔐 PIN-Verified Mutual TLS (mTLS) Pairing:** Performs a full mutual TLS handshake on port `6467` using an Android Keystore hardware-backed 2048-bit RSA client certificate and the 6-character hexadecimal PIN displayed on the TV.
- **📌 Server Certificate Pinning:** Captures and persists the SHA-256 fingerprint of the TV's X.509 certificate during pairing and enforces certificate verification on subsequent port `6466` control sessions.
- **🏠 Home Dashboard (`Panel`):** Displays the connected television (`GoogleTV7288`), model (`Vestel 55QG9960`), power button, stabilized volume slider (`-` / `+` and `Mute`), 4-tile quick action grid (`Home`, `Back`, `Assistant`, `Wake TV`), active input card, and a timestamped **Protocol Event Log**.
- **🎮 Ergonomic Remote Controller (`Kumanda`):** Circular tactile D-Pad, vertical volume/channel rockers, media playback row, and an expandable `...` drawer housing the Touchpad, broadcast color keys, and `0–9` numeric keypad.
- **🚀 Hybrid Application Launcher (`Uygulamalar`):** DIAL (`Port 8008`) status verification (`Running` / `Installed`), Quick Launch Shortcuts (Deep Link) grid, and a custom URI/package launcher.
- **🔌 Input Sources (`Girişler`):** 2-column HDMI / Live TV / AV selection grid with an active source highlight badge.
- **🩺 Connection Diagnostics & Grouped Settings (`Ayarlar`):** One-tap `Copy Diagnostics` export without exposing personal data, Device Management access, language/theme/haptic toggles, and a one-tap disconnect button.

---

## 3. Screenshots

All screenshots below were captured on a **Samsung Galaxy S24 Ultra** actively paired and connected to the physical **Vestel 55QG9960 (`GoogleTV7288`)** television (the last octet of the private LAN IP is redacted as `192.168.1.XXX` for privacy):

### Primary Screens (5-Tab Navigation & Device Management)

<table>
  <tr>
    <td align="center" width="33%">
      <img src="docs/screenshots/dashboard.jpg" alt="Dashboard Screen" width="240"/><br/>
      <b>1. Dashboard (Panel)</b><br/>
      <sub>Live <code>GoogleTV7288</code> status, volume control, quick actions, active input & protocol log</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/remote.jpg" alt="Remote Screen" width="240"/><br/>
      <b>2. Remote Controller (Kumanda)</b><br/>
      <sub>Circular D-Pad, vertical <code>VOL</code> / <code>CH</code> rockers, quick keys & media playback</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/applications.jpg" alt="Applications Screen" width="240"/><br/>
      <b>3. Applications (Uygulamalar)</b><br/>
      <sub>DIAL (Port 8008) verified apps, quick launch grid & custom Deep Link</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="33%">
      <img src="docs/screenshots/inputs.jpg" alt="Inputs Screen" width="240"/><br/>
      <b>4. Inputs (Girişler)</b><br/>
      <sub>2-column grid for HDMI 1, HDMI 2 (eARC), HDMI 3, Live TV & AV</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/settings.jpg" alt="Settings Screen" width="240"/><br/>
      <b>5. Settings (Ayarlar)</b><br/>
      <sub>Devices, Appearance, Remote, Advanced/Diagnostics sections & disconnect</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/devices.jpg" alt="Devices Screen" width="240"/><br/>
      <b>6. Devices (Cihazlar)</b><br/>
      <sub>mDNS / SSDP network scan, saved <code>GoogleTV7288</code> card & manual IP entry</sub>
    </td>
  </tr>
</table>

<details>
<summary><b>ℹ️ Additional Screenshot: About & Open-Source Attribution (Click to expand)</b></summary>
<br/>
<div align="center">
  <img src="docs/screenshots/about.jpg" alt="About Screen" width="250"/><br/>
  <b>7. About (Hakkında)</b><br/>
  <sub>Application version, developer attribution (Yunus İNAN) & open-source license notices</sub>
</div>
</details>

---

## 4. Supported and Verified Hardware

This release (`v1.0.0-beta.2`) has been tested and confirmed working end-to-end on real physical hardware:

| Component | Verified Device / Model | Status |
| :--- | :--- | :--- |
| **Smartphone** | **Samsung Galaxy S24 Ultra** (Dynamic AMOLED 2X, 1440×3120, One UI) | ✅ Verified |
| **Television** | **Vestel 55QG9960 4K QLED Android TV** (Advertised as `GoogleTV7288`) | ✅ Verified |
| **Other Compatible TVs** | Android TV and Google TV models exposing `Android TV Remote Service v2` (`_androidtvremote2._tcp`) | ✅ Compatible |
| **Discovery & Pairing** | Local Wi-Fi mDNS / SSDP discovery and Port `6467` mTLS 6-digit PIN pairing | ✅ Verified |
| **Live Remote & Volume / HDMI** | Port `6466` session, D-Pad, stabilized volume slider, HDMI input switching & app launching | ✅ Verified |

---

## 5. Installation

### Option 1: Install Pre-Built APK from GitHub Releases
1. Download the latest `app-debug.apk` asset from the **[Releases](https://github.com/Terabithia1572/Vestel-Remote-Android/releases)** page onto your Android smartphone.
2. Open the downloaded APK file and allow installation from unknown sources if prompted.

### Option 2: Install via ADB (Android Debug Bridge)
With **USB Debugging** enabled on your phone, run:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 6. Pairing Instructions

1. **Connect to the Same Wi-Fi Network:** Ensure your smartphone and your Vestel Android TV are connected to the same local Wi-Fi network.
2. **Power On the TV:** Turn on the television using its physical remote and keep it awake.
3. **Open Device Management:** Tap the connection status pill in the top header or navigate to **Settings → Device Management** (ensure *Settings → Developer / Virtual TV Mode* is turned off).
4. **Discover the TV:** Tap **"Scan Network (mDNS / SSDP)"**. Your TV (e.g., `GoogleTV7288`) will appear under discovered televisions. If your router blocks multicast traffic, enter the TV's IPv4 address manually.
5. **Pair with PIN:** Tap **"Pair"** on the TV card, enter the 6-character hexadecimal pairing code shown on your Vestel TV screen, and tap **Confirm**.
6. **Control Your TV:** Once the status transitions to **Connected**, all control tabs become active. Subsequent sessions reconnect directly using the pinned certificate without requiring a new PIN.

---

## 7. Remote-Control Functionality

The **Remote** screen is designed for ergonomic one-handed operation:

- **Top Utility Row:** `Source` (Input), `Assistant` (Google Assistant), `...` (More Controls Drawer), and red `Power` button.
- **Center Navigation Deck:**
  - **Left Vertical Rocker (`VOL`):** Haptic `VOL +` and `VOL -` rocker.
  - **Circular D-Pad:** Dark recessed ring with Up, Down, Left, Right directional keys and an elevated center **`OK`** disc.
  - **Right Vertical Rocker (`CH`):** `CH +` and `CH -` rocker.
- **Quick Navigation Row:** `Back`, `Home`, `Mute`, and `Menu` pill buttons.
- **Media Transport Card:** `Rewind`, red-accented `Play / Pause`, and `Fast Forward` controls.
- **Expandable `...` Drawer:** Provides instant access to the **Touchpad**, **Broadcast Color Keys** (Red, Green, Yellow, Blue), **Info / Subtitles**, and **`0–9` Numeric Keypad** without cluttering the primary remote view.

---

## 8. Application Launcher & DIAL Discovery

The **Applications** screen combines two complementary Android TV launch mechanisms transparently:

1. **TV-Verified Applications (DIAL — Port `8008`):**
   - Queries the TV's local DIAL REST server (`http://<TV_IP>:8008/apps/<AppName>`) to verify whether DIAL-capable apps such as **YouTube** and **Netflix** are installed on the TV and whether they are currently `Running` (`Açık`) or `Installed` (`Kurulu`).
   > **Protocol Note:** Standard Android TV Remote v2 (`Port 6466`) and DIAL (`Port 8008`) protocols do not expose a full list of all installed APK packages on the television unless ADB developer mode is enabled.
2. **Quick Launch Shortcuts (Deep Link — Port `6466`):**
   - Provides a 2-column grid for **YouTube, Netflix, Prime Video, Disney+, Spotify, Exxen, BluTV, Gain, TOD, and Twitch** with star (`★`) pinning and instant search filtering. Apps launch via Android TV Remote v2 `RemoteAppLinkLaunchRequest` (Field `90`) with DIAL fallback.
3. **Custom Deep Link / Package Launcher:**
   - The **Custom Deep Link** bar at the bottom of the screen allows sending any `https://...`, `vnd.youtube://...`, or custom app URI directly to the television.

---

## 9. HDMI & Input Source Controls

The **Inputs** screen presents physical and broadcast sources in a clean 2-column card grid:

- **Supported Sources:** `HDMI 1`, `HDMI 2 (eARC)`, `HDMI 3`, `Live TV` (Satellite / Terrestrial), and `AV / Composite`.
- **Active Source Indicator:** The currently selected input is highlighted with a red border and an **`ACTIVE`** badge, and is also mirrored on the Home Dashboard's **Active Input** card.

---

## 10. Volume Control Stabilization

Because the Android TV Remote v2 protocol does not support absolute volume percentage commands (`setVolume(35)`) and only accepts incremental `KEYCODE_VOLUME_UP` / `KEYCODE_VOLUME_DOWN` key events, [`VolumeSliderController`](app/src/main/java/com/yunusinan/vestelremote/ui/viewmodel/VolumeSliderController.kt) stabilizes slider interactions:

- **Frame Coalescing:** Rapid UI drag frames are coalesced into a single latest target value rather than queuing an unbounded stream of network packets.
- **Rate Limiting (`140ms`) & Step Cap:** Enforces a `140ms` interval between steps and caps the maximum number of incremental steps per gesture so the TV's command buffer never overflows.
- **Immediate Cancellation on Release or Reversal:** Releasing the slider (`onValueChangeFinished`) or reversing drag direction **immediately cancels** any pending volume steps so volume stops right where the user releases.
- **Fine Step Controls:** Dedicated `-` and `+` single-step buttons and a `Mute` toggle accompany the slider.

---

## 11. Technical Architecture

The codebase follows **Clean Architecture + MVVM** with reactive `StateFlow` state management:

```text
app/src/main/java/com/yunusinan/vestelremote/
├── MainActivity.kt                    # Single-Activity Compose entry point & 5-tab bottom navigation
├── data/
│   ├── crypto/
│   │   ├── CertificateHelper.kt       # Android Keystore 2048-bit RSA & X.509 certificate generator
│   │   ├── PairingCrypto.kt           # SHA-256(ClientMod||ClientExp||ServerMod||ServerExp||PIN)
│   │   └── TlsSocketFactoryHelper.kt  # mTLS KeyManager & SHA-256 certificate-pinning TrustManager
│   ├── datastore/
│   │   └── PreferencesManager.kt      # Persisted devices, pinned fingerprints & user preferences
│   ├── protocol/
│   │   ├── AndroidTvRemoteV2Client.kt # Port 6467 pairing & Port 6466 live remote session
│   │   ├── RemoteProtocol.kt          # Polo/Remote v2 state machines & strict frame validators
│   │   ├── ProtoUtils.kt              # Lightweight varint-framed Protobuf encoder/decoder
│   │   ├── TvDiscoveryManager.kt      # NsdManager (mDNS) and SSDP discovery manager
│   │   ├── DialClient.kt              # Port 8008 DIAL application probing & launcher
│   │   ├── WakeOnLan.kt               # UDP Port 9 Magic Packet broadcaster
│   │   ├── AttemptDiagnostics.kt      # Privacy-safe connection attempt diagnostic recorder
│   │   └── MockTvClient.kt            # Virtual TV simulator for hardware-free testing
│   └── repository/
│       ├── TvRepository.kt            # Domain-Data contract
│       └── TvRepositoryImpl.kt        # Session, discovery & persistence orchestration
├── domain/model/                      # TvDevice, TvCommand, ConnectionStatus, AppItem, InputSource, etc.
└── ui/
    ├── components/                    # VestelBottomNavigationBar, DPadControl, TouchpadControl, etc.
    ├── screens/                       # Dashboard, Remote, Apps, Inputs, Devices, Settings screens
    ├── theme/                         # AMOLED Black, Charcoal Cards & Vestel Red design system
    └── viewmodel/
        ├── MainViewModel.kt           # Single source of truth (StateFlow) & UI event handlers
        └── VolumeSliderController.kt  # Stabilized volume slider coalescing & cancellation controller
```

Additional technical documentation is available in `docs/`:
- [Architecture Design (`docs/ARCHITECTURE.md`)](docs/ARCHITECTURE.md)
- [Protocol Research (`docs/PROTOCOL_RESEARCH.md`)](docs/PROTOCOL_RESEARCH.md)
- [Pairing Audit & Repair Report (`docs/PAIRING_AUDIT_20261017_TR.md`)](docs/PAIRING_AUDIT_20261017_TR.md)
- [Compatibility Matrix (`docs/COMPATIBILITY.md`)](docs/COMPATIBILITY.md)

---

## 12. Android Permissions

The application requests only the minimal permissions required for local network communication and tactile feedback (`app/src/main/AndroidManifest.xml`):

| Permission | Purpose |
| :--- | :--- |
| `android.permission.INTERNET` | Communicates with the TV over local TLS (`6466`/`6467`), HTTP DIAL (`8008`), and UDP sockets. |
| `android.permission.ACCESS_NETWORK_STATE` | Checks active local network connectivity. |
| `android.permission.ACCESS_WIFI_STATE` | Verifies Wi-Fi connection status for LAN discovery. |
| `android.permission.CHANGE_WIFI_MULTICAST_STATE` | Acquires a multicast lock during mDNS (`_androidtvremote2._tcp`) and SSDP discovery scans. |
| `android.permission.VIBRATE` | Provides tactile haptic feedback when pressing remote control buttons. |

> The application does **not** request camera, microphone, location, contacts, or external storage permissions.

---

## 13. Security and Privacy

- **100% Local Network (LAN) Operation:** No telemetry, analytics, or cloud servers are contacted. All traffic stays strictly on the user's local Wi-Fi network.
- **Android Keystore & mTLS:** The client RSA private key (`2048-bit`) is generated inside the Android Keystore and never leaves secure storage.
- **SHA-256 Certificate Pinning:** The paired TV's X.509 certificate fingerprint is persisted and verified on every subsequent Port `6466` control session.
- **Sanitized Diagnostics & Screenshots:** The in-app `Copy Diagnostics` feature never logs PIN codes, private keys, or pairing secrets. Local IP addresses in documentation screenshots are redacted (`192.168.1.XXX`).

---

## 14. Build Instructions

1. **Prerequisites:** JDK 17 or JDK 21, Android SDK Platform 35.
2. **Clone the Repository:**
   ```powershell
   git clone https://github.com/Terabithia1572/Vestel-Remote-Android.git
   cd Vestel-Remote-Android
   ```
3. **Build the Debug APK:**
   - Windows PowerShell:
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   - macOS / Linux:
     ```bash
     ./gradlew assembleDebug
     ```
4. **Generated APK Location:**
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 15. Testing

The repository includes **37 unit and regression tests** covering Protobuf framing, SHA-256 PIN cryptography, pairing state machine validation, real loopback mTLS socket sessions, volume slider coalescing/cancellation, DIAL XML parsing, and secret-redaction checks:

```powershell
.\gradlew.bat testDebugUnitTest
```

---

## 16. Known Limitations

- **App Discovery (DIAL vs. Full Package Enumeration):** The Android TV Remote v2 (`Port 6466`) protocol does not provide a list of all installed APK packages on the TV. Only DIAL-supported applications (`Port 8008`) can be queried for installed/running status without ADB; other streaming apps are launched via Deep Link shortcuts.
- **Unsupported Legacy Models:** Non-Android Vestel Smart Center televisions (Linux-based MB90, MB100, MB130, MB211 boards) use a different proprietary protocol and are not supported.
- **Deep Standby Wake-on-LAN:** If the television powers down its Wi-Fi interface during deep sleep, Wake-on-LAN packets may depend on the TV's network standby setting.
- **DHCP IP Changes:** If the router assigns a new IPv4 address to the TV via DHCP, re-scanning on the **Devices** screen may be required.

---

## 17. Credits and Open-Source Notices

- **Original macOS Reference Project:** [LGwebOS-menubar](https://github.com/gabrielgreco1/LGwebOS-menubar) by **Gabriel Greco** (MIT License). Yunus İNAN does not claim authorship of the original macOS application; original copyright notices and the MIT license are preserved in [`LICENSE`](LICENSE) and [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).
- **Android TV Remote v2 Protocol References:**
  - [tronikos/androidtvremote2](https://github.com/tronikos/androidtvremote2) (Apache License 2.0)
  - [louis49/androidtv-remote](https://github.com/louis49/androidtv-remote) (MIT License)
  - [REA Framework (morluto/rea)](https://github.com/morluto/rea) (MIT License)
  - Android Open Source Project (AOSP) Jetpack & Conscrypt (Apache License 2.0)

See [`LICENSE`](LICENSE), [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md), and [`docs/THIRD_PARTY_NOTICES.md`](docs/THIRD_PARTY_NOTICES.md) for complete license texts and notices.

---

## 18. License and Developer Information

- **License:** [MIT License](LICENSE)
- **Developer:** **Yunus İNAN**
- **Attribution:** *Developed and adapted for Android TV by Yunus İNAN*
- **GitHub Profile:** [https://github.com/Terabithia1572](https://github.com/Terabithia1572)
- **Repository:** [https://github.com/Terabithia1572/Vestel-Remote-Android](https://github.com/Terabithia1572/Vestel-Remote-Android)

> **Disclaimer:** This project is an independent open-source effort and is not affiliated with, endorsed by, or sponsored by **Vestel Elektronik Sanayi ve Ticaret A.Ş.** or **Google LLC**. *Vestel*, *Android TV*, *Google TV*, and *Samsung Galaxy* are registered trademarks of their respective owners.
