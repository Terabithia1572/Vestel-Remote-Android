package com.yunusinan.vestelremote.data.protocol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object WakeOnLan {

    suspend fun wake(macAddress: String, targetIp: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val macBytes = parseMac(macAddress)
                ?: return@withContext Result.failure(IllegalArgumentException("Geçersiz MAC adresi formatı. Örn: AA:BB:CC:DD:EE:FF"))

            // Magic packet: 6x 0xFF followed by 16x MAC
            val packetData = ByteArray(6 + 16 * macBytes.size)
            for (i in 0 until 6) {
                packetData[i] = 0xFF.toByte()
            }
            for (i in 0 until 16) {
                System.arraycopy(macBytes, 0, packetData, 6 + i * macBytes.size, macBytes.size)
            }

            DatagramSocket().use { socket ->
                socket.broadcast = true

                // Broadcast to 255.255.255.255
                val broadcastPacket = DatagramPacket(
                    packetData,
                    packetData.size,
                    InetAddress.getByName("255.255.255.255"),
                    9
                )
                socket.send(broadcastPacket)

                // Also send directly to target IP if provided
                if (!targetIp.isNullOrBlank() && targetIp != "255.255.255.255") {
                    try {
                        val directPacket = DatagramPacket(
                            packetData,
                            packetData.size,
                            InetAddress.getByName(targetIp),
                            9
                        )
                        socket.send(directPacket)
                    } catch (_: Exception) {}
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseMac(macStr: String): ByteArray? {
        val clean = macStr.trim().replace("-", ":").replace(".", ":")
        val parts = clean.split(":")
        if (parts.size != 6) return null

        val bytes = ByteArray(6)
        try {
            for (i in 0 until 6) {
                val hex = parts[i]
                if (hex.length != 2) return null
                bytes[i] = hex.toInt(16).toByte()
            }
            return bytes
        } catch (e: Exception) {
            return null
        }
    }
}
