# TVX ve RecTV Statik APK Analizi ve Başlatma Stratejisi Raporu

**Tarih:** 17 Ekim 2026  
**Geliştirici:** Yunus İNAN  
**Hedef Donanım:** Samsung Galaxy S24 Ultra ↔ Vestel 55QG9960 Android TV (`GoogleTV7288`)  
**Analiz Araçları:** Android SDK Build-Tools `36.1.0` (`aapt2 dump badging`, `aapt2 dump xmltree`, `aapt2 dump resources`, `aapt2 dump strings`)

---

## 1. Yönetici Özeti

Kullanıcının fiziksel **Vestel 55QG9960 Android TV** üzerinde kurulu olduğunu doğruladığı iki üçüncü parti APK dosyası (`app-release.apk` ve `rectv22.0.2-161mobilbox.apk`) yerel ortamda çalıştırılmadan ve cihaza kurulmadan statik olarak incelenmiştir:

| Özellik | TVX | RecTV |
| :--- | :--- | :--- |
| **APK Dosya Adı** | `app-release.apk` | `rectv22.0.2-161mobilbox.apk` |
| **Dosya Boyutu** | `104,830,430` bayt (~100.0 MB) | `72,219,379` bayt (~68.9 MB) |
| **SHA-256 Özeti** | `1857E1E0F13C02E3B053937CEF2077B8143255DDED91A955FF50AC10EDE09D61` | `00C666D984FE84FC819DF6A0F66C37066F5E233E0FCCD631AAFD58E88FEFC217` |
| **Uygulama Adı (Manifest)** | `TVX` | `REC TV` (Katalog gösterimi: `RecTV`) |
| **Gerçek Paket Adı (`package`)** | `com.tvx.tv` | `com.rectv.shot` |
| **Sürüm Adı (`versionName`)** | `1.0.2` | `22.0.2` |
| **Sürüm Kodu (`versionCode`)** | `5` | `161` |
| **SDK Seviyeleri** | `minSdk=24`, `targetSdk=36`, `compileSdk=36` | `minSdk=24`, `targetSdk=35`, `compileSdk=36` |
| **Uygulama Çerçevesi** | React Native / Expo (`expo-channel-name: tvxbox`) | Flutter (`flutterEmbedding=2`) |
| **Başlatıcı Activity (`LAUNCHER`)** | `com.tvx.tv.MainActivity` (`exported=true`) | `com.rectv.shot.MainActivity` (`exported=true`) |
| **Android TV Leanback Activity** | `com.tvx.tv.MainActivity` (`LEANBACK_LAUNCHER`) | `com.rectv.shot.MainActivity` (`LEANBACK_LAUNCHER`) |
| **Doğrulanmış Deep Link Şeması** | ✅ **`tvx://`** (`ACTION_VIEW` + `DEFAULT` + `BROWSABLE`) | ❌ **Yok** (`MainActivity` üzerinde `VIEW`/`BROWSABLE` şeması tanımlı değil) |
| **DIAL (Port 8008) Desteği** | ❌ Yok | ❌ Yok |
| **Seçilen Başlatma Stratejisi** | **Strateji 1:** Android TV Remote v2 `RemoteAppLinkLaunchRequest` (`tvx://`) | **Doğrudan Başlatma Desteklenmiyor** (Türkçe açıklama + isteğe bağlı tek tuş `HOME` ile TV Ana Ekranına yönlendirme) |
| **Gerçek Donanım Test Durumu** | ✅ **Fiziksel Vestel 55QG9960 (`GoogleTV7288`) TV'de doğrudan açıldığı doğrulandı** | ℹ️ **Katalogda kurulu olarak tanımlı; doğrudan başlatma protokol kısıtı nedeniyle desteklenmiyor (Ana Ekran yardımcısı aktif)** |

---

## 2. TVX (`com.tvx.tv`) Detaylı Statik Analizi

### 2.1 Paket ve Derleme Üst Verileri
- **Paket Adı:** `com.tvx.tv`
- **Uygulama Etiketi:** `TVX` (tüm yerel ayarlarda `TVX`)
- **Sürüm:** `versionName="1.0.2"`, `versionCode="5"`
- **SDK Bilgileri:** `minSdkVersion="24"`, `targetSdkVersion="36"`, `compileSdkVersion="36"`
- **Uygulama Sınıfı:** `com.tvx.tv.MainApplication`

