package com.yunusinan.vestelremote.protocol

import com.yunusinan.vestelremote.data.protocol.ProtoUtils
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProtoUtilsTest {

    @Test
    fun testVarintEncodingAndDecoding() {
        val testValues = listOf(0L, 1L, 127L, 128L, 300L, 16384L, 2097151L, 268435455L)
        for (value in testValues) {
            val baos = ByteArrayOutputStream()
            ProtoUtils.writeVarint(baos, value)
            val bytes = baos.toByteArray()

            val bais = ByteArrayInputStream(bytes)
            val decoded = ProtoUtils.readVarint(bais)
            assertEquals("Varint encode/decode mismatch for value $value", value, decoded)
        }
    }

    @Test
    fun testMessageBuilderAndProtoMessageParser() {
        // Construct OuterMessage with:
        // field 1: protocol_version = 2
        // field 2: status = 200
        // field 10: PairingRequest (service_name="atvremote", client_name="Vestel Remote")
        val rawMessage = ProtoUtils.MessageBuilder()
            .int32(1, 2)
            .int32(2, 200)
            .message(
                10,
                ProtoUtils.MessageBuilder()
                    .string(1, "atvremote")
                    .string(2, "Vestel Remote")
            )
            .build()

        assertTrue(rawMessage.isNotEmpty())

        val parsed = ProtoUtils.ProtoMessage.parse(rawMessage)
        assertEquals(2, parsed.getInt(1))
        assertEquals(200, parsed.getInt(2))
        assertTrue(parsed.hasField(10))
        assertFalse(parsed.hasField(11))

        val inner = parsed.getMessage(10)
        assertNotNull(inner)
        assertEquals("atvremote", inner?.getString(1))
        assertEquals("Vestel Remote", inner?.getString(2))
    }

    @Test
    fun testPoloConfigurationAckParsing() {
        // OuterMessage containing ConfigurationAck (tag 31, empty message)
        val rawMessage = ProtoUtils.MessageBuilder()
            .int32(1, 2)
            .int32(2, 200)
            .message(31, ByteArray(0))
            .build()

        val parsed = ProtoUtils.ProtoMessage.parse(rawMessage)
        assertEquals(2, parsed.getInt(1))
        assertEquals(200, parsed.getInt(2))
        assertTrue(parsed.hasField(31))
        assertFalse(parsed.hasField(41))
    }

    @Test
    fun testSecretMessageEncoding() {
        val dummySecret = ByteArray(32) { (it + 1).toByte() }
        val rawMessage = ProtoUtils.MessageBuilder()
            .int32(1, 2)
            .int32(2, 200)
            .message(
                40,
                ProtoUtils.MessageBuilder().bytes(1, dummySecret)
            )
            .build()

        val parsed = ProtoUtils.ProtoMessage.parse(rawMessage)
        assertEquals(200, parsed.getInt(2))
        val secretMsg = parsed.getMessage(40)
        assertNotNull(secretMsg)
        assertArrayEquals(dummySecret, secretMsg?.getBytes(1))
    }
}
