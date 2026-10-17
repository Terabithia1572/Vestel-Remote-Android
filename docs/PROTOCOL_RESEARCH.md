# VESTEL 55QG9960 ANDROID TV PROTOKOL VE BAĞLANTI ARAŞTIRMASI

**Hazırlayan:** Yunus İNAN  
**Tarih:** 17 Ekim 2026  
**Hedef Cihaz:** Vestel 55QG9960 4K QLED Android TV  
**Uygulama:** Vestel Remote (Android)  

---

## 1. GİRİŞ VE TELEVİZYON MİMARİSİ

Vestel 55QG9960, Vestel'in amiral gemisi QLED Android TV serisine ait, Android TV 11 tabanlı akıllı televizyondur. Orijinal projedeki (LGwebOS-menubar) LG webOS WebSocket (port 3001) protokolü Android TV platformuyla doğrudan uyumlu değildir. Bu nedenle Android TV ekosisteminin resmi ve doğrulanabilir iletişim protokolleri araştırılmış ve uygulamaya entegre edilmiştir.

Vestel Android TV'lerde yer alan ağ iletişim katmanları şunlardır:
1. **Android TV Remote Service (v2 Protokolü)**: Port 6466 (TLS Komut) ve Port 6467 (TLS Eşleştirme)
2. **DIAL (Discovery and Launch) Servisi**: Port 8008 / 8009 (HTTP/UPnP)
3. **Google Cast (Chromecast Built-in)**: Port 8009 (TLS / Protobuf)
4. **mDNS / DNS-SD ve SSDP Keşfi**: `_androidtvremote2._tcp` ve UPnP M-SEARCH
5. **Wake-on-LAN (WoL)**: UDP Port 9 Sihirli Paket (Magic Packet)
6. **İsteğe Bağlı ADB Hata Ayıklama**: Port 5555 (Geliştirici Teşhisi)

---

## 2. ANDROID TV REMOTE SERVICE (v2) PROTOKOLÜ

Android TV Remote Service v2, Google'ın modern Android TV ve Google TV cihazlarında kullandığı güvenli, çift yönlü TLS protokolüdür.

### 2.1 Eşleştirme (Pairing) Prosedürü (Port 6467)
1. **Sertifika Değişimi (Mutual TLS):**
   - İstemci (Vestel Remote), 2048-bit RSA anahtarı ve `CN=VestelRemote` öz-imzalı X.509 istemci sertifikası üretir. Sertifika cihazda Android Keystore ile korunur.
   - İstemci `tv_ip:6467` adresine TLS soketi açar ve sunucunun sertifikasını alır.
2. **PairingRequest (İstemci -> TV):**
   - Protokol sürümü: 2, Durum: 200, Servis adı: `com.google.android.tv.remote`, İstemci adı: `Vestel Remote (S24 Ultra)`.
3. **PairingOption & PairingConfiguration:**
   - İstemci rolünü (INPUT) ve kodlama türünü (HEX/Alfanümerik 6 karakter) belirtir.
   - TV, `PairingConfigurationAck` (200 OK) döndüğü anda televizyon ekranında büyük puntolarla 6 haneli eşleştirme kodu (PIN) görüntülenir.
4. **Gizli Anahtar (PairingSecret) Doğrulaması:**
   - Kullanıcı TV ekranındaki kodu akıllı telefonuna girer.
   - İstemci şu hash işlemini uygular:
     $$\text{Secret} = \text{SHA-256}(\text{SHA-256}(\text{ClientCert}) + \text{SHA-256}(\text{ServerCert}) + \text{PIN\_Bytes})$$
   - İstemci `PairingSecret` mesajını gönderir. TV şifreyi doğrularsa `PairingSecretAck` (200 OK) döner ve eşleştirme kalıcı olarak kaydedilir.

### 2.2 Uzaktan Kumanda ve Durum Oturumu (Port 6466)
Eşleştirilmiş istemci sertifikası ile `tv_ip:6466` portuna TLS bağlantısı kurulur.
- **Tuş Gönderme (RemoteKeyInject):**
  - Android KeyEvent kodları ve basım yönü (1 = SHORT press, 2 = START_LONG) paketlenir.
- **Canlı Kalma (Keep-Alive):**
  - Her 8 saniyede bir `RemotePingRequest` gönderilerek oturumun canlı kalması sağlanır.
- **Doğrudan Uygulama Başlatma (RemoteAppLink):**
  - Uygulama URI intent şeması (örn: `https://www.netflix.com/title/`) gönderilerek ilgili içerik veya uygulama TV'de açılır.

---

## 3. ANDROID TV RESMİ KEYCODE TABLOSU

