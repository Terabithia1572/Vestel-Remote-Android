# Third-Party Notices & Open-Source Attribution

**Application:** Vestel Remote  
**Developer:** **Yunus İNAN** ([https://github.com/Terabithia1572](https://github.com/Terabithia1572))  
- **TR:** *Android uygulaması geliştirme ve Vestel Android TV uyarlaması: Yunus İNAN*  
- **EN:** *Android application development and Vestel Android TV adaptation: Yunus İNAN*

---

## 1. Original Reference Project (LGwebOS-menubar)

This project was inspired by the product concepts of [LGwebOS-menubar](https://github.com/gabrielgreco1/LGwebOS-menubar) by **Gabriel Greco**, and adapted/re-engineered as a native Android application for Vestel Android TV devices by **Yunus İNAN**. Yunus İNAN does not claim authorship of the original macOS application.

```text
MIT License

Copyright (c) 2024-2026 Gabriel Greco and LGwebOS-menubar contributors
Copyright (c) 2026 Yunus İNAN (Android application development and Vestel Android TV adaptation)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 2. Android TV Remote v2 Protocol References (2026-10-17 Audit)

The pairing and remote wire behavior was checked against:

- [tronikos/androidtvremote2](https://github.com/tronikos/androidtvremote2/tree/e664a8e1f14ee458b14c646bfb3ea46f10367051), licensed under [Apache-2.0](https://github.com/tronikos/androidtvremote2/blob/e664a8e1f14ee458b14c646bfb3ea46f10367051/LICENSE).
- [louis49/androidtv-remote](https://github.com/louis49/androidtv-remote/tree/0e82d72ec067b6452e0e2266720ba5e023821bf6), licensed under [MIT](https://github.com/louis49/androidtv-remote/blob/0e82d72ec067b6452e0e2266720ba5e023821bf6/LICENSE).
- Android Keystore and [Conscrypt](https://github.com/google/conscrypt) public source and Android API documentation for delegated RSA signing behavior (Apache-2.0 source).
- [REA Framework (morluto/rea)](https://github.com/morluto/rea), licensed under MIT.

The Kotlin implementation is native to this project. No reference Python/JavaScript library or copied reference source is shipped in the production APK. Independent serialized test messages were generated from the reference protobuf definitions; their origin and regeneration procedure are documented in `app/src/test/resources/protocol/README.md`. Synthetic certificates and digest fixtures contain no real device credentials.

---

## 3. Android Open Source Project (AOSP) & Jetpack Libraries

The Android application uses the following libraries licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0):
- AndroidX Core KTX, Activity Compose, Lifecycle & ViewModel Compose
- AndroidX Jetpack Compose & Material 3
- AndroidX Navigation Compose
- AndroidX DataStore Preferences
- JetBrains Kotlin Standard Library & Kotlin Coroutines

---

## 4. Legacy Reference Notices (LGwebOS-menubar)

`Sources/LGwebOSMenubar/registration.json` contains the public LG test-client registration manifest distributed by [PyWebOSTV](https://github.com/supersaiyanmode/PyWebOSTV) under the [MIT License](https://github.com/supersaiyanmode/PyWebOSTV/blob/master/LICENSE) (`Copyright (c) 2017 Srivatsan Iyer`).

The LG, PlayStation, and Google Cast symbol assets in legacy macOS assets were rendered from [Simple Icons](https://simpleicons.org/), distributed under [CC0 1.0](https://github.com/simple-icons/simple-icons/blob/develop/LICENSE.md). The public [Connect SDK](https://github.com/ConnectSDK/Connect-SDK-iOS) and [vorssaint-utils](https://github.com/vorssaint/vorssaint-utils) informed the original macOS menu bar reference.

---

## 5. Trademark & Non-Affiliation Disclaimer

This project is an independent open-source community project and is not affiliated with, endorsed by, or sponsored by **Vestel Elektronik Sanayi ve Ticaret A.Ş.**, **Google LLC**, **LG Electronics**, or **Samsung Electronics**. All product names, trademarks, and registered trademarks are property of their respective owners.
