package com.yunusinan.vestelremote.data.protocol

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.yunusinan.vestelremote.domain.model.TvDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TvDiscoveryManager(
    private val context: Context,
    private val onLog: ((String, String) -> Unit)? = null
) {

    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices.asStateFlow()

    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private val scanMutex = Mutex()
    @Volatile private var scanGeneration = 0
    @Volatile private var isScanning = false

    init {
        try {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
        } catch (_: Exception) {}
    }

    suspend fun startDiscovery(durationMs: Long = 6000) = withContext(Dispatchers.IO) {
        if (!scanMutex.tryLock()) return@withContext
        val generation = ++scanGeneration
        isScanning = true
        _discoveredDevices.value = emptyList()
        onLog?.invoke("Discovery Started", "Searching via mDNS and SSDP...")

        acquireMulticastLock()

        val foundDevices = mutableMapOf<String, TvDevice>()

        val queue = Channel<NsdServiceInfo>(Channel.UNLIMITED)
        var resolver: Job? = null
        var ssdpJob: Job? = null
        try {
            resolver = launch {
                for (service in queue) {
                    val resolved = resolve(service) ?: continue
                    if (!isScanning || generation != scanGeneration) continue
                    val host = resolved.host?.hostAddress ?: continue
                    if (host.startsWith("127.") || host == "0.0.0.0") continue
                    val tv = TvDevice(id = "atv-$host", name = resolved.serviceName ?: "Android TV",
                        ipAddress = host, port = resolved.port.takeIf { it > 0 } ?: 6466,
                        isOnline = true, hasAndroidTvRemote2 = true)
                    synchronized(foundDevices) {
                        foundDevices[host] = tv
                        _discoveredDevices.value = foundDevices.values.toList()
                    }
                    onLog?.invoke("mDNS TV Found", "${tv.name} at $host:${tv.port}")
                }
            }
            startNsdDiscovery(queue)
            ssdpJob = launch { startSsdpDiscovery(foundDevices) }
            delay(durationMs)
        } finally {
            isScanning = false
            queue.close()
            resolver?.cancel()
            ssdpJob?.cancel()
            stopNsd()
            releaseMulticastLock()
            scanMutex.unlock()
        }
        onLog?.invoke("Discovery Complete", "Found ${synchronized(foundDevices) { foundDevices.size }} television(s)")
    }

    private fun acquireMulticastLock() {
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifi?.createMulticastLock("vestel_remote_multicast_lock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (_: Exception) {}
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        multicastLock = null
    }

    private suspend fun resolve(service: NsdServiceInfo): NsdServiceInfo? = suspendCancellableCoroutine { continuation ->
        val manager = nsdManager
        if (manager == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        try {
            manager.resolveService(service, object : NsdManager.ResolveListener {
                override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                    onLog?.invoke("mDNS Resolve Failed", "errorCode=$errorCode")
                    if (continuation.isActive) continuation.resume(null)
                }
                override fun onServiceResolved(info: NsdServiceInfo) {
                    if (continuation.isActive) continuation.resume(info)
                }
            })
        } catch (e: Exception) {
            onLog?.invoke("mDNS Resolve Failed", e.javaClass.simpleName)
            if (continuation.isActive) continuation.resume(null)
        }
    }

    private fun startNsdDiscovery(queue: Channel<NsdServiceInfo>) {
        val manager = nsdManager ?: return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                queue.trySend(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                onLog?.invoke("mDNS Start Failed", "errorCode=$errorCode")
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                onLog?.invoke("mDNS Stop Failed", "errorCode=$errorCode")
            }
        }

        try {
            manager.discoverServices("_androidtvremote2._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            onLog?.invoke("mDNS Notice", "mDNS discovery unavailable on this network: ${e.localizedMessage}")
        }
    }

    private fun stopNsd() {
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (_: Exception) {}
        discoveryListener = null
    }

    private suspend fun startSsdpDiscovery(devices: MutableMap<String, TvDevice>) = withContext(Dispatchers.IO) {
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = 3000
                val ssdpMessage = "M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "MX: 2\r\n" +
                        "ST: urn:dial-multiscreen-org:service:dial:1\r\n\r\n"

                val bytes = ssdpMessage.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(bytes, bytes.size, InetAddress.getByName("239.255.255.250"), 1900)
                socket.send(packet)

                val buffer = ByteArray(4096)
                val receivePacket = DatagramPacket(buffer, buffer.size)

                val endTime = System.currentTimeMillis() + 3500
                while (System.currentTimeMillis() < endTime && isActive) {
                    try {
                        receivePacket.length = buffer.size
                        socket.receive(receivePacket)
                        ensureActive()
                        val senderIp = receivePacket.address.hostAddress ?: continue
                        val responseText = String(receivePacket.data, 0, receivePacket.length)

                        if (responseText.contains("LOCATION:", ignoreCase = true) ||
                            responseText.contains("DIAL", ignoreCase = true)
                        ) {
                            val tv = TvDevice(
                                id = "dial-$senderIp",
                                name = "DIAL TV",
                                model = "55QG9960",
                                ipAddress = senderIp,
                                port = 6466,
                                isOnline = true,
                                hasAndroidTvRemote2 = false,
                                hasDial = true
                            )
                            synchronized(devices) {
                                if (!devices.containsKey(senderIp)) {
                                    devices[senderIp] = tv
                                    _discoveredDevices.value = devices.values.toList()
                                }
                            }
                            onLog?.invoke("SSDP/DIAL TV Found", "Detected device at $senderIp")
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onLog?.invoke("SSDP Notice", "SSDP search finished: ${e.localizedMessage}")
        }
    }

    /**
     * Checks if a specific IP has Android TV Remote or DIAL ports open.
     */
    suspend fun verifyDeviceReachability(ip: String): Boolean = withContext(Dispatchers.IO) {
        val portsToCheck = listOf(6466, 6467, 8008)
        for (port in portsToCheck) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 800)
                    return@withContext true
                }
            } catch (_: Exception) {}
        }
        false
    }
}
