# VESTEL REMOTE — UYUMLULUK VE GEÇİŞ MATRİSİ (MIGRATION MATRIX)

**Geliştirici:** Yunus İNAN  
**Hedef Cihaz:** Samsung Galaxy S24 Ultra  
**Hedef Televizyon:** Vestel 55QG9960 Android TV  

---

## 1. MİGRASYON MATRİSİ (LGwebOS-menubar -> Vestel Remote)

Orijinal Swift/macOS projesi ile Android TV için geliştirilen Vestel Remote arasındaki özellik karşılaştırması ve sınıflandırması:

| Orijinal Özellik | LGwebOS-menubar (macOS) | Vestel Remote (Android) | Sınıflandırma |
|---|---|---|---|
| **Cihaz Keşfi** | SSDP M-SEARCH (`urn:lge-com`) | mDNS (`_androidtvremote2._tcp`) + SSDP (DIAL) + Subnet Scan | **Requires Android implementation** |
| **Güvenlik & Eşleştirme** | WebSocket port 3001, ekranda popup | TLS port 6467, 6 haneli PIN, SHA-256 Secret, Android Keystore | **Requires Android implementation** |
| **Kumanda Navigasyonu** | WebOS JSON RPC (`button/click`) | Android TV Remote v2 (Protobuf, Android KeyEvent KeyCodes) | **Requires Vestel protocol investigation** |
| **Dokunmatik Yüzey** | WebOS Pointer WebSocket | Compose Gesture Touchpad (Delta sürükleme & dokunma) | **Requires Android implementation** |
| **Uygulama Başlatıcı** | LG Content Store App ID'leri | DIAL (Port 8008) + Android AppLink URI Intent | **Requires Vestel protocol investigation** |
| **HDMI Giriş Kontrolü** | LG WebOS input switching | Android TV Input KeyEvent + DIAL kaynak geçişi | **Requires Vestel protocol investigation** |
| **Ses ve Sessiz (Mute)** | WebOS volume API | Android KeyEvent Volume Rockers + Slider | **Directly reusable concept** |
| **Bekleme / Uyandırma** | Wake-on-LAN UDP port 9 | UDP port 9 Magic Packet (Broadcast + Direct IP) | **Directly reusable concept** |
| **Arayüz Tasarımı** | macOS Menubar AppKit / SwiftUI | Jetpack Compose + Material 3 (Samsung S24 Ultra Optimize) | **Requires Android implementation** |
| **Olay Günlüğü** | Local JSON Event Journal | Gerçek zamanlı StateFlow Protokol Günlüğü | **Directly reusable concept** |
| **Telif & Lisans** | MIT Lisansı (Gabriel Greco) | Orijinal atıflar korunarak Yunus İNAN adaptasyonu | **Directly reusable concept** |
| **Doğrulama Durumu** | LG webOS 5.0 | Vestel 55QG9960 Android TV 11 | **Hardware validation required** |

---

## 2. TELEVİZYON UYUMLULUK KAPSAMI

### 2.1 Doğrudan Hedeflenen Model
- **Vestel 55QG9960 Android TV (4K QLED):**
  - İşletim Sistemi: Android TV 11 (Google TV arayüzü)
  - Android TV Remote v2 (port 6466/6467) ve DIAL (port 8008) protokollerini tam destekler.

### 2.2 Uyumlu Diğer Vestel ve Kardeş Marka Modelleri
Vestel tesislerinde üretilen ve Android TV / Google TV işletim sistemi barındıran tüm modeller uygulamayla uyumludur:
- Vestel Q9900, Q9700, Q9500 QLED Serisi
- Vestel UA9600, UA9500 4K UHD Android TV Serisi
- Vestel O9900 OLED Android TV Serisi
- Toshiba, Daewoo, Regal, JVC ve Telefunken marka Vestel üretim Android TV'ler.

### 2.3 Desteklenmeyen Eski Modeller (Kısıtlamalar)
- **Vestel Linux / Smart Center (MB90, MB100, MB130, MB211 Anakartlar):**
  - Bu modeller Android TV değildir; kendi tescilli Vestel Smart Center protokolünü (UDP port 56789) kullanırlar. Standart Android TV Remote v2 desteklemezler.

---

## 3. AKILLI TELEFON UYUMLULUK KAPSAMI

### 3.1 Samsung Galaxy S24 Ultra (Birincil Cihaz)
- **Ekran:** 6.8 inç Dynamic AMOLED 2X, 120Hz, 3120x1440 çözünürlük.
- **Optimizasyonlar:**
  - Saf siyah (`#09090C`) AMOLED teması.
  - Kamera deliği ve sistem çubuğu kenar boşlukları (`edge-to-edge`).
  - One UI dokunsal titreşim (Haptic) motoru desteği.

### 3.2 Genel Android Cihazlar
- Minimum SDK: Android 8.0 Oreo (API Level 26)
- Hedef SDK: Android 15 (API Level 35)
- Android eko-sistemindeki cihazların %99.5+'ini kapsar.
