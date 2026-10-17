package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.domain.model.AppDiscoveryInfo
import com.yunusinan.vestelremote.domain.model.AppDiscoveryState
import com.yunusinan.vestelremote.domain.model.TvApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DialClient(
    private val host: String,
    private val port: Int = 8008,
    private val onEventLog: ((title: String, detail: String) -> Unit)? = null
) {

    suspend fun launchApp(dialName: String, params: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$host:$port/apps/$dialName")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = params != null
                setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            }

            if (params != null) {
                conn.outputStream.use { it.write(params.toByteArray(Charsets.UTF_8)) }
            }

            val responseCode = conn.responseCode
            onEventLog?.invoke("DIAL Launch", "App: $dialName, HTTP Response: $responseCode")
            Result.success(responseCode in 200..299 || responseCode == 303)
        } catch (e: Exception) {
            onEventLog?.invoke("DIAL Error", "Could not launch $dialName: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun isAppRunning(dialName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val info = queryDialEndpoint(appId = dialName, dialName = dialName)
            Result.success(info.state == AppDiscoveryState.VERIFIED_RUNNING)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Probes all apps in [catalog] against the TV's DIAL REST server (port 8008)
     * to determine which apps are verifiably installed/registered and whether they are running.
     */
    suspend fun probeCatalogApps(catalog: List<TvApp>): Map<String, AppDiscoveryInfo> = withContext(Dispatchers.IO) {
        coroutineScope {
            val results = catalog.map { app ->
                async {
                    val info = probeApp(app)
                    app.id to info
                }
            }.awaitAll()
            results.toMap()
        }
    }

    suspend fun probeApp(app: TvApp): AppDiscoveryInfo = withContext(Dispatchers.IO) {
        val primaryName = app.dialName
        if (primaryName.isNullOrBlank()) {
            return@withContext AppDiscoveryInfo(
                appId = app.id,
                state = AppDiscoveryState.UNVERIFIED_SHORTCUT
            )
        }

        val candidates = buildList {
            add(primaryName)
            when (app.id) {
                "prime_video" -> {
                    add("PrimeVideo")
                    add("AmazonVideo")
                }
                "disney_plus" -> add("Disney+")
                "youtube_music" -> add("YouTubeMusicTV")
            }
        }.distinct()

        var lastResult = AppDiscoveryInfo(appId = app.id, state = AppDiscoveryState.UNVERIFIED_SHORTCUT)
        for (candidate in candidates) {
            val info = queryDialEndpoint(app.id, candidate)
            if (info.isVerifiedOnTv) {
                return@withContext info
            }
            lastResult = info
        }
        lastResult
    }

    private fun queryDialEndpoint(appId: String, dialName: String): AppDiscoveryInfo {
        return try {
            val url = URL("http://$host:$port/apps/$dialName")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1800
                readTimeout = 1800
                instanceFollowRedirects = true
            }
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
            parseDialServiceXml(appId = appId, dialName = dialName, httpStatusCode = code, xmlBody = body)
        } catch (_: Exception) {
            AppDiscoveryInfo(
                appId = appId,
                state = AppDiscoveryState.UNVERIFIED_SHORTCUT,
                dialServiceName = dialName
            )
        }
    }

    companion object {
        /**
         * Parses a DIAL REST Application Resource XML response into [AppDiscoveryInfo].
         */
        fun parseDialServiceXml(
            appId: String,
            dialName: String,
            httpStatusCode: Int,
            xmlBody: String
        ): AppDiscoveryInfo {
            if (httpStatusCode == 404) {
                return AppDiscoveryInfo(
                    appId = appId,
                    state = AppDiscoveryState.DIAL_NOT_REGISTERED,
                    dialServiceName = dialName
                )
            }
            if (httpStatusCode !in 200..299) {
                return AppDiscoveryInfo(
                    appId = appId,
                    state = AppDiscoveryState.UNVERIFIED_SHORTCUT,
                    dialServiceName = dialName
                )
            }

            val normalized = xmlBody.lowercase()
            val state = when {
                normalized.contains("<state>running</state>") -> AppDiscoveryState.VERIFIED_RUNNING
                normalized.contains("<state>stopped</state>") ||
                    normalized.contains("<state>installable") == false && normalized.contains("<service") ->
                    AppDiscoveryState.VERIFIED_STOPPED
                else -> AppDiscoveryState.UNVERIFIED_SHORTCUT
            }
            val allowStop = normalized.contains("allowstop=\"true\"") || normalized.contains("allowstop='true'")
            return AppDiscoveryInfo(
                appId = appId,
                state = state,
                dialServiceName = dialName,
                allowStop = allowStop
            )
        }
    }
}
