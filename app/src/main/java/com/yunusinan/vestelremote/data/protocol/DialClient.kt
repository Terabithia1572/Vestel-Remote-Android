package com.yunusinan.vestelremote.data.protocol

import kotlinx.coroutines.Dispatchers
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
            val url = URL("http://$host:$port/apps/$dialName")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
            }

            val responseCode = conn.responseCode
            val responseBody = if (responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else ""

            val isRunning = responseBody.contains("<state>running</state>")
            Result.success(isRunning)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
