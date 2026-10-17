package com.yunusinan.vestelremote.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yunusinan.vestelremote.domain.model.TvDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vestel_remote_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SAVED_DEVICES = stringPreferencesKey("saved_devices")
        val KEY_ACTIVE_DEVICE_IP = stringPreferencesKey("active_device_ip")
        val KEY_FAVORITE_APPS = stringSetPreferencesKey("favorite_apps")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        val KEY_MOCK_MODE = booleanPreferencesKey("mock_mode_enabled")
        val KEY_LAST_KNOWN_MAC = stringPreferencesKey("last_known_mac")
    }

    val savedDevicesFlow: Flow<List<TvDevice>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_SAVED_DEVICES] ?: "[]"
        parseDevicesJson(jsonStr)
    }

    val activeDeviceIpFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_DEVICE_IP]
    }

    val favoriteAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_APPS] ?: setOf("youtube", "netflix", "prime_video", "disney_plus")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "tr"
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "dark"
    }

    val hapticsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS] ?: true
    }

    val mockModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MOCK_MODE] ?: false
    }

    suspend fun saveDevice(device: TvDevice) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_SAVED_DEVICES] ?: "[]"
            val currentList = parseDevicesJson(jsonStr).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.ipAddress == device.ipAddress }
            if (existingIndex >= 0) {
                currentList[existingIndex] = device
            } else {
                currentList.add(device)
            }
            prefs[KEY_SAVED_DEVICES] = serializeDevicesJson(currentList)
            prefs[KEY_ACTIVE_DEVICE_IP] = device.ipAddress
            if (!device.macAddress.isNullOrBlank()) {
                prefs[KEY_LAST_KNOWN_MAC] = device.macAddress
            }
        }
    }

    suspend fun removeDevice(ipAddress: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_SAVED_DEVICES] ?: "[]"
            val currentList = parseDevicesJson(jsonStr).filterNot { it.ipAddress == ipAddress }
            prefs[KEY_SAVED_DEVICES] = serializeDevicesJson(currentList)
            if (prefs[KEY_ACTIVE_DEVICE_IP] == ipAddress) {
                prefs[KEY_ACTIVE_DEVICE_IP] = currentList.firstOrNull()?.ipAddress ?: ""
            }
        }
    }

    suspend fun setActiveDeviceIp(ip: String) {
        context.dataStore.edit { it[KEY_ACTIVE_DEVICE_IP] = ip }
    }

    suspend fun toggleFavoriteApp(appId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITE_APPS] ?: setOf("youtube", "netflix", "prime_video", "disney_plus")).toMutableSet()
            if (current.contains(appId)) {
                current.remove(appId)
            } else {
                current.add(appId)
            }
            prefs[KEY_FAVORITE_APPS] = current
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    suspend fun setMockMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MOCK_MODE] = enabled }
    }

    private fun parseDevicesJson(json: String): List<TvDevice> {
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<TvDevice>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TvDevice(
                        id = obj.optString("id", "tv-${obj.optString("ip")}"),
                        name = obj.optString("name", "Vestel Android TV"),
                        model = obj.optString("model", "55QG9960"),
                        ipAddress = obj.optString("ip", ""),
                        macAddress = obj.optString("mac").takeIf { it.isNotBlank() },
                        isPaired = obj.optBoolean("isPaired", false),
                        serverFingerprint = obj.optString("serverFingerprint").takeIf { it.matches(Regex("[0-9a-fA-F]{64}")) },
                        port = obj.optInt("port", 6466),
                        pairingPort = obj.optInt("pairingPort", 6467),
                        isOnline = false
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeDevicesJson(devices: List<TvDevice>): String {
        val arr = JSONArray()
        for (d in devices) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("name", d.name)
                put("model", d.model)
                put("ip", d.ipAddress)
                put("mac", d.macAddress ?: "")
                put("isPaired", d.isPaired)
                put("serverFingerprint", d.serverFingerprint ?: "")
                put("port", d.port)
                put("pairingPort", d.pairingPort)
            }
            arr.put(obj)
        }
        return arr.toString()
    }
}
