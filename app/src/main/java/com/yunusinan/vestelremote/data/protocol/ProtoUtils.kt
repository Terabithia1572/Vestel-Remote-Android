package com.yunusinan.vestelremote.data.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object ProtoUtils {

    const val MAX_FRAME_SIZE = 1024 * 1024

    fun writeFrame(out: OutputStream, payload: ByteArray) = synchronized(out) {
        require(payload.size in 1..MAX_FRAME_SIZE) { "Invalid frame length" }
        writeVarint(out, payload.size.toLong())
        out.write(payload)
        out.flush()
    }

    fun readFrame(input: InputStream): ByteArray {
        val length = readVarint(input)
        require(length in 1..MAX_FRAME_SIZE.toLong()) { "Invalid frame length" }
        return readExact(input, length.toInt())
    }

    private fun readExact(input: InputStream, length: Int): ByteArray {
        val bytes = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val count = input.read(bytes, offset, length - offset)
            if (count < 0) throw java.io.EOFException("Truncated protobuf data")
            if (count == 0) {
                val next = input.read()
                if (next < 0) throw java.io.EOFException("Truncated protobuf data")
                bytes[offset++] = next.toByte()
            } else offset += count
        }
        return bytes
    }

    const val WIRE_TYPE_VARINT = 0
    const val WIRE_TYPE_64BIT = 1
    const val WIRE_TYPE_LENGTH_DELIMITED = 2
    const val WIRE_TYPE_32BIT = 5

    fun writeVarint(out: OutputStream, value: Long) {
        var v = value
        while ((v and 0x7FL.inv()) != 0L) {
            out.write(((v and 0x7F) or 0x80).toInt())
            v = v ushr 7
        }
        out.write((v and 0x7F).toInt())
    }

    fun readVarint(input: InputStream): Long {
        var result = 0L
        var shift = 0
        while (shift < 64) {
            val b = input.read()
            if (b == -1) throw java.io.EOFException("Unexpected end of stream while reading varint")
            require(shift != 63 || (b and 0xFE) == 0) { "Varint overflow" }
            result = result or ((b and 0x7F).toLong() shl shift)
            if ((b and 0x80) == 0) return result
            shift += 7
        }
        throw IllegalArgumentException("Varint too long")
    }

    fun writeTag(out: OutputStream, fieldNumber: Int, wireType: Int) {
        writeVarint(out, ((fieldNumber shl 3) or wireType).toLong())
    }

    fun writeInt32Field(out: OutputStream, fieldNumber: Int, value: Int) {
        writeTag(out, fieldNumber, WIRE_TYPE_VARINT)
        writeVarint(out, value.toLong())
    }

    fun writeStringField(out: OutputStream, fieldNumber: Int, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeBytesField(out, fieldNumber, bytes)
    }

    fun writeBytesField(out: OutputStream, fieldNumber: Int, bytes: ByteArray) {
        writeTag(out, fieldNumber, WIRE_TYPE_LENGTH_DELIMITED)
        writeVarint(out, bytes.size.toLong())
        out.write(bytes)
    }

    fun writeMessageField(out: OutputStream, fieldNumber: Int, message: ByteArray) {
        writeBytesField(out, fieldNumber, message)
    }

    // High-level packet builder
    class MessageBuilder {
        private val baos = ByteArrayOutputStream()

        fun int32(fieldNumber: Int, value: Int): MessageBuilder {
            writeInt32Field(baos, fieldNumber, value)
            return this
        }

        fun string(fieldNumber: Int, value: String): MessageBuilder {
            writeStringField(baos, fieldNumber, value)
            return this
        }

        fun bytes(fieldNumber: Int, bytes: ByteArray): MessageBuilder {
            writeBytesField(baos, fieldNumber, bytes)
            return this
        }

        fun message(fieldNumber: Int, nested: MessageBuilder): MessageBuilder {
            writeMessageField(baos, fieldNumber, nested.build())
            return this
        }

        fun message(fieldNumber: Int, bytes: ByteArray): MessageBuilder {
            writeMessageField(baos, fieldNumber, bytes)
            return this
        }

        fun build(): ByteArray = baos.toByteArray()
    }

    data class ProtoField(
        val fieldNumber: Int,
        val wireType: Int,
        val varintValue: Long? = null,
        val bytesValue: ByteArray? = null
    )

    class ProtoMessage(val fields: List<ProtoField>) {
        fun getInt(fieldNumber: Int): Int? =
            fields.firstOrNull { it.fieldNumber == fieldNumber }?.varintValue?.toInt()

        fun getString(fieldNumber: Int): String? =
            fields.firstOrNull { it.fieldNumber == fieldNumber }?.bytesValue?.toString(Charsets.UTF_8)

        fun getBytes(fieldNumber: Int): ByteArray? =
            fields.firstOrNull { it.fieldNumber == fieldNumber }?.bytesValue

        fun getMessage(fieldNumber: Int): ProtoMessage? =
            getBytes(fieldNumber)?.let { parse(it) }

        fun hasField(fieldNumber: Int): Boolean =
            fields.any { it.fieldNumber == fieldNumber }

        companion object {
            fun parse(data: ByteArray): ProtoMessage {
                val list = mutableListOf<ProtoField>()
                val stream = ByteArrayInputStream(data)
                while (stream.available() > 0) {
                    val tag = readVarint(stream)
                    require(tag in 8..0xFFFFFFFFL) { "Invalid protobuf tag" }
                    val fieldNumber = (tag ushr 3).toInt()
                    val wireType = (tag and 0x7).toInt()
                    when (wireType) {
                        WIRE_TYPE_VARINT -> {
                            val value = readVarint(stream)
                            list.add(ProtoField(fieldNumber, wireType, varintValue = value))
                        }
                        WIRE_TYPE_LENGTH_DELIMITED -> {
                            val len = readVarint(stream)
                            require(len in 0..stream.available().toLong()) { "Invalid protobuf length" }
                            val bytes = readExact(stream, len.toInt())
                            list.add(ProtoField(fieldNumber, wireType, bytesValue = bytes))
                        }
                        WIRE_TYPE_64BIT -> {
                            readExact(stream, 8)
                        }
                        WIRE_TYPE_32BIT -> {
                            readExact(stream, 4)
                        }
                        else -> {
                            throw IllegalArgumentException("Unsupported protobuf wire type")
                        }
                    }
                }
                return ProtoMessage(list)
            }
        }
    }
}
