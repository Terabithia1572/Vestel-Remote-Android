# Vestel Remote — Eşleştirme Regresyonu Denetimi, Onarımı ve Donanım Doğrulaması

**Tarih:** 17 Ekim 2026  
**Geliştirici:** Yunus İNAN (`https://github.com/Terabithia1572`)  
**Durum:** Kod, protokol, yerel mTLS birim testleri (33/33) ve **Samsung Galaxy S24 Ultra ↔ Vestel 55QG9960 (`GoogleTV7288`) fiziksel donanım doğrulaması başarıyla tamamlandı.**

## Doğrulanan Donanım Başarısı

Bu onarım sonrasında uygulama fiziksel **Samsung Galaxy S24 Ultra** cihazına kurulmuş ve gerçek **Vestel 55QG9960 Android TV (`GoogleTV7288`)** ile test edilmiştir:
- **TV Keşfi:** Başarılı (`GoogleTV7288`)
- **PIN Eşleştirme (Port 6467):** Başarılı
- **Kimlik Doğrulamalı Kumanda Oturumu (Port 6466):** Başarılı (`RemoteStart` alındı)
- **Ana Kumanda İşlevleri ve Ekranlar:** Başarılı

## Doğrulanan Kusurlar ve Kanıt

Aşağıdaki “önce” satırları onarım öncesi kaynak yedeğindeki dosyalara aittir; bağlantılar onarılan güncel dosyaları açar.