| Tuş Fonksiyonu | Android KeyEvent İsmi | KeyCode Değeri | Açıklama |
|---|---|---|---|
| Yukarı | `KEYCODE_DPAD_UP` | 19 | Yön navigasyonu |
| Aşağı | `KEYCODE_DPAD_DOWN` | 20 | Yön navigasyonu |
| Sol | `KEYCODE_DPAD_LEFT` | 21 | Yön navigasyonu |
| Sağ | `KEYCODE_DPAD_RIGHT` | 22 | Yön navigasyonu |
| Tamam / Seç | `KEYCODE_DPAD_CENTER` | 23 | Seçim ve onay |
| Geri | `KEYCODE_BACK` | 4 | Önceki ekrana dönüş |
| Ana Sayfa | `KEYCODE_HOME` | 3 | Android TV ana menüsü |
| Menü | `KEYCODE_MENU` | 82 | Ayarlar ve bağlam menüsü |
| Asistan / Sesli Arama | `KEYCODE_ASSIST` | 219 | Google Asistan |
| Ses Artır | `KEYCODE_VOLUME_UP` | 24 | Donanımsal ses artırma |
| Ses Azalt | `KEYCODE_VOLUME_DOWN` | 25 | Donanımsal ses azaltma |
| Sessiz | `KEYCODE_VOLUME_MUTE` | 164 | Sesi tamamen kapatma |
| Güç | `KEYCODE_POWER` | 26 | Açma / Bekleme (Standby) |
| Oynat / Duraklat | `KEYCODE_MEDIA_PLAY_PAUSE` | 85 | Medya kontrolü |
| Geri Sar | `KEYCODE_MEDIA_REWIND` | 89 | Medya geri sarma |
| İleri Sar | `KEYCODE_MEDIA_FAST_FORWARD` | 90 | Medya ileri sarma |
| Durdur | `KEYCODE_MEDIA_STOP` | 86 | Medyayı durdurma |
| Kanal Artır | `KEYCODE_CHANNEL_UP` | 166 | Canlı TV kanal artırma |
| Kanal Azalt | `KEYCODE_CHANNEL_DOWN` | 167 | Canlı TV kanal azaltma |
| Giriş Kaynağı | `KEYCODE_TV_INPUT` | 178 | Giriş kaynakları menüsü |
| Kırmızı Tuş | `KEYCODE_PROG_RED` | 183 | HbbTV ve yayın etkileşimi |
| Yeşil Tuş | `KEYCODE_PROG_GREEN` | 184 | HbbTV ve yayın etkileşimi |
| Sarı Tuş | `KEYCODE_PROG_YELLOW` | 185 | HbbTV ve yayın etkileşimi |
| Mavi Tuş | `KEYCODE_PROG_BLUE` | 186 | HbbTV ve yayın etkileşimi |
| Numaralar (0-9) | `KEYCODE_0` .. `KEYCODE_9` | 7 .. 16 | Kanal numarası girişi |

---

## 4. DIAL (DISCOVERY AND LAUNCH) UYGULAMA SERVİSİ

DIAL protokolü, akıllı telefonların yerel ağdaki televizyonlarda uygulamaları keşfetmesini ve başlatmasını sağlayan açık bir protokoldür. Vestel Android TV'deki Chromecast/Google Cast bileşeni varsayılan olarak DIAL servisini (port 8008) çalıştırır.

- **Uygulama Başlatma:** `POST http://<tv_ip>:8008/apps/<AppName>`
- **Uygulama Durumunu Sorgulama:** `GET http://<tv_ip>:8008/apps/<AppName>`
- **Desteklenen Başlıca DIAL Uygulamaları:**
  - YouTube: `YouTube`
  - Netflix: `Netflix`
  - Prime Video: `AmazonInstantVideo`
  - Spotify: `Spotify`

---

## 5. WAKE-ON-LAN (WOL) VE BEKLEME MODU

Vestel 55QG9960 bekleme modundayken (standby), Wi-Fi veya Ethernet çipi düşük güç durumunda dinleme yapmaya devam eder.
- **Sihirli Paket Formatı:** 6 adet `0xFF` baytı ve ardından TV'nin 6 baytlık MAC adresinin 16 kez ardışık tekrarı (toplam 102 bayt).
- **İletim:** UDP soketi üzerinden `255.255.255.255:9` genel yayın adresine ve TV'nin bilinen son yerel IP adresine gönderilir.

---

## 6. GÜVENLİK VE GİZLİLİK İLKELERİ

1. **Sertifika Sabitleme (Certificate Pinning):** İlk eşleştirme sırasında TV'nin sunduğu sertifikanın SHA-256 parmak izi kaydedilir. Sonraki bağlantılarda Man-in-the-Middle (MitM) saldırılarını engellemek için bu parmak izi doğrulanır.
2. **Yerel Ağ İzolasyonu:** Televizyon kumanda ve eşleştirme verileri yalnızca yerel ağ (LAN) üzerinden iletilir; hiçbir üçüncü taraf sunucuya veya buluta telemetri gönderilmez.
3. **Android Keystore Koruması:** İstemci özel anahtarı güvenli donanım alanında oluşturulur ve saklanır.
