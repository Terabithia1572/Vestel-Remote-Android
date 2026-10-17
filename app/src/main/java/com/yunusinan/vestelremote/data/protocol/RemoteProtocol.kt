package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.domain.model.TvCommand

/** Wire constants checked against tronikos polo.proto/remotemessage.proto and louis49. */
object PairingProtocol {
    private fun envelope(field: Int, body: ProtoUtils.MessageBuilder) = ProtoUtils.MessageBuilder()
        .int32(1, 2).int32(2, 200).message(field, body).build()
    private fun encoding() = ProtoUtils.MessageBuilder().int32(1, 3).int32(2, 6)
    fun request() = envelope(10, ProtoUtils.MessageBuilder().string(1, "atvremote").string(2, "Vestel Remote"))
    fun options() = envelope(20, ProtoUtils.MessageBuilder().message(1, encoding()).int32(3, 1))
    fun configuration() = envelope(30, ProtoUtils.MessageBuilder().message(1, encoding()).int32(2, 1))
    fun secret(secret: ByteArray) = envelope(40, ProtoUtils.MessageBuilder().bytes(1, secret))

    val names = mapOf(10 to "PairingRequest", 11 to "PairingRequestAck", 20 to "Options",
        30 to "Configuration", 31 to "ConfigurationAck", 40 to "Secret", 41 to "SecretAck")

    fun validate(message: ProtoUtils.ProtoMessage, expected: Int) {
        if (message.getInt(2) != 200) throw ProtocolFailure("TV eşleştirmeyi reddetti; durum=${message.getInt(2)}")
        if (message.getInt(1) != 2) throw ProtocolFailure("Desteklenmeyen eşleştirme protokol sürümü")
        val payloads = message.fields.filter { it.fieldNumber in names }
        if (payloads.size != 1 || payloads.single().fieldNumber != expected ||
            payloads.single().wireType != ProtoUtils.WIRE_TYPE_LENGTH_DELIMITED) {
            throw ProtocolFailure("Beklenmeyen eşleştirme yanıtı; beklenen=${names[expected]}")
        }
        // Parse nested data as well, so a malformed ACK cannot open the PIN dialog.
        val body = message.getMessage(expected)!!
        if (expected == 20) {
            val outputs = body.fields.filter { it.fieldNumber == 2 }.mapNotNull { it.bytesValue }
            if (outputs.none { bytes ->
                    val enc = ProtoUtils.ProtoMessage.parse(bytes)
                    enc.getInt(1) == 3 && enc.getInt(2) == 6
                }) throw ProtocolFailure("TV altı sembollü HEX çıktı kodlamasını sunmadı")
        }
        if (expected == 41 && body.getBytes(1)?.size != 32) {
            throw ProtocolFailure("Geçersiz SecretAck")
        }
    }
}

class RemoteProtocol {
    companion object {
        // PING | KEY | POWER | VOLUME | APP_LINK. Do not advertise unsupported IME/voice.
        const val SUPPORTED_FEATURES = 611
        val names = mapOf(1 to "RemoteConfigure", 2 to "RemoteSetActive", 3 to "RemoteError",
            8 to "RemotePingRequest", 9 to "RemotePingResponse", 10 to "RemoteKeyInject",
            20 to "RemoteImeKeyInject", 21 to "RemoteImeBatchEdit", 22 to "RemoteImeShowRequest",
            40 to "RemoteStart", 50 to "RemoteSetVolumeLevel", 90 to "RemoteAppLinkLaunchRequest")
        fun key(command: TvCommand) = ProtoUtils.MessageBuilder().message(10,
            ProtoUtils.MessageBuilder().int32(1, command.androidKeyCode).int32(2, 3)).build()
        fun appLink(uri: String) = ProtoUtils.MessageBuilder().message(90,
            ProtoUtils.MessageBuilder().string(1, uri)).build()
    }

    private var features: Int? = null
    var started = false
        private set

    fun accept(message: ProtoUtils.ProtoMessage): List<ByteArray> {
        if (message.hasField(3)) throw ProtocolFailure("TV RemoteError gönderdi")
        val replies = mutableListOf<ByteArray>()
        message.getMessage(1)?.let { config ->
            val negotiated = (config.getInt(1) ?: 0) and SUPPORTED_FEATURES
            if (negotiated and 2 == 0) throw ProtocolFailure("TV tuş enjeksiyonu özelliğini sunmadı")
            features = negotiated
            replies += ProtoUtils.MessageBuilder().message(1, ProtoUtils.MessageBuilder()
                .int32(1, negotiated).message(2, ProtoUtils.MessageBuilder()
                    .string(1, "Vestel Remote").string(2, "Android").int32(3, 1).string(4, "1")
                    .string(5, "com.yunusinan.vestelremote").string(6, "1.0.0"))).build()
        }
        message.getMessage(2)?.let {
            val active = features ?: throw ProtocolFailure("RemoteConfigure öncesinde RemoteSetActive alındı")
            replies += ProtoUtils.MessageBuilder().message(2, ProtoUtils.MessageBuilder().int32(1, active)).build()
        }
        message.getMessage(8)?.let { ping ->
            replies += ProtoUtils.MessageBuilder().message(9,
                ProtoUtils.MessageBuilder().int32(1, ping.getInt(1) ?: 0)).build()
        }
        message.getMessage(40)?.let {
            if (features == null) throw ProtocolFailure("RemoteConfigure öncesinde RemoteStart alındı")
            // started=false is TV power state, not an authentication failure.
            started = true
        }
        return replies
    }
}