### 2.2 Dışa Aktarılan (Exported) Bileşenler ve Intent Filtreleri
`aapt2 dump xmltree app-release.apk --file AndroidManifest.xml` çıktısına göre `com.tvx.tv.MainActivity` (`line=102`):
- `android:exported="true"`
- `android:launchMode="2"` (`singleTask`)
- `android:banner="@0x7f0f0002"` (`res/Ee.webp`)
- `android:supportsPictureInPicture="true"`

Tanımlı `intent-filter` blokları:
1. **Standart ve Android TV Leanback Başlatıcı (`line=111..115`):**
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.MAIN" />
       <category android:name="android.intent.category.LAUNCHER" />
       <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
   </intent-filter>
   ```
2. **Özel Deep Link Şeması (`line=117..123`):**
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.VIEW" />
       <category android:name="android.intent.category.DEFAULT" />
       <category android:name="android.intent.category.BROWSABLE" />
       <data android:scheme="tvx" />
   </intent-filter>
   ```
   - `host` veya `path` kısıtlaması bulunmadığından `tvx://` URI'si doğrudan `com.tvx.tv.MainActivity` bileşenini tetikler.

### 2.3 Diğer Bileşenler
- `expo.modules.video.FullscreenPlayerActivity` (dışa aktarılmamış)
- `com.jakewharton.processphoenix.ProcessPhoenix` (`android:exported="false"`)
- `expo.modules.clipboard.ClipboardFileProvider` (`authorities="com.tvx.tv.ClipboardFileProvider"`)

### 2.4 İkon ve TV Banner Kaynakları
- `@0x7f0f0000` (`mipmap/ic_launcher`):
  - `mdpi`: `res/d2.webp`
  - `hdpi`: `res/MO.webp`
  - `xhdpi`: `res/qs.webp`
  - `xxhdpi`: `res/Sn.webp`
  - `xxxhdpi`: `res/sK.webp` (`192×192` WebP — Siyah zemin üzerinde sarı/altın `#F5D142` ve beyaz `#FFFFFF` `TVX+` logosu ve TV anteni)
- `@0x7f0f0001` (`mipmap/ic_launcher_foreground`): `res/5c.webp` (`432×432` WebP)
- `@0x7f0f0002` (`mipmap/tvbanner`): `res/Ee.webp` (`320×180` WebP)

---

## 3. RecTV (`com.rectv.shot`) Detaylı Statik Analizi

### 3.1 Paket ve Derleme Üst Verileri
- **Paket Adı:** `com.rectv.shot`
- **Uygulama Etiketi:** Manifest içinde `REC TV` (Uygulama kataloğunda kullanıcı talebine uygun olarak `RecTV`)
- **Sürüm:** `versionName="22.0.2"`, `versionCode="161"`
- **SDK Bilgileri:** `minSdkVersion="24"`, `targetSdkVersion="35"`, `compileSdkVersion="36"`
- **Uygulama Sınıfı:** `com.rectv.shot.RecTvApplication`

### 3.2 Android TV Uyumluluk Üst Verileri
- `<uses-feature android:name="android.software.leanback" android:required="false" />`
- `<uses-feature android:name="android.hardware.type.television" android:required="false" />`
- `<uses-feature android:name="android.hardware.touchscreen" android:required="false" />`
- `<application android:banner="@0x7f08046b" ... />` (`res/gU.png`, `320×180` PNG)

### 3.3 Dışa Aktarılan (Exported) Bileşenler ve Intent Filtreleri
`aapt2 dump xmltree rectv22.0.2-161mobilbox.apk --file AndroidManifest.xml` çıktısına göre `com.rectv.shot.MainActivity` (`line=236`):
- `android:exported="true"`
- `android:launchMode="1"` (`singleTop`)
- `android:supportsPictureInPicture="true"`

