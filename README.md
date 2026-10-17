<div align="center">

# 📺 Vestel Remote — Android TV Akıllı Kumanda

**Vestel 55QG9960 4K QLED Android TV ve uyumlu Android TV / Google TV televizyonları yerel Wi-Fi ağı üzerinden kontrol etmek için geliştirilmiş, donanım üzerinde doğrulanmış yerel (native) Android uygulaması.**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Target Phone](https://img.shields.io/badge/Doğrulanan%20Telefon-Samsung%20Galaxy%20S24%20Ultra-1428A0?logo=samsung&logoColor=white)](#3-doğrulanan-donanım-verified-hardware)
[![Target TV](https://img.shields.io/badge/Doğrulanan%20TV-Vestel%2055QG9960%20(GoogleTV7288)-E30613)](#3-doğrulanan-donanım-verified-hardware)
[![Release](https://img.shields.io/badge/Sürüm-v1.0.0--beta.1-orange)](https://github.com/Terabithia1572/Vestel-Remote-Android/releases)
[![License: MIT](https://img.shields.io/badge/Lisans-MIT-green.svg)](LICENSE)

**Android uygulaması geliştirme ve Vestel Android TV uyarlaması: [Yunus İNAN](https://github.com/Terabithia1572)**  
Orijinal macOS menü çubuğu referans projesi: [LGwebOS-menubar — Gabriel Greco](https://github.com/gabrielgreco1/LGwebOS-menubar)

🌐 **Dil / Language:** **🇹🇷 Türkçe** | **[🇬🇧 English (README_EN.md)](README_EN.md)**

---

### 📱 Uygulama Önizlemesi (Samsung Galaxy S24 Ultra & Vestel 55QG9960 `GoogleTV7288`)

<table>
  <tr>
    <td align="center" width="33%">
      <img src="docs/screenshots/dashboard.jpg" alt="Ana Kontrol Paneli" width="260"/><br/>
      <b>Ana Kontrol Paneli (Panel)</b><br/>
      <sub>Canlı <code>GoogleTV7288</code> bağlantısı ve protokol günlüğü</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/remote.jpg" alt="Uzaktan Kumanda D-Pad" width="260"/><br/>
      <b>Uzaktan Kumanda (D-Pad)</b><br/>
      <sub>Haptic destekli yön, ses/kanal mandalları ve medya tuşları</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/devices.jpg" alt="Cihaz Yönetimi ve Eşleştirme" width="260"/><br/>
      <b>Cihaz Yönetimi ve Keşif</b><br/>
      <sub>mDNS / SSDP tarama ve mTLS PIN eşleştirme</sub>
    </td>
  </tr>
</table>

</div>

---

## 📋 İçindekiler

1. [Proje Tanıtımı](#1-proje-tanıtımı)
2. [Özellikler](#2-özellikler)
3. [Doğrulanan Donanım](#3-doğrulanan-donanım)
4. [Teknoloji Yığını](#4-teknoloji-yığını)
5. [Ekran Görüntüleri](#5-ekran-görüntüleri)
6. [Kurulum Talimatları](#6-kurulum-talimatları)
7. [Eşleştirme Talimatları](#7-eşleştirme-talimatları)
8. [Android İzinleri](#8-android-izinleri)
9. [Ağ Gereksinimleri](#9-ağ-gereksinimleri)
10. [Mimari](#10-mimari)
11. [Geliştirme Ortamı Kurulumu](#11-geliştirme-ortamı-kurulumu)
12. [Derleme Talimatları](#12-derleme-talimatları)
13. [Test Süreçleri](#13-test-süreçleri)
14. [Bilinen Sorunlar (Known Issues)](#14-bilinen-sorunlar-known-issues)
15. [Uyumluluk Sınırları](#15-uyumluluk-sınırları)
16. [Güvenlik ve Gizlilik](#16-güvenlik-ve-gizlilik)
17. [Açık Kaynak Atıfları](#17-açık-kaynak-atıfları)
18. [Geliştirici Bilgileri](#18-geliştirici-bilgileri)

---

## 1. Proje Tanıtımı

**Vestel Remote**, **Vestel 55QG9960 4K QLED Android TV** başta olmak üzere Android TV Remote Service v2 protokolünü destekleyen televizyonları yerel Wi-Fi ağı üzerinden kontrol etmek amacıyla geliştirilmiş modern bir Android akıllı telefon uygulamasıdır.

Proje, Gabriel Greco'nun macOS/LG webOS için geliştirdiği [LGwebOS-menubar](https://github.com/gabrielgreco1/LGwebOS-menubar) projesinden ilham alınarak; tamamen **Kotlin**, **Jetpack Compose**, **Material 3** ve **Android TV Remote v2 (mTLS / Protobuf)** protokol altyapısıyla yerel Android platformu için yeniden tasarlanmış ve **Yunus İNAN** tarafından Vestel Android TV donanımına uyarlanmıştır.

Uygulama, **Samsung Galaxy S24 Ultra** üzerinde gerçek **Vestel 55QG9960 (`GoogleTV7288`)** televizyon ile eşleştirilerek donanım üzerinde doğrulanmıştır.

---

## 2. Özellikler

- **🔍 Otomatik TV Keşfi (mDNS & SSDP):** Yerel ağdaki `_androidtvremote2._tcp` (Android TV Remote v2) ve SSDP/DIAL servislerini otomatik olarak tarar; ayrıca manuel IPv4 ve MAC adresi girişini destekler.
- **🔐 PIN Doğrulamalı Karşılıklı TLS (mTLS) Eşleştirme:** Port `6467` üzerinden Android Keystore donanım destekli 2048-bit RSA istemci sertifikası ve televizyon ekranında beliren 6 karakterli HEX PIN kodu ile güvenli eşleştirme yapar.
- **📌 Sunucu Sertifikası Sabitleme (Certificate Pinning):** Eşleştirme sırasında alınan TV X.509 sertifikasının SHA-256 parmak izini kaydeder ve Port `6466` üzerindeki kumanda oturumlarında doğrular.
- **🏠 Ana Kontrol Paneli (Panel):** Bağlı televizyonun adını (`GoogleTV7288`), modelini (`Vestel 55QG9960`), aktif giriş kaynağını, ses durumunu, hızlı işlem kısayollarını (Ana Sayfa, Geri, Google Asistan, Wake-on-LAN, Cihazlar) ve gerçek zamanlı **Protokol Olay Günlüğü** akışını gösterir.
- **🎮 Tam Fonksiyonlu Uzaktan Kumanda:**
  - **D-Pad Modu:** Dokunsal geri bildirimli (Haptic) Yukarı, Aşağı, Sol, Sağ ve OK tuşları.
  - **Dokunmatik Yüzey (Touchpad) Modu:** Kaydırma ve dokunma hareketleriyle televizyon arayüzünde gezinme.
  - **Ses ve Kanal Mandalları (Rockers):** Fiziksel kumanda ergonomisinde dikey `VOL +/-` ve `CH +/-` kontrolleri.
  - **Sistem ve Medya Tuşları:** Güç, Giriş Kaynağı, Google Asistan, Menü, Geri, Ana Sayfa, Sessiz (Mute), Bilgi (Info), Geri Sar, Oynat/Duraklat, Durdur, İleri Sar.
  - **Renk ve Numara Tuşları:** HbbTV/yayın etkileşimi için Kırmızı, Yeşil, Sarı, Mavi tuşlar ve açılır-kapanır `0–9` sayısal kanal tuş takımı.
- **🚀 Uygulama Başlatıcı (Apps):** YouTube, Netflix, Prime Video, Disney+, Spotify, Exxen, BluTV, Gain, TOD ve Twitch kısayolları, favori sabitleme ve uygulama arama alanı.
- **🔌 Giriş Kaynakları (Inputs):** HDMI 1, HDMI 2 (eARC), HDMI 3, Canlı TV (Uydu/Karasal) ve AV / Kompozit kaynak ekranı.
- **🩺 Bağlantı Teşhis Aracı (Diagnostics):** Kişisel veri, PIN veya özel anahtar sızdırmadan son bağlantı/eşleştirme denemelerinin protokol aşamalarını panoya kopyalama (`Teşhisi Kopyala`) ve port erişilebilirlik kontrolü.
- **⚙️ Ayarlar ve Sanal TV Modu:** Türkçe / İngilizce dil seçimi, Saf AMOLED Koyu / Açık tema, dokunsal titreşim kontrolü ve fiziksel TV olmadan test yapılmasını sağlayan Sanal TV (Mock) modu.

---

## 3. Doğrulanan Donanım

Bu sürüm (`v1.0.0-beta.1`), emülatör testlerinin ötesinde gerçek fiziksel donanımlar üzerinde uçtan uca test edilmiş ve doğrulanmıştır:

| Bileşen | Doğrulanan Cihaz / Model | Durum |
| :--- | :--- | :--- |
| **Akıllı Telefon** | **Samsung Galaxy S24 Ultra** (Dynamic AMOLED 2X, 1440×3120, One UI) | ✅ Doğrulandı |
| **Televizyon** | **Vestel 55QG9960 4K QLED Android TV** (Ağ adı: `GoogleTV7288`) | ✅ Doğrulandı |
| **Cihaz Keşfi** | Yerel Wi-Fi üzerinde mDNS / SSDP ile `GoogleTV7288` tespiti | ✅ Doğrulandı |
| **Güvenli Eşleştirme** | Port `6467` mTLS el sıkışması ve TV ekranındaki 6 haneli PIN doğrulaması | ✅ Doğrulandı |
| **Kimlik Doğrulamalı Oturum** | Port `6466` `RemoteConfigure` → `RemoteSetActive` → `RemoteStart` oturumu | ✅ Doğrulandı |
| **Ana Kumanda İşlevleri** | D-Pad yönlendirme, OK, Geri, Ana Sayfa, Ses +/-, Sessiz, Güç ve diğer ekranlar | ✅ Doğrulandı |

---

## 4. Teknoloji Yığını

- **Dil:** Kotlin `2.0.21` (JVM Target 17)
- **UI Çerçevesi:** Jetpack Compose (BOM `2024.10.01`) & Material 3
- **Mimari:** Clean Architecture + MVVM (`StateFlow`, `ViewModel`, Kotlin Coroutines `1.9.0`)
- **Navigasyon:** AndroidX Navigation Compose `2.8.3`
- **Kalıcı Veri Saklama:** AndroidX DataStore Preferences `1.1.1`
- **Kriptografi & Güvenlik:**
  - Android Keystore (`RSA 2048-bit`, `DIGEST_NONE`, `SHA-256/384/512`, `PKCS#1` & `RSA-PSS` TLS imzalama desteği)
  - Özel `X509ExtendedKeyManager` ve SHA-256 sertifika parmak izi sabitlemeli `X509TrustManager`
- **Ağ Protokolleri:**
  - Android TV Remote Service v2 (TLS Port `6466` & `6467`, varint-framed Protobuf)
  - DIAL REST (HTTP Port `8008`)
  - mDNS / Android `NsdManager` (`_androidtvremote2._tcp`) & SSDP (`239.255.255.250:1900`)
  - Wake-on-LAN (UDP Port `9` Magic Packet)
- **Derleme Sistemi:** Gradle `8.14` (Kotlin DSL) & Android Gradle Plugin `8.8.0`
- **Minimum / Hedef SDK:** `minSdk = 26` (Android 8.0) / `targetSdk = 35` (`compileSdk = 35`, Android 15)

---

## 5. Ekran Görüntüleri

Aşağıdaki tüm ekran görüntüleri, **Vestel 55QG9960 (`GoogleTV7288`)** televizyona bağlı **Samsung Galaxy S24 Ultra** cihazından doğrudan alınmıştır (yerel ağ IP adresinin son okteti gizlilik amacıyla `192.168.1.XXX` olarak maskelenmiştir):

<table>
  <tr>
    <td align="center" width="25%">
      <img src="docs/screenshots/dashboard.jpg" alt="Panel Ekranı" width="220"/><br/>
      <b>1. Panel (Dashboard)</b><br/>
      <sub>Canlı durum, ses çubuğu, hızlı işlemler ve protokol olay günlüğü</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/remote.jpg" alt="Kumanda D-Pad Ekranı" width="220"/><br/>
      <b>2. Kumanda (D-Pad)</b><br/>
      <sub>D-Pad navigasyon, ses/kanal mandalları, medya ve renk tuşları</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/touchpad.jpg" alt="Dokunmatik Yüzey Ekranı" width="220"/><br/>
      <b>3. Kumanda (Dokunmatik)</b><br/>
      <sub>Kaydırma ve dokunma hareketli hassas dokunmatik yüzey</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/apps.jpg" alt="Uygulamalar Ekranı" width="220"/><br/>
      <b>4. Uygulamalar (Apps)</b><br/>
      <sub>Favori uygulamalar, hızlı arama ve TV uygulama başlatıcı</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="25%">
      <img src="docs/screenshots/inputs.jpg" alt="Giriş Kaynakları Ekranı" width="220"/><br/>
      <b>5. Girişler (Inputs)</b><br/>
      <sub>HDMI 1, HDMI 2 (eARC), HDMI 3, Canlı TV ve AV seçimi</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/devices.jpg" alt="Cihaz Yönetimi Ekranı" width="220"/><br/>
      <b>6. Cihazlar (Devices)</b><br/>
      <sub>Ağdaki TV'ler (<code>GoogleTV7288</code>), kayıtlı cihazlar ve manuel IP</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/settings.jpg" alt="Ayarlar Ekranı" width="220"/><br/>
      <b>7. Ayarlar (Settings)</b><br/>
      <sub>Dil, AMOLED tema, haptic titreşim ve teşhis kopyalama</sub>
    </td>
    <td align="center" width="25%">
      <img src="docs/screenshots/about.jpg" alt="Hakkında Ekranı" width="220"/><br/>
      <b>8. Hakkında (About)</b><br/>
      <sub>Sürüm bilgisi, geliştirici atfı ve açık kaynak bildirimleri</sub>
    </td>
  </tr>
</table>

---

## 6. Kurulum Talimatları

### Yöntem 1: GitHub Releases Üzerinden Hazır APK Kurulumu
1. Bu deponun **[Releases](https://github.com/Terabithia1572/Vestel-Remote-Android/releases)** sayfasından en güncel `apk` dosyasını telefonunuza indirin.
2. Android telefonunuzda dosyaya dokunun ve gerekirse **"Bilinmeyen uygulamaları yükle"** iznini onaylayın.

### Yöntem 2: ADB (Android Debug Bridge) ile Kurulum
Telefonunuzda **USB Hata Ayıklama** açıkken bilgisayarınızdan şu komutu çalıştırın:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 7. Eşleştirme Talimatları

1. **Aynı Wi-Fi Ağına Bağlanın:** Samsung Galaxy S24 Ultra (veya Android telefonunuz) ile Vestel 55QG9960 televizyonunuzun aynı yerel ağda olduğundan emin olun.
2. **Televizyonu Açık Tutun:** Televizyonu açık konuma getirin.
3. **Cihazlar Sekmesine Gidin:** Uygulamanın alt gezinme çubuğundan **Cihazlar** sekmesini açın (Ayarlar'da *Geliştirici Modu / Sanal TV Testi* seçeneğinin kapalı olduğundan emin olun).
4. **Televizyonu Bulun:** **"Ağda Ara (mDNS / SSDP)"** düğmesine dokunun. Televizyonunuz (ör. `GoogleTV7288`) listede belirecektir. Ağınız multicast paketlerini kısıtlıyorsa **Manuel IP ile Bağlan** bölümünden televizyonun IP adresini girin.
5. **Eşleştirmeyi Başlatın:** Cihaz kartındaki **"Eşleştir"** düğmesine dokunun.
6. **6 Haneli PIN Kodunu Girin:** Televizyon ekranında 6 karakterli onaltılık (HEX) bir eşleştirme kodu belirecektir. Bu kodu telefondaki eşleştirme penceresine girip **Onayla** düğmesine dokunun.
7. **Kontrole Başlayın:** Durum **"AKTİF / Bağlandı"** olarak güncellendiğinde **Panel** veya **Kumanda** sekmesinden televizyonunuzu hemen kontrol edebilirsiniz. Sonraki açılışlarda PIN girmeye gerek kalmadan kayıtlı sertifika ile doğrudan **Bağlan** düğmesini kullanabilirsiniz.

---

## 8. Android İzinleri

Uygulama yalnızca yerel ağ üzerinden televizyon keşfi, bağlantısı ve dokunsal geri bildirim için gerekli minimum izinleri talep eder (`app/src/main/AndroidManifest.xml`):

| İzin Adı | Kullanım Amacı |
| :--- | :--- |
| `android.permission.INTERNET` | Televizyon ile TLS (`6466`/`6467`), HTTP DIAL (`8008`) ve UDP üzerinden yerel ağ iletişimi kurmak. |
| `android.permission.ACCESS_NETWORK_STATE` | Aktif yerel ağ bağlantısını kontrol etmek. |
| `android.permission.ACCESS_WIFI_STATE` | Wi-Fi bağlantı durumunu ve yerel ağ bilgilerini doğrulamak. |
| `android.permission.CHANGE_WIFI_MULTICAST_STATE` | mDNS (`_androidtvremote2._tcp`) ve SSDP televizyon keşfi sırasında multicast paketlerini alabilmek. |
| `android.permission.VIBRATE` | Kumanda tuşlarına basıldığında dokunsal titreşim (haptic feedback) sağlamak. |

> Uygulama kamera, mikrofon, konum, rehber veya harici depolama izni **talep etmez**.

---

## 9. Ağ Gereksinimleri

- Telefon ve televizyon aynı yerel alt ağda (subnet) bulunmalı ve modem/router üzerinde **AP İzolasyonu (Client Isolation)** kapalı olmalıdır.
- Yerel ağda aşağıdaki portların erişilebilir olması gerekir:

| Port | Protokol | Servis | Açıklama |
| :--- | :--- | :--- | :--- |
| **6466** | TCP / TLS | Android TV Remote Control v2 | Kimlik doğrulamalı kumanda oturumu, tuş gönderimi, ses ve durum mesajları |
| **6467** | TCP / TLS | Android TV Remote Pairing v2 | Karşılıklı TLS sertifika değişimi ve 6 haneli PIN ile eşleştirme |
| **8008** | TCP / HTTP | DIAL (Discovery & Launch) | Uygulama başlatma ve durum sorgulama |
| **5353** | UDP | mDNS / DNS-SD | `_androidtvremote2._tcp.local.` otomatik cihaz keşfi |
| **1900** | UDP | SSDP / UPnP | Yerel ağda UPnP / DIAL cihaz keşfi |
| **9** | UDP | Wake-on-LAN (WoL) | Bekleme modundaki televizyonu MAC adresiyle uyandırma |

---

## 10. Mimari

Proje, sorumlulukların net biçimde ayrıldığı **Clean Architecture + MVVM** yapısını kullanır:

```text
app/src/main/java/com/yunusinan/vestelremote/
├── MainActivity.kt                  # Tek-Activity Compose giriş noktası ve alt navigasyon
├── data/
│   ├── crypto/
│   │   ├── CertificateHelper.kt     # Android Keystore 2048-bit RSA & X.509 sertifika üretimi
│   │   ├── PairingCrypto.kt         # SHA-256(ClientMod||ClientExp||ServerMod||ServerExp||PIN)
│   │   └── TlsSocketFactoryHelper.kt# mTLS KeyManager & SHA-256 sertifika sabitlemeli TrustManager
│   ├── datastore/
│   │   └── PreferencesManager.kt    # Kayıtlı cihazlar, sertifika parmak izleri ve kullanıcı ayarları
│   ├── protocol/
│   │   ├── AndroidTvRemoteV2Client.kt # Port 6467 eşleştirme & Port 6466 canlı kumanda oturumu
│   │   ├── RemoteProtocol.kt        # Polo/Remote v2 durum makinesi ve paket doğrulayıcılar
│   │   ├── ProtoUtils.kt            # Hafif, çerçeve korumalı Protobuf varint kodlayıcı/çözücü
│   │   ├── TvDiscoveryManager.kt    # NsdManager (mDNS) ve SSDP keşif yöneticisi
│   │   ├── DialClient.kt            # Port 8008 DIAL uygulama başlatıcı
│   │   ├── WakeOnLan.kt             # UDP Magic Packet gönderici
│   │   ├── AttemptDiagnostics.kt    # Gizlilik korumalı bağlantı teşhis kaydedici
│   │   └── MockTvClient.kt          # Donanımsız testler için sanal TV istemcisi
│   └── repository/
│       ├── TvRepository.kt          # Domain-Data sözleşmesi
│       └── TvRepositoryImpl.kt      # Oturum, keşif ve kalıcılık orkestrasyonu
├── domain/model/                    # TvDevice, TvCommand, ConnectionStatus, ProtocolEvent vb.
└── ui/
    ├── components/                  # DPadControl, TouchpadControl, VolumeRockers, PairingDialog vb.
    ├── screens/                     # Dashboard, Remote, Apps, Inputs, Devices, Settings ekranları
    ├── theme/                       # AMOLED Siyahı ve Vestel Kırmızısı Material 3 teması
    └── viewmodel/MainViewModel.kt   # Tekil gerçeklik kaynağı (StateFlow) ve kullanıcı etkileşimleri
```

Detaylı teknik belgeler için `docs/` dizinine göz atabilirsiniz:
- [Yazılım Mimarisi (`docs/ARCHITECTURE.md`)](docs/ARCHITECTURE.md)
- [Protokol Araştırması (`docs/PROTOCOL_RESEARCH.md`)](docs/PROTOCOL_RESEARCH.md)
- [Eşleştirme Denetimi ve Onarım Raporu (`docs/PAIRING_AUDIT_20261017_TR.md`)](docs/PAIRING_AUDIT_20261017_TR.md)
- [Uyumluluk Matrisi (`docs/COMPATIBILITY.md`)](docs/COMPATIBILITY.md)

---

## 11. Geliştirme Ortamı Kurulumu

1. **Ön Koşullar:**
   - **JDK 17** veya **JDK 21** (Android Studio Ladybug / Meerkat JBR önerilir)
   - **Android SDK Platform 35** ve Build-Tools
2. **Depoyu Klonlayın:**
   ```powershell
   git clone https://github.com/Terabithia1572/Vestel-Remote-Android.git
   cd Vestel-Remote-Android
   ```
3. **Android SDK Yolu:**
   Android Studio projeyi açtığında `local.properties` dosyasını otomatik oluşturur (bu dosya güvenlik gereği `.gitignore` içindedir ve depoya dahil edilmez).

---

## 12. Derleme Talimatları

Windows PowerShell üzerinden Debug APK derlemek için:

```powershell
.\gradlew.bat assembleDebug
```

macOS / Linux üzerinden derlemek için:

```bash
./gradlew assembleDebug
```

Derlenen APK çıktısı şu konumda oluşturulur:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 13. Test Süreçleri

Proje; Protobuf çerçeveleme, SHA-256 PIN kriptografisi, eşleştirme durum makinesi, gerçek loopback mTLS soket oturumu, ses kaydırıcısı hız sınırlama/iptal mantığı, DIAL XML ayrıştırma ve gizlilik sızıntısı kontrollerini kapsayan **37 adet birim ve regresyon testi** içerir.

Testleri çalıştırmak için:

```powershell
.\gradlew.bat testDebugUnitTest
```

Test sınıfları (`app/src/test/java/com/yunusinan/vestelremote/`):
- `VolumeAndAppDiscoveryTest` — Ses kaydırıcısı kare birleştirme, bırakma/yön değişimi anında iptal ve DIAL XML ayrıştırma testleri
- `protocol/ProtoUtilsTest` — Varint ve çerçeve bütünlüğü testleri
- `protocol/PairingCryptoTest` — RSA modül/üs ve PIN SHA-256 secret vektör testleri
- `protocol/PairingStateMachineTest` — Eşleştirme ACK ve hata durumu geçiş testleri
- `protocol/ReferenceProtocolTest` — Resmi Android TV Remote v2 protokol mesaj şeması uyumluluk testleri
- `protocol/RemoteSessionRegressionTest` — Loopback mTLS, sertifika sabitleme, `RemoteStart`, ping/volume ve iptal testleri
- `protocol/TvCommandEncodingTest` — Android `KeyEvent` tuş kodu doğrulama testleri
- `protocol/MockTvClientTest` — Sanal TV durum makinesi testleri

---

## 14. Son Güncellemeler ve Protokol Notları

Gerçek donanım doğrulaması (**Samsung Galaxy S24 Ultra** ↔ **Vestel 55QG9960 `GoogleTV7288`**) sonrasında aşağıdaki iyileştirmeler tamamlanmıştır:

1. **Kararlı Ses Kaydırıcısı (`VolumeSliderController`):**
   - Android TV Remote v2 protokolü yalnızca artımlı `VOLUME_UP` / `VOLUME_DOWN` komutlarını desteklediğinden, `Panel` ekranındaki ses kaydırıcısına kare birleştirme (coalescing), `140ms` hız sınırı, parmak çekildiğinde (`onValueChangeFinished`) veya yön değiştirildiğinde bekleyen komutları anında iptal etme mekanizması ve hassas `-` / `+` adım tuşları eklenmiştir.
2. **DIAL Uygulama Doğrulaması ve Özel Deep Link Başlatıcı:**
   - `Uygulamalar` ekranı, DIAL (`Port 8008`) protokolünü destekleyen uygulamaların TV üzerindeki varlığını ve çalışma durumunu (`running` / `stopped`) doğrular. Android TV Remote v2 (`Port 6466`) protokolü ADB olmadan tam paket listesi sunmadığından, DIAL dışı uygulamalar şeffaf biçimde **Hızlı Başlatma Kısayolları (Deep Link)** altında ayrıştırılmış ve herhangi bir paket/URI bağlantısını açabilen **Özel Deep Link** başlatıcısı eklenmiştir.
3. **Uyarlanabilir 6 Sekmeli Alt Gezinme Çubuğu (`VestelBottomNavigationBar`):**
   - Tüm 6 sekme (`Panel`, `Kumanda`, `Uygulamalar`, `Girişler`, `Cihazlar`, `Ayarlar`) tek satır etiket garantisi ve sistem hareket çubuğu (gesture inset) uyumuyla yeniden tasarlanmıştır.

---

## 15. Uyumluluk Sınırları

- **Desteklenen Televizyonlar:** Vestel 55QG9960 başta olmak üzere **Android TV** veya **Google TV** işletim sistemiyle çalışan ve `Android TV Remote Service v2` (`_androidtvremote2._tcp`) barındıran tüm Vestel ve kardeş marka (Toshiba, Regal, JVC, Daewoo, Telefunken vb. Android TV) televizyonlar.
- **Desteklenmeyen Modeller:** Android TV işletim sistemi içermeyen, Linux tabanlı eski Vestel Smart Center anakartlı (MB90, MB100, MB130, MB211 vb.) televizyonlar bu protokolü desteklemez.
- **DHCP IP Değişimi:** Televizyonun yerel IP adresi modem tarafından değiştirilirse, cihazın yeniden taranması veya yeni IP adresi üzerinden tekrar bağlanılması gerekebilir.

---

## 16. Güvenlik ve Gizlilik

- ** %100 Yerel Ağ İletişimi:** Uygulama hiçbir bulut sunucusuna, analitik servisine veya üçüncü taraf API'ye veri göndermez. Tüm iletişim doğrudan telefon ile televizyon arasında yerel ağ (LAN) üzerinde gerçekleşir.
- **Donanım Korumalı Anahtarlar:** İstemci özel anahtarı Android Keystore içinde üretilir ve uygulama dışına çıkarılamaz.
- **Sızıntı Korumalı Teşhis:** `Teşhisi Kopyala` özelliği yalnızca bağlantı aşaması üst verilerini (metadata) içerir; PIN kodlarını, özel anahtarları, sertifika içeriklerini veya eşleştirme sırlarını (pairing secret) asla kaydetmez.
- **Depo Hijyeni:** Bu GitHub deposunda hiçbir gerçek eşleştirme anahtarı, PIN kodu, yerel IP adresi, `local.properties` veya imzalama anahtar deposu (keystore) yer almaz.

---

## 17. Açık Kaynak Atıfları

Bu proje açık kaynaklı yazılımlara ve protokol araştırmalarına dayanmaktadır:

- **Orijinal macOS Referans Projesi:** [LGwebOS-menubar](https://github.com/gabrielgreco1/LGwebOS-menubar) — **Gabriel Greco** (MIT Lisansı). Yunus İNAN, orijinal macOS uygulamasının yazarı olduğunu iddia etmez; orijinal telif bildirimleri ve MIT lisansı [`LICENSE`](LICENSE) ve [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) dosyalarında eksiksiz korunmuştur.
- **Android TV Remote v2 Protokol Referansları:**
  - [tronikos/androidtvremote2](https://github.com/tronikos/androidtvremote2) (Apache License 2.0)
  - [louis49/androidtv-remote](https://github.com/louis49/androidtv-remote) (MIT Lisansı)
  - [REA Framework (morluto/rea)](https://github.com/morluto/rea) (MIT Lisansı)
  - Android Open Source Project (AOSP) Jetpack & Conscrypt (Apache License 2.0)

Detaylı lisans metinleri için [`LICENSE`](LICENSE), [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) ve [`docs/THIRD_PARTY_NOTICES.md`](docs/THIRD_PARTY_NOTICES.md) dosyalarını inceleyebilirsiniz.

---

## 18. Geliştirici Bilgileri

- **Geliştirici:** **Yunus İNAN**
- **Katkı ve Rol:** *Android uygulaması geliştirme ve Vestel Android TV uyarlaması: Yunus İNAN*
- **GitHub Profili:** [https://github.com/Terabithia1572](https://github.com/Terabithia1572)
- **Proje Deposu:** [https://github.com/Terabithia1572/Vestel-Remote-Android](https://github.com/Terabithia1572/Vestel-Remote-Android)

> **Yasal Uyarı:** Bu proje bağımsız bir açık kaynak çalışmasıdır; **Vestel Elektronik Sanayi ve Ticaret A.Ş.** veya **Google LLC** ile resmi bir bağlantısı, ortaklığı veya onayı bulunmamaktadır. *Vestel*, *Android TV*, *Google TV* ve *Samsung Galaxy* ilgili hak sahiplerinin tescilli ticari markalarıdır.
