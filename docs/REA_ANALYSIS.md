# REA (REVERSE ENGINEERING ANYTHING) ENTEGRASYON VE PROTOKOL ANALİZ RAPORU

**Yazar / Araştırmacı:** Yunus İNAN  
**Tarih:** 17 Ekim 2026  
**Hedef Cihaz:** Vestel 55QG9960 Android TV  
**Araştırma Altyapısı:** REA Framework (https://github.com/morluto/rea)  

---

## 1. REA ÇERÇEVESİ VE ORTAM ENTEGRASYONU

REA (Reverse Engineering Anything), ikili dosyaları (native binaries), yönetilen derlemeleri (managed assemblies/PE), Android paketlerini (APK/DEX) ve ağ protokollerini incelemek için geliştirilmiş standartlaştırılmış bir tersine mühendislik ve analiz çerçevesidir.

### 1.1 Ortam ve MCP Desteği İncelemesi
- **Mevcut Çalışma Ortamı:** Windows 11 x64, OpenJDK 21, Android SDK 35, Antigravity Agent.
- **REA MCP Durumu:** REA sunucusu (`rea`), MCP (Model Context Protocol) üzerinden ortama entegre edilmiştir.
- **Doğrulama Komutu:** `binary_session` çağrısı gerçekleştirilmiş; sunucunun aktif olduğu ve `inspect_artifact`, `trace_dylib_resolution`, `inspect_managed_artifact`, `inspect_android_package` yeteneklerinin oturumda hazır bulunduğu doğrulanmıştır.
- **Yetkilendirme Sınırları:** Global ajan yapılandırması değiştirilmeden mevcut MCP bağlantısı kullanılmış; DRM korumaları, cihaz donanım kilitleri veya tescilli şifrelemeler bypass edilmemiş, yalnızca doğrulanabilir ve yetkili yerel ağ iletişim arayüzleri analiz edilmiştir.

---

## 2. ORİJİNAL PROJE KAYNAK ANALİZİ (LGwebOS-menubar)

Orijinal referans proje `LGwebOS-menubar` (Gabriel Greco), Swift ve macOS AppKit/SwiftUI ile yazılmış bir masaüstü durum çubuğu uygulamasıdır.

### 2.1 Orijinal Mimarinin Temel Bileşenleri
1. **Discovery (`Discovery.swift`):**
   - UDP soketi üzerinden `239.255.255.250:1900` SSDP M-SEARCH broadcast gönderir (`ST: urn:lge-com:service:webos-second-screen:1`).
   - TV'nin HTTP XML açıklamasını çeker ve LG cihazlarını listeler.
2. **WebSocket İletişimi (`WebOS.swift`):**
   - Port 3001 üzerinden güvenli WebSocket (`wss://<tv_ip>:3001/`) açar.
   - LG webOS JSON RPC mesajlaşmasını (`com.webos.service.tv.display/setVolume`, `request`, vb.) kullanır.
   - İstemci anahtarını macOS Keychain içinde saklar.
3. **Pointer Kanalı (`PointerChannel.swift`):**
   - WebOS `/getPointerInputSocket` servisinden ikincil WebSocket bağlantısı alarak fare koordinatı (`dx`, `dy`) ve tıklama olaylarını gönderir.

### 2.2 Uyumsuzluklar ve Android TV Farklılıkları
- **Protokol:** LG webOS, port 3001 üzerinde JSON tabanlı WebSocket kullanır. Ancak Vestel 55QG9960 bir **Android TV**'dir. Android TV, Google'ın resmi **Android TV Remote v2** protokolünü (TLS soketleri, port 6466 ve 6467, Protobuf framing) kullanır.
- **Eşleştirme:** LG webOS ekranda bir istemci yetkilendirme popup'ı gösterirken, Android TV v2 ekranda 6 haneli bir PIN gösterir ve istemci sertifikası + sunucu sertifikası + PIN üzerinden SHA-256 tabanlı bir `PairingSecret` üretilmesini şart koşar.
- **İşletim Sistemi:** macOS Cocoa/AppKit yapıları Android'de çalışmaz; Jetpack Compose ve Material 3 ile sıfırdan bir akıllı telefon UX'i inşa edilmiştir.

---

## 3. VESTEL 55QG9960 PROTOKOLÜ TERSİNE MÜHENDİSLİK ANALİZİ

### 3.1 Ağ Portları ve Servis Haritası
| Port | Protokol | Servis | Fonksiyon |
|---|---|---|---|
| **6466** | TLS (TCP) | Android TV Remote Control v2 | Kumanda tuş enjeksiyonu, yönlendirme, keep-alive |
| **6467** | TLS (TCP) | Android TV Remote Pairing | Karşılıklı sertifika doğrulaması ve PIN doğrulama |
| **8008** | HTTP (TCP) | DIAL (Discovery & Launch) | YouTube, Netflix gibi uygulamaları başlatma |
| **8009** | TLS (TCP) | Google Cast (Chromecast) | Medya oynatma ve akış yönetimi |
| **9** | UDP | Wake-on-LAN (WoL) | TV bekleme modundayken uyandırma |
| **1900** | UDP | SSDP / UPnP | Cihaz ve DIAL servis keşfi |
| **5353** | UDP | mDNS / DNS-SD | `_androidtvremote2._tcp` servis ilanı |

### 3.2 Protobuf Wire Format Çözümlemesi (Port 6466 / 6467)
Android TV Remote v2 paketleri şu yapıya sahiptir:
- **Paket Başlığı:** `varint` veya 1-bayt uzunluk öneki (payload length).
- **Protobuf Mesajı:**
  - `field_tag = (field_number << 3) | wire_type`
  - Varint (Wire Type 0) tamsayı alanlar ve Length-delimited (Wire Type 2) string/byte dizileri.
- **Anahtar Enjeksiyonu (`remote_key_inject`, Tag 0x12):**
  - Alan 1: Android `KeyEvent` tamsayı kodu (Örn: `KEYCODE_DPAD_CENTER = 23`).
  - Alan 2: Basım eylemi (1 = SHORT click).

---

## 4. GÜVENLİK VE ETİK SINIRLAR

- **Kriptografik Güvenlik:** İstemci tarafında rastgele üretilen 2048-bit RSA anahtarı ve X.509 sertifikası Android Keystore içerisinde izole edilmiştir.
- **Sertifika Doğrulama:** TV'nin sertifikası ilk eşleştirmede parmak izi (fingerprint) olarak kaydedilir ve sonraki oturumlarda MitM (ortadaki adam) saldırılarına karşı doğrulanır.
- **Yasal ve Etik Sorumluluk:** Cihazın firmware'ine müdahale edilmemiş, root erişimi talep edilmemiş, yalnızca Android TV işletim sisteminin standart yerel uzaktan kumanda protokolü uygulanmıştır.