Tanımlı `intent-filter` blokları (`line=251..276`):
1. **Standart Başlatıcı:**
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.MAIN" />
       <category android:name="android.intent.category.LAUNCHER" />
   </intent-filter>
   ```
2. **Android TV Leanback Başlatıcı:**
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.MAIN" />
       <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
   </intent-filter>
   ```
3. **Arama Eylemleri (`SEARCH` & `MEDIA_PLAY_FROM_SEARCH`):**
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.SEARCH" />
   </intent-filter>
   <intent-filter>
       <action android:name="android.media.action.MEDIA_PLAY_FROM_SEARCH" />
       <category android:name="android.intent.category.DEFAULT" />
   </intent-filter>
   ```

### 3.4 Deep Link Şeması Analizi
- `com.rectv.shot.MainActivity` üzerinde **`android.intent.action.VIEW` veya `android.intent.category.BROWSABLE` içeren hiçbir `intent-filter` ve hiçbir `<data android:scheme="..." />` tanımı bulunmamaktadır.**
- APK içindeki tek `VIEW` + `BROWSABLE` filtreleri, Firebase Authentication SDK'sına ait iç yardımcı bileşenlerdir:
  - `com.google.firebase.auth.internal.GenericIdpActivity` (`genericidp://firebase.auth/`)
  - `com.google.firebase.auth.internal.RecaptchaActivity` (`recaptcha://firebase.auth/`)
  Bu bileşenler kimlik doğrulama oturumu dışında çağrıldığında uygulamayı başlatmaz.
- Uygulama Google Play Store dışında harici APK (`rectv22.0.2-161mobilbox.apk`) olarak kurulduğundan `market://details?id=com.rectv.shot` bağlantısı da uygulamayı doğrudan açmaz.

### 3.5 İkon ve TV Banner Kaynakları
- `@0x7f0f0000` (`mipmap/ic_launcher`):
  - Adaptive Icon (`res/BW.xml`): Arka plan `@0x7f06008d` (`#FFB71C1C` koyu kırmızı), ön plan `@0x7f0f0001` (`res/as.png`, `432×432` PNG — beyaz el yazısı `R` harfi ve üstünde TV anteni).
  - Yoğunluk bazlı PNG: `res/o-.png` (`xxxhdpi`, `192×192` PNG).
- `@0x7f08046b` (`drawable/tv_banner`): `res/gU.png` (`320×180` PNG).

---

## 4. Uzaktan Başlatma Protokol Değerlendirmesi (Phase C)

### 4.1 Strateji 1 — Android TV Remote v2 `RemoteAppLinkLaunchRequest` (Port 6466, Field 90)
- **Çalışma Prensibi:** Android TV üzerindeki `AtvRemoteService`, Protobuf Field `90` (`RemoteAppLinkLaunchRequest`) mesajındaki `uri` dizgesini alarak `Intent(Intent.ACTION_VIEW, Uri.parse(uri))` oluşturur.
- **TVX (`com.tvx.tv`) İçin Uygunluk:** ✅ **Tam Uyumlu ve Fiziksel TV'de Doğrulandı.** `com.tvx.tv.MainActivity` bileşeni `<data android:scheme="tvx" />` ile `ACTION_VIEW` + `CATEGORY_BROWSABLE` + `CATEGORY_DEFAULT` filtrelerini açıkça kaydettiği için `tvx://` URI'si doğrudan bu aktiviteyi hedefler.
- **RecTV (`com.rectv.shot`) İçin Uygunluk:** ❌ **Desteklenmiyor.** `com.rectv.shot.MainActivity` herhangi bir URI şeması (`<data android:scheme=.../>`) kaydetmemiştir. Android TV Remote v2 protokolünde yalnızca paket adı (`com.rectv.shot`) veya sınıf adı (`com.rectv.shot/.MainActivity`) göndererek `MAIN/LEANBACK_LAUNCHER` aktivitesi başlatan bir mesaj türü mevcut değildir.

### 4.2 Strateji 2 — DIAL REST Sunucusu (Port 8008)
- Ne `com.tvx.tv` ne de `com.rectv.shot` bir DIAL alıcısı kaydetmektedir. Sahte DIAL uygulama adı üretilmemiştir (`dialName = null`).