1. **Android Keystore anahtar izinleri TLS'nin ham RSA imza işlemini karşılamıyordu.** Önce `CertificateHelper.kt:53–54` yalnızca SHA-256 ve PKCS#1 imzasını yetkilendiriyordu. Android belgeleri TLS için `DIGEST_NONE` gereksinimini açıklar. Conscrypt RSA imzasını `Cipher` üzerinden devreder; AOSP Keystore bu yolu SIGN amacı, NONE özeti ve ilgili padding ile çalıştırır. Yeni kimlik NONE/SHA-256/384/512, PKCS#1/PSS ve ham RSA NoPadding izinleriyle üretilir. Eski kısıtlı kimlik yalnızca yeniden eşleştirme sırasında ayrı `VestelRemoteClientKeyTlsV2` alias'ına geçirilir; eski alias silinmez. Keystore hatasında sessizce yeni yazılım anahtarı üretme kaldırıldı. [Güncel kod](../app/src/main/java/com/yunusinan/vestelremote/data/crypto/CertificateHelper.kt), [Android belgesi](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.Builder#setDigests(java.lang.String...)), [Conscrypt kaynağı](https://github.com/google/conscrypt/blob/master/common/src/main/java/org/conscrypt/CryptoUpcalls.java), [AOSP Keystore kaynağı](https://github.com/aosp-mirror/platform_frameworks_base/blob/main/keystore/java/android/security/keystore2/AndroidKeyStoreRSACipherSpi.java).

2. **Beklenen eşleştirme ACK türü doğrulanmıyordu.** Önce `AndroidTvRemoteV2Client.kt:164` yalnızca `status=200` ile PIN aşamasına, `:204` yalnızca durum koduyla eşleşmiş durumuna geçiyordu. `ConfigurationAck` veya `SecretAck` bulunması şart değildi. Artık sürüm, durum, tek beklenen yük alanı, wire type ve iç mesaj doğrulanır. TV'nin HEX/6 çıktı kodlaması seçeneklerinde bulunmalıdır. Yerel mTLS regresyon testi, yalnızca status içeren yapılandırma yanıtında PIN callback'inin hiç çağrılmadığını doğrular. [Doğrulama](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/RemoteProtocol.kt).

3. **Kumanda protokolü TV mesajlarını hiç okumuyordu; bağlantı erken başarılı gösteriliyordu.** Önce `AndroidTvRemoteV2Client.kt:259` TLS sonrası `isConnected=true` yapıyordu. `:285` TV'yi beklemeden `RemoteSetActive(active=1)` gönderiyor; `:386` TV ping'lerine cevap vermek yerine kendi ping'ini gönderiyordu. Artık TV'nin Configure mesajına ortak yetenekler ile cevap verilir, SetActive için yetenek maskesi kullanılır, RemoteStart beklenir ve okuyucu oturum boyunca açık kalır. Ping isteği `val1` değeriyle yanıtlanır; EOF, sunucu hatası ve zaman aşımı bağlantı durumuna yansır. [Oturum kodu](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/AndroidTvRemoteV2Client.kt), [protokol işleyicisi](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/RemoteProtocol.kt).

4. **TV sertifikası sabitlenmiyordu.** Önce `TvRepositoryImpl.kt:147` her kumanda bağlantısına `pinnedServerFingerprint=null` veriyor, `:201` eşleştirme sonucundaki parmak izini kaydetmiyordu. Parmak izi artık cihaz kaydında saklanıp yeniden bağlantıda kullanılır. Eski, yalnızca `isPaired=true` içeren kayıtlar yeniden eşleştirme gerektirir. Sertifika uyuşmazlığı sessizce kabul edilmez. Keşfedilen portlar da istemciye aktarılır ve kaydedilir. [Kayıt](../app/src/main/java/com/yunusinan/vestelremote/data/datastore/PreferencesManager.kt), [bağlantı](../app/src/main/java/com/yunusinan/vestelremote/data/repository/TvRepositoryImpl.kt).

5. **Eksik protobuf verisi kabul edilebiliyordu.** Önce `ProtoUtils.kt:134–139` kontrolsüz uzunluk ayırıp kısa mesajı sıfırlarla tamamlanmış veri gibi döndürebiliyordu; bazı parse hataları yutuluyordu. Artık varint taşması, geçersiz alan/wire type, eksik sabit alanlar ve yarım yük reddedilir. Çerçeve üst sınırı 1 MiB'dir. Parçalı ve tek okumada birleşmiş mesajlar test edilir. Yazımlar çerçeve bütünlüğünü koruyacak biçimde kilitlenir. [Çerçeve ve parser](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/ProtoUtils.kt).

6. **PIN değeri hata/olay metnine sızabiliyordu.** Önce `PairingCrypto.kt:31,34` girilen PIN'i exception metnine ekliyordu; sanal istemci de girilen kodu günlüğe yazıyordu. Bu içerikler kaldırıldı. Kopyalanan teşhis yalnızca metadata içerir; bilinmeyen ham exception metni dışarı çıkarılmaz. [PIN doğrulaması](../app/src/main/java/com/yunusinan/vestelremote/data/crypto/PairingCrypto.kt), [hata temizleme](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/AttemptDiagnostics.kt).

7. **İptal ve eşzamanlı girişler oturumları karıştırabiliyordu.** Önce `MainViewModel.kt:195` her tıklamada ayrı eşleştirme başlatıp tek `currentPinDeferred` değişkenini değiştiriyordu. İptal normal hata olarak yakalanıyordu; bloklayan ağ okuması hemen kapanmıyordu. Şimdi bir bağlantı işi tutulur, tekrar tıklamalar engellenir, PIN callback'i Main dispatcher'da çalışır ve iptal soketi kapatır. Eski okuyucu kapanışının yeni bağlantıyı yönetmesine izin verilmez. [ViewModel](../app/src/main/java/com/yunusinan/vestelremote/ui/viewmodel/MainViewModel.kt).

8. **Teşhis yanlış başarı bildiriyordu.** Önce `TvRepositoryImpl.kt:366`, TCP portu açıksa TLS başarılı diyordu. Port taramasının TLS yapmadığı açıkça gösterilir. Kumanda ses durumu yerel tahminle artırılmak yerine TV'nin volume mesajından güncellenir. [Teşhis sonucu](../app/src/main/java/com/yunusinan/vestelremote/data/repository/TvRepositoryImpl.kt).

9. **Keşif hata ve kaynak yönetimi eksikti.** Aynı anda birden çok NSD çözümleme başlatılıyor, hata kodları yutuluyor, iptal sırasında multicast kilidi/listener temizliği garanti edilmiyordu. Çözümlemeler kuyruğa alındı; eski callback'ler yeni taramayı değiştiremez; kaynaklar finally içinde bırakılır. SSDP alıcı paketinin uzunluğu her okumada sıfırlanır. Sadece DIAL ile bulunan cihaza doğrulanmış Android Remote servisi etiketi verilmez. [Keşif yönetimi](../app/src/main/java/com/yunusinan/vestelremote/data/protocol/TvDiscoveryManager.kt).

## Tam akış ve protokol karşılaştırması

`DevicesScreen → MainViewModel → TvRepositoryImpl → AndroidTvRemoteV2Client → TlsSocketFactoryHelper → SSLSocket → TV` incelendi. UI, durum modeli, DataStore, sertifika üretimi, protobuf, mDNS/SSDP, PIN ve uzaktan tuş gönderme yolları birlikte değerlendirildi.

- Eşleştirme portu 6467; kumanda portu varsayılan 6466. mDNS'nin ilan ettiği kumanda portu artık kullanılabilir.
- Eşleştirme zarfı: sürüm alanı 1 = 2, durum alanı 2 = 200. Servis `atvremote` korunur.
- PairingRequest 10 → PairingRequestAck 11; Options 20; Configuration 30 → ConfigurationAck 31; Secret 40 → SecretAck 41.
- Options input encoding alanı 1; TV output encoding alanı 2; encoding type 3 (HEX), symbol length 6, client role INPUT=1.
- PIN özeti: SHA-256(client RSA modulus || client exponent || server modulus || server exponent || PIN'in son iki baytı). İşaretsiz büyük tamsayı baytları kullanılır; ilk PIN baytı özetin ilk baytıyla karşılaştırılır.
- Varint uzunluk öneki korunur; tek bayt uzunluk varsayımı yapılmaz.
- RemoteConfigure 1; RemoteSetActive 2; RemotePingRequest 8 → RemotePingResponse 9; RemoteStart 40; volume 50. Yetenek maskesi `611 & serverMask` (PING, KEY, POWER, VOLUME, APP_LINK).
- KeyInject alanı 10, key_code alanı 1, direction alanı 2 = SHORT(3). D-pad 19–23, Home 3, Back 4, Volume 24/25, Mute 164 korunur. Deep link alanı 90 korunur.

Referanslar: [tronikos eşleştirme](https://github.com/tronikos/androidtvremote2/blob/e664a8e1f14ee458b14c646bfb3ea46f10367051/src/androidtvremote2/pairing.py), [Polo şeması](https://github.com/tronikos/androidtvremote2/blob/e664a8e1f14ee458b14c646bfb3ea46f10367051/src/androidtvremote2/polo.proto), [kumanda protokolü](https://github.com/tronikos/androidtvremote2/blob/e664a8e1f14ee458b14c646bfb3ea46f10367051/src/androidtvremote2/remote.py), [louis49 PairingManager](https://github.com/louis49/androidtv-remote/blob/0e82d72ec067b6452e0e2266720ba5e023821bf6/src/pairing/PairingManager.js), [louis49 RemoteManager](https://github.com/louis49/androidtv-remote/blob/0e82d72ec067b6452e0e2266720ba5e023821bf6/src/remote/RemoteManager.js). TLS 1.3 RSA-PSS gereksinimi: [RFC 8446 §4.4.3](https://www.rfc-editor.org/rfc/rfc8446.html#section-4.4.3).

## Teşhis kullanımı

Cihazlar ve Ayarlar ekranlarında **Teşhisi Kopyala (Copy Diagnostics)** bulunur. Uygulama oturumu içindeki son 20 gerçek bağlantı denemesi ayrı kimliklerle korunur. Eşleştirme ve ardından açılan kumanda bağlantısı ayrı kayıtlardır; ping trafiği bu denemelerin listesini doldurup eski hatayı silmez.

Her kayıtta hedef IP/port, servis, zaman, TCP sonucu, TLS sonucu/sürümü/şifre takımı, istemci kimliğinin depolama türü, aşama, durum, son giden/gelen mesaj türü, sunucu durum kodu, timeout aşaması, exception sınıfı ve temizlenmiş mesaj vardır. En fazla 32 aşama geçişi saklanır. PIN, private key, sertifika içeriği, pairing secret veya ham protokol paketi kaydedilmez.

## Test ve derleme sonucu

- `.\gradlew.bat testDebugUnitTest`: **başarılı — 33 test, 0 başarısız, 0 atlanan**.
- `.\gradlew.bat assembleDebug`: **başarılı**.

Test dağılımı: `MockTvClientTest` (4), `PairingCryptoTest` (6), `PairingStateMachineTest` (3), `ProtoUtilsTest` (4), `ReferenceProtocolTest` (8), `RemoteSessionRegressionTest` (6), `TvCommandEncodingTest` (2).

## Sonraki Geliştirme Fazına Devredilen Bilinen Sorunlar

1. **Panel (Dashboard) Ses Kaydırıcısı Kararsızlığı:** Kullanıcı kaydırıcıyı bıraktıktan sonra ses komutları gönderilmeye devam edebilmektedir.
2. **Kurulu Uygulamaların Keşfi:** Uygulamalar ekranında televizyonda fiilen kurulu olan uygulamaların dinamik olarak listelenmesi sonraki fazda ele alınacaktır.
