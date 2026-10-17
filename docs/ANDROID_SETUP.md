# VESTEL REMOTE — ANDROID KURULUM VE KULLANIM KILAVUZU

**Uygulama:** Vestel Remote  
**Geliştirici:** Yunus İNAN  
**Hedef Cihaz:** Samsung Galaxy S24 Ultra (ve Android 8.0+ destekleyen tüm cihazlar)  
**Hedef Televizyon:** Vestel 55QG9960 4K QLED Android TV  

---

## 1. GEREKSİNİMLER

### 1.1 Geliştirme ve Derleme Ortamı
- **İşletim Sistemi:** Windows 10/11, macOS veya Linux
- **Java:** OpenJDK 17 veya OpenJDK 21 (Android Studio JBR önerilir)
- **Android SDK:** API Level 35 (Android 15), Minimum SDK: API Level 26 (Android 8.0 Oreo)
- **Gradle:** Gradle 8.14 (Gradle Wrapper dahil)
- **Android Gradle Plugin:** AGP 8.8.0

### 1.2 Hedef Akıllı Telefon
- **Cihaz:** Samsung Galaxy S24 Ultra (veya uyumlu Android telefon)
- **Android Sürümü:** Android 14 / One UI 6.1 veya üstü
- **Ağ:** Televizyon ile aynı yerel Wi-Fi ağı (2.4 GHz veya 5 GHz)

---

## 2. DERLEME VE APK ÜRETİMİ

Proje kök dizininde yer alan Gradle Wrapper ile uygulamanın debug APK'sını oluşturmak için:

```powershell
# Windows PowerShell
.\gradlew.bat assembleDebug
```

Derleme tamamlandığında üretilen kurulabilir APK dosyası şu dizinde yer alır:
```
app/build/outputs/apk/debug/app-debug.apk
```

Birim testlerini çalıştırmak için:
```powershell
.\gradlew.bat testDebugUnitTest
```

---

## 3. SAMSUNG GALAXY S24 ULTRA'YA KURULUM

Üretilen `app-debug.apk` dosyasını telefonunuza yüklemek için üç farklı yöntem kullanabilirsiniz:

### Yöntem A: ADB (Android Debug Bridge) ile Kurulum (Önerilen)
1. Telefonunuzda **Ayarlar > Telefon Hakkında > Yazılım Bilgileri** bölümüne gidin.
2. **Yapım Numarası** (Build Number) seçeneğine 7 kez art arda dokunarak **Geliştirici Seçenekleri**'ni aktif edin.
3. **Ayarlar > Geliştirici Seçenekleri** menüsünden **USB Hata Ayıklama** (USB Debugging) veya **Kablosuz Hata Ayıklama** seçeneğini açın.
4. Telefonunuzu USB kablosuyla bilgisayara bağlayın ve şu komutu çalıştırın:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Yöntem B: Quick Share / Dosya Aktarımı ile Kurulum
1. `app-debug.apk` dosyasını Samsung Quick Share, Bluetooth veya USB kablosu ile telefonunuzun **İndirilenler (Download)** klasörüne aktarın.
2. Telefonunuzda **Dosyalarım** uygulamasını açın ve `app-debug.apk` dosyasına dokunun.
3. *"Bilinmeyen kaynaklardan yükleme"* izni istendiğinde onay vererek yüklemeyi tamamlayın.

---

## 4. VESTEL 55QG9960 İLE İLK EŞLEŞTİRME VE BAĞLANTI

1. **Ağ Kontrolü:**
   - Samsung Galaxy S24 Ultra telefonunuzun ve Vestel 55QG9960 televizyonunuzun **aynı Wi-Fi ağına** bağlı olduğundan emin olun.
   - Modeminizin ayarlarında "AP İzolasyonu" (Client Isolation) özelliğinin kapalı olduğunu doğrulayın.

2. **Televizyonu Açın:**
   - Televizyonu fiziksel kumandasıyla açın ve ana ekranda bekleyin.

3. **Cihazı Bulma:**
   - Telefonda **Vestel Remote** uygulamasını açın.
   - Alt menüden **Cihazlar** sekmesine geçin.
   - **"Ağda Ara"** butonuna basarak televizyonun mDNS / SSDP üzerinden otomatik bulunmasını sağlayın.
   - Otomatik keşif ağınızda engelleniyorsa, televizyonun IP adresini (**Ayarlar > Cihaz Tercihleri > Hakkında > Durum > IP adresi**) manuel giriş alanına yazarak **"Kaydet ve Bağlan"** butonuna dokunun.

4. **PIN ile Güvenli Eşleştirme:**
   - Cihaz kartındaki **"Eşleştir"** butonuna dokunun.
   - Vestel 55QG9960 ekranında anında **6 haneli bir eşleştirme kodu (PIN)** görüntülenecektir (örn: `559960`).
   - Telefonunuzda açılan modal pencereye bu kodu girin ve **"Onayla"** butonuna basın.
   - TV sertifikası otomatik olarak doğrulanacak, sertifika parmak izi Android Keystore destekli yerel veritabanına kaydedilecek ve bağlantı kurulacaktır!

5. **Kullanım:**
   - **Kumanda** sekmesine geçerek D-Pad veya Dokunmatik yüzey ile televizyonunuzu yönetin.
   - **Uygulamalar** sekmesinden YouTube veya Netflix'i doğrudan başlatın.
   - **Girişler** sekmesinden HDMI 1, HDMI 2 (eARC) veya Canlı TV yayınına geçiş yapın.

---

## 5. SORUN GİDERME VE TEŞHİS

- **TV Bulunamıyor:**
  - Modeminizde 2.4 GHz ve 5 GHz bantları arasında izolasyon olabilir. Telefonu ve TV'yi aynı frekans bandına bağlayın.
- **Eşleştirme Kodu Ekranında Kalıyor:**
  - Kodun büyük/küçük harf veya rakamlarını doğru girdiğinizden emin olun.
  - Cihazlar sekmesindeki **Ağ Teşhisi** ikonuna dokunarak Port 6466 ve 6467'nin açık olup olmadığını kontrol edin.
- **Televizyon Kapalıyken Açılmıyor:**
  - Televizyonunuzun bekleme modunda ağ kartını uyanık tutması için TV ayarlarından **"Hızlı Başlatma" (Fast Boot / Wake on LAN)** seçeneğini aktif edin ve cihaz kartına TV'nin MAC adresini tanımlayın.
