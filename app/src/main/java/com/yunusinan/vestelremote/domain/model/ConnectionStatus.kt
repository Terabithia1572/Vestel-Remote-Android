package com.yunusinan.vestelremote.domain.model

sealed class ConnectionStatus {
    // 1. Disconnected
    object Disconnected : ConnectionStatus()

    // Scanning for devices on the local network
    object Scanning : ConnectionStatus()

    // Generic connecting state
    data class Connecting(val ip: String) : ConnectionStatus()

    // 2. Connecting to Pairing Service (TCP port 6467)
    data class ConnectingToPairingService(val ip: String) : ConnectionStatus()

    // 3. Mutual TLS Established
    data class TlsEstablished(val ip: String) : ConnectionStatus()

    // 4. Protocol Handshake (PairingRequest, Options, Configuration)
    data class PairingNegotiation(val ip: String, val stage: String) : ConnectionStatus()

    // 5. Awaiting PIN code displayed on TV screen
    data class AwaitingTvPin(val device: TvDevice) : ConnectionStatus()

    // 6. Verifying PIN code and calculating pairing secret
    data class VerifyingPin(val device: TvDevice) : ConnectionStatus()

    // 7. Pairing successfully completed and certificate fingerprint pinned
    data class Paired(val device: TvDevice) : ConnectionStatus()

    // 8. Connecting to Remote Control Service (TCP port 6466)
    data class ConnectingToRemoteService(val ip: String) : ConnectionStatus()

    // 9. Remote connection active and operational
    data class Connected(val device: TvDevice) : ConnectionStatus()

    // TV in Standby / Suspended state
    data class Standby(val device: TvDevice) : ConnectionStatus()

    // 10. Failed / Error state
    data class Failed(val message: String, val canRetry: Boolean = true) : ConnectionStatus()
    data class Error(val message: String, val canRetry: Boolean = true) : ConnectionStatus()

    // Compatibility aliases
    data class PairingPrompt(val device: TvDevice) : ConnectionStatus()
    object PairingInProgress : ConnectionStatus()

    val isConnected: Boolean
        get() = this is Connected

    val isConnecting: Boolean
        get() = this is Connecting ||
                this is ConnectingToPairingService ||
                this is TlsEstablished ||
                this is PairingNegotiation ||
                this is ConnectingToRemoteService ||
                this is PairingInProgress

    val isPairing: Boolean
        get() = this is ConnectingToPairingService ||
                this is TlsEstablished ||
                this is PairingNegotiation ||
                this is AwaitingTvPin ||
                this is VerifyingPin ||
                this is Paired ||
                this is PairingPrompt ||
                this is PairingInProgress
}