### 4.3 Strateji 3 — Desteklenen Kumanda Navigasyonu (Güvenli Kullanıcı Akışı)
- `RecTV` için doğrudan protokol başlatması mevcut olmadığından, kullanıcı `RecTV` kartına dokunduğunda:
  1. Sahte başarı mesajı **gösterilmez**.
  2. Neden doğrudan başlatılamadığını açıklayan net bir Türkçe bilgilendirme diyaloğu gösterilir.
  3. Kullanıcının isteğiyle televizyonun **Ana Ekran / Uygulamalar (`KEYCODE_HOME`)** arayüzünü tek dokunuşla açan güvenli yardımcı aksiyon sunulur (kör tuş dizisi kullanılmaz).

### 4.4 Strateji 4 — Gelecek İsteğe Bağlı TV Eşlikçi (Companion) Uygulaması
- Android TV üzerinde `ACTION_VIEW` deep link şeması tanımlamayan üçüncü parti APK'ların (`com.rectv.shot` gibi) telefon üzerinden doğrudan paket adıyla başlatılabilmesi için, televizyon tarafında çalışan küçük bir **Vestel Remote TV Companion** alıcı uygulamasının kendi özel deep link şemasını (`vestelremote://launch?pkg=com.rectv.shot`) dinleyerek TV üzerinde `PackageManager.getLaunchIntentForPackage(...)` veya `getLeanbackLaunchIntentForPackage(...)` çağrısı yapması mümkündür. Bu aşamada ek bir TV uygulaması zorunlu tutulmamıştır.

---

## 5. Gerçek Donanım Doğrulama Sonuçları (Samsung Galaxy S24 Ultra & Vestel 55QG9960)
- **TVX (`com.tvx.tv`):** Samsung Galaxy S24 Ultra üzerinde çalışan Vestel Remote uygulamasından **TVX** kartına dokunulduğunda, Port `6466` üzerinden gönderilen `RemoteAppLinkLaunchRequest` (`tvx://`) komutu **Vestel 55QG9960 (`GoogleTV7288`)** televizyon tarafından başarıyla işlenmiş ve **TVX uygulaması televizyon ekranında doğrudan açılmıştır**.
- **RecTV (`com.rectv.shot`):** Uygulama kataloğunda özel Compose ikonu ve kullanıcı onaylı kurulu durumuyla yer almaktadır. Doğrudan başlatma protokol/manifest sınırı nedeniyle desteklenmemekte olup açıklayıcı diyalog ve tek dokunuşla TV Ana Ekranı (`HOME`) geçişi doğrulanmıştır.

---

## 6. İkon Dağıtım, Güvenlik ve Telif Politikası
- **Üçüncü Taraf APK ve İkili (Binary) Gizliliği:** İncelenen üçüncü taraf APK dosyaları (`app-release.apk` ve `rectv22.0.2-161mobilbox.apk`) veya içlerinden çıkarılan ham kaynaklar kesinlikle Git deposuna dahil edilmemiştir.
- **Vektör/Tuval İkon Tasarımı:** Uygulama içindeki 3 sütunlu uygulama ızgarasında (`BrandedAppIcon`), diğer katalog uygulamalarıyla (Netflix, YouTube, Disney+, Prime Video, Exxen, BluTV) aynı mimaride, TVX (`#08090C` siyah zemin, `#F5D142` altın sarısı ve beyaz `TVX+` + anten motifi) ve RecTV (`#B71C1C` koyu kırmızı dairesel rozet, beyaz `R` + anten motifi) için yüksek keskinlikte yerel Jetpack Compose vektör/tuval bileşenleri oluşturulmuştur.
- **URI Doğrulama ve Teşhis Maskeleme:** `AppLauncherEngine` yalnızca güvenli URI şemalarına (`https`, `http`, `vnd.youtube`, `netflix`, `tvx` vb.) izin verir; kontrol karakterleri, boşluklar veya kabuk/komut enjeksiyonu kalıpları reddedilir. `AppLaunchDiagnostic` kayıtlarında yerel IP adresleri `<tv-host>` olarak maskelenir ve hiçbir kimlik doğrulama sırrı kaydedilmez.

