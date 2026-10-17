package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.protocol.*
import com.yunusinan.vestelremote.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Properties

internal object Fixtures {
    private val data = Properties().apply {
        Fixtures::class.java.getResourceAsStream("/protocol/wire.properties")!!.use { load(it) }
    }
    fun bytes(name: String): ByteArray = data.getProperty(name).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    fun message(name: String) = ProtoUtils.ProtoMessage.parse(bytes(name))
    fun frame(name: String): ByteArray = ByteArrayOutputStream().also { ProtoUtils.writeFrame(it, bytes(name)) }.toByteArray()
}

class ReferenceProtocolTest {
    @Test fun pairingPacketsMatchIndependentProtobufFixtures() {
        assertArrayEquals(Fixtures.bytes("pair_request"), PairingProtocol.request())
        assertArrayEquals(Fixtures.bytes("pair_options"), PairingProtocol.options())
        assertArrayEquals(Fixtures.bytes("pair_configuration"), PairingProtocol.configuration())
        assertArrayEquals(Fixtures.bytes("pair_secret"), PairingProtocol.secret(ByteArray(32) { it.toByte() }))
    }

    @Test fun expectedPairingMessagesAreRequiredEvenWithStatusOk() {
        for ((name, field) in listOf("pair_request_ack" to 11, "server_options" to 20,
                "configuration_ack" to 31, "secret_ack" to 41)) {
            PairingProtocol.validate(Fixtures.message(name), field)
            assertThrows(ProtocolFailure::class.java) { PairingProtocol.validate(Fixtures.message("status_only"), field) }
            assertThrows(ProtocolFailure::class.java) { PairingProtocol.validate(Fixtures.message("bad_secret"), field) }
        }
        assertThrows(ProtocolFailure::class.java) { PairingProtocol.validate(Fixtures.message("pair_request_ack"), 31) }
        assertThrows(ProtocolFailure::class.java) { PairingProtocol.validate(Fixtures.message("pair_options"), 20) }
    }

    @Test fun remoteNegotiatesFeatureMaskAndWaitsForStart() {
        val p = RemoteProtocol()
        assertArrayEquals(Fixtures.bytes("client_configure"), p.accept(Fixtures.message("server_configure")).single())
        assertFalse(p.started)
        assertArrayEquals(Fixtures.bytes("client_active"), p.accept(Fixtures.message("server_active")).single())
        assertFalse(p.started)
        p.accept(Fixtures.message("remote_start"))
        assertTrue(p.started)
        assertArrayEquals(Fixtures.bytes("pong"), p.accept(Fixtures.message("ping")).single())
    }

    @Test fun unexpectedStartAndServerErrorDoNotSucceed() {
        assertThrows(ProtocolFailure::class.java) { RemoteProtocol().accept(Fixtures.message("remote_start")) }
        assertThrows(ProtocolFailure::class.java) { RemoteProtocol().accept(Fixtures.message("server_active")) }
        assertThrows(ProtocolFailure::class.java) { RemoteProtocol().accept(Fixtures.message("remote_error")) }
        val p = RemoteProtocol()
        p.accept(Fixtures.message("server_configure"))
        p.accept(Fixtures.message("remote_start_off"))
        assertTrue("Power off is not an authentication error", p.started)
    }

    @Test fun keyInjectionMatchesReferenceFixtures() {
        val keys = mapOf("up" to TvCommand.DPadUp, "down" to TvCommand.DPadDown,
            "left" to TvCommand.DPadLeft, "right" to TvCommand.DPadRight, "select" to TvCommand.Select,
            "home" to TvCommand.Home, "back" to TvCommand.Back, "volume_up" to TvCommand.VolumeUp,
            "volume_down" to TvCommand.VolumeDown, "mute" to TvCommand.Mute)
        keys.forEach { (name, key) -> assertArrayEquals(Fixtures.bytes("key_$name"), RemoteProtocol.key(key)) }
        assertArrayEquals(Fixtures.bytes("long_app_link"), RemoteProtocol.appLink("https://example.com/" + "x".repeat(140)))
    }

    @Test fun multiByteFramingHandlesFragmentedAndCoalescedPackets() {
        val all = Fixtures.frame("long_app_link") + Fixtures.frame("ping")
        val fragmented = object : InputStream() {
            val input = ByteArrayInputStream(all)
            override fun read() = input.read()
            override fun read(b: ByteArray, off: Int, len: Int) = input.read(b, off, minOf(len, 1))
        }
        assertTrue(Fixtures.frame("long_app_link")[0].toInt() and 0x80 != 0)
        assertArrayEquals(Fixtures.bytes("long_app_link"), ProtoUtils.readFrame(fragmented))
        assertArrayEquals(Fixtures.bytes("ping"), ProtoUtils.readFrame(fragmented))
    }

    @Test fun malformedProtobufAndFramesAreRejected() {
        val invalid = listOf("fa0104", "00", "80", "0901", "0d01", "0e", "0affffffff0f")
        invalid.forEach { hex ->
            assertThrows(Exception::class.java) { ProtoUtils.ProtoMessage.parse(hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()) }
        }
        for (size in listOf(0L, 1048577L, 4294967296L, -1L)) {
            val out = ByteArrayOutputStream()
            ProtoUtils.writeVarint(out, size)
            assertThrows(IllegalArgumentException::class.java) { ProtoUtils.readFrame(ByteArrayInputStream(out.toByteArray())) }
        }
        assertThrows(java.io.EOFException::class.java) { ProtoUtils.readFrame(ByteArrayInputStream(byteArrayOf(3, 1))) }
    }

    @Test fun diagnosticsRecordTimeoutAndNeverCopyRawExceptions() {
        var entry = ConnectionDiagnostic(host = "127.0.0.1", port = 6467, service = "pairing")
        val recorder = AttemptDiagnostics(entry) { entry = it }
        recorder.stage("ConfigurationAck")
        recorder.update { it.copy(lastOutgoing = "Configuration", lastIncoming = "Options", serverStatus = 200) }
        recorder.failed(java.net.SocketTimeoutException("secret=A1B2C3 private_key=hidden"))
        assertEquals("ConfigurationAck", entry.timeoutStage)
        assertEquals("java.net.SocketTimeoutException", entry.exceptionClass)
        assertFalse(entry.export().contains("A1B2C3"))
        assertFalse(entry.export().contains("private_key"))
        assertEquals("Configuration", entry.lastOutgoing)
    }
}
