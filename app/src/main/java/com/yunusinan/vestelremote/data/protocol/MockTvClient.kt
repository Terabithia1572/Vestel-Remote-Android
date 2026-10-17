package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.domain.model.TvApp
import com.yunusinan.vestelremote.domain.model.TvCommand
import com.yunusinan.vestelremote.domain.model.TvDevice
import com.yunusinan.vestelremote.domain.model.TvInput
import kotlinx.coroutines.delay

class MockTvClient(
    val mockDevice: TvDevice = TvDevice(
        id = "mock-vestel-55qg9960",
        name = "Vestel 55QG9960 (Sanal Test TV)",
        model = "55QG9960",
        ipAddress = "192.168.1.150",
        macAddress = "D8:0D:17:A1:B2:C3",
        isPaired = false,
        isOnline = true
    ),
    private val onEventLog: ((title: String, detail: String) -> Unit)? = null
) {

    var isConnected: Boolean = false
        private set
    var isPowerOn: Boolean = true
        private set
    var volumeLevel: Int = 18
        private set
    var isMuted: Boolean = false
        private set
    var currentApp: String = "Ana Menü"
        private set
    var currentInput: String = "HDMI 1"
        private set

    val generatedPairingPin: String = "559960"

    suspend fun pair(pinProvider: suspend () -> String): Result<String> {
        onEventLog?.invoke("Mock Pairing Started", "Sanal TV eşleştirme portu 6467 simüle ediliyor")
        delay(400)
        onEventLog?.invoke("Mock TV Display", "Sanal kod girişi bekleniyor")

        val enteredPin = pinProvider()
        delay(300)

        return if (enteredPin.trim() == generatedPairingPin || enteredPin.trim() == "123456") {
            onEventLog?.invoke("Mock Pairing OK", "Eşleştirme onaylandı. Sertifika parmak izi kaydedildi.")
            Result.success("mock-sha256-fingerprint-vestel-55qg9960")
        } else {
            onEventLog?.invoke("Mock Pairing Failed", "Girilen kod geçersiz.")
            Result.failure(IllegalArgumentException("TV eşleştirme kodu hatalı."))
        }
    }

    suspend fun connect(): Result<Unit> {
        delay(300)
        isConnected = true
        onEventLog?.invoke("Mock Remote Connected", "Port 6466 simülasyon oturumu aktif")
        return Result.success(Unit)
    }

    suspend fun sendCommand(command: TvCommand): Result<Unit> {
        if (!isConnected) return Result.failure(IllegalStateException("Televizyon bağlı değil"))
        delay(40)

        when (command) {
            is TvCommand.VolumeUp -> {
                if (volumeLevel < 100) volumeLevel++
                isMuted = false
                onEventLog?.invoke("Mock Ses", "Ses artırıldı: $volumeLevel")
            }
            is TvCommand.VolumeDown -> {
                if (volumeLevel > 0) volumeLevel--
                onEventLog?.invoke("Mock Ses", "Ses azaltıldı: $volumeLevel")
            }
            is TvCommand.Mute -> {
                isMuted = !isMuted
                onEventLog?.invoke("Mock Ses", if (isMuted) "Sessize alındı" else "Ses açıldı")
            }
            is TvCommand.Power -> {
                isPowerOn = !isPowerOn
                onEventLog?.invoke("Mock Güç", if (isPowerOn) "TV Açıldı" else "TV Bekleme Modunda")
            }
            is TvCommand.Home -> {
                currentApp = "Ana Menü"
                onEventLog?.invoke("Mock Navigasyon", "Ana Ekrana dönüldü")
            }
            else -> {
                onEventLog?.invoke("Mock Komut", "${command.name} (keyCode: ${command.androidKeyCode})")
            }
        }
        return Result.success(Unit)
    }

    suspend fun launchApp(app: TvApp): Result<Boolean> {
        delay(350)
        currentApp = app.name
        onEventLog?.invoke("Mock Uygulama", "${app.name} başlatıldı (DIAL / DeepLink)")
        return Result.success(true)
    }

    suspend fun switchInput(input: TvInput): Result<Boolean> {
        delay(300)
        currentInput = input.name
        currentApp = input.name
        onEventLog?.invoke("Mock Giriş", "${input.name} kaynağına geçildi")
        return Result.success(true)
    }

    fun disconnect() {
        isConnected = false
        onEventLog?.invoke("Mock Bağlantı Kesildi", "Oturum sonlandırıldı")
    }
}

