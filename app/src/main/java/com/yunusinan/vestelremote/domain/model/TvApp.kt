package com.yunusinan.vestelremote.domain.model

import java.util.UUID

enum class AppDiscoveryState {
    /** App has not been probed yet or only uses Deep Link shortcut without DIAL verification. */
    UNVERIFIED_SHORTCUT,
    /** User has explicitly confirmed that this APK is installed on the television. */
    USER_CONFIRMED_INSTALLED,
    /** Verified present on TV via DIAL HTTP 200 (<state>stopped</state>). */
    VERIFIED_STOPPED,
    /** Verified currently running on TV via DIAL HTTP 200 (<state>running</state>). */
    VERIFIED_RUNNING,
    /** Probed via DIAL HTTP 404; not registered on DIAL server (still usable as Deep Link shortcut). */
    DIAL_NOT_REGISTERED
}

enum class AppCatalogFilter {
    ALL,
    INSTALLED,
    SYSTEM,
    FAVORITES
}

enum class AppLaunchStrategy {
    /** Strategy 1: Verified Android TV Remote v2 RemoteAppLinkLaunchRequest (Field 90) deep link. */
    REMOTE_APP_LINK,
    /** Strategy 2: Supported DIAL REST endpoint on Port 8008. */
    DIAL_ONLY,
    /** Strategy 2 with Strategy 1 fallback (DIAL first, then Android TV Remote v2 app link). */
    DIAL_WITH_APP_LINK_FALLBACK,
    /** Neither a BROWSABLE VIEW deep link nor a DIAL endpoint exists in the APK manifest. */
    UNSUPPORTED_DIRECT_LAUNCH
}

enum class AppLaunchDispatchStatus {
    /** Protobuf Field 90 frame was transmitted over TLS; protocol does not return app-open ACK. */
    COMMAND_SENT,
    /** DIAL HTTP POST returned 2xx/303 confirming launch request acceptance. */
    LAUNCH_ACCEPTED,
    /** DIAL follow-up probe (<state>running</state>) or Mock TV confirmed app is visibly running. */
    APPLICATION_VISIBLY_OPENED,
    /** Target APK does not expose any supported remote launch URI scheme or DIAL endpoint. */
    UNSUPPORTED_CAPABILITY,
    /** Network, connection state, timeout, or protocol failure prevented dispatch. */
    DISPATCH_FAILED
}

data class AppLaunchDiagnostic(
    val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val appDisplayName: String,
    val packageName: String,
    val selectedStrategy: AppLaunchStrategy,
    val attemptedSchemeOrEndpoint: String,
    val dispatchResult: AppLaunchDispatchStatus,
    val protocolAcknowledgment: String,
    val timeoutOrErrorReason: String? = null,
    val unsupportedCapabilityReason: String? = null,
    val userFeedbackTr: String
) {
    fun export(): String = """
        launchAttempt=$id timestampMs=$timestampMs
        app=$appDisplayName package=$packageName
        strategy=${selectedStrategy.name} endpoint=$attemptedSchemeOrEndpoint
        result=${dispatchResult.name} ack=$protocolAcknowledgment
        error=${timeoutOrErrorReason ?: "None"}
        unsupportedReason=${unsupportedCapabilityReason ?: "None"}
    """.trimIndent()
}

data class AppDiscoveryInfo(
    val appId: String,
    val state: AppDiscoveryState = AppDiscoveryState.UNVERIFIED_SHORTCUT,
    val dialServiceName: String? = null,
    val allowStop: Boolean = false
) {
    val isVerifiedOnTv: Boolean
        get() = state == AppDiscoveryState.VERIFIED_STOPPED || state == AppDiscoveryState.VERIFIED_RUNNING
}

data class TvApp(
    val id: String,
    val name: String,
    val packageName: String,
    val dialName: String? = null,
    val deepLinkUri: String? = null,
    val isFavorite: Boolean = false,
    val category: String = "Streaming",
    val discoveryState: AppDiscoveryState = AppDiscoveryState.UNVERIFIED_SHORTCUT,
    val isUserConfirmedInstalled: Boolean = false,
    val launcherActivity: String? = null,
    val leanbackLauncherActivity: String? = null,
    val versionName: String? = null,
    val versionCode: Int? = null,
    val launchStrategy: AppLaunchStrategy = when {
        !dialName.isNullOrBlank() && isValidAppLinkUri(deepLinkUri) -> AppLaunchStrategy.DIAL_WITH_APP_LINK_FALLBACK
        !dialName.isNullOrBlank() -> AppLaunchStrategy.DIAL_ONLY
        isValidAppLinkUri(deepLinkUri) -> AppLaunchStrategy.REMOTE_APP_LINK
        else -> AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH
    },
    val unsupportedLaunchReasonTr: String? = null
) {
    /** True ONLY when DIAL (Port 8008) independently verified the application on the TV. */
    val isVerifiedOnTv: Boolean
        get() = discoveryState == AppDiscoveryState.VERIFIED_STOPPED ||
            discoveryState == AppDiscoveryState.VERIFIED_RUNNING

    /** True when either DIAL verified the app OR the user confirmed the APK is installed on the TV. */
    val isInstalledOnTv: Boolean
        get() = isVerifiedOnTv ||
            isUserConfirmedInstalled ||
            discoveryState == AppDiscoveryState.USER_CONFIRMED_INSTALLED

    /** Accurate installation status representation required by Phase B. */
    val installationStatusLabel: String
        get() = when (discoveryState) {
            AppDiscoveryState.VERIFIED_RUNNING -> "DIAL defined: running"
            AppDiscoveryState.VERIFIED_STOPPED -> "DIAL verified installed"
            AppDiscoveryState.USER_CONFIRMED_INSTALLED -> USER_CONFIRMED_INSTALLED_LABEL
            AppDiscoveryState.DIAL_NOT_REGISTERED,
            AppDiscoveryState.UNVERIFIED_SHORTCUT -> if (isUserConfirmedInstalled) {
                USER_CONFIRMED_INSTALLED_LABEL
            } else {
                "Shortcut"
            }
        }

    /** Localized Turkish badge label for UI presentation. */
    val installationBadgeLabelTr: String
        get() = when {
            discoveryState == AppDiscoveryState.VERIFIED_RUNNING -> "Açık (DIAL)"
            discoveryState == AppDiscoveryState.VERIFIED_STOPPED -> "Kurulu (DIAL)"
            isUserConfirmedInstalled || discoveryState == AppDiscoveryState.USER_CONFIRMED_INSTALLED -> "Kurulu (Onaylı)"
            else -> "Kısayol"
        }

    /** True if a supported remote launch mechanism (verified App Link URI or DIAL endpoint) exists. */
    val supportsDirectLaunch: Boolean
        get() = when (launchStrategy) {
            AppLaunchStrategy.REMOTE_APP_LINK -> isValidAppLinkUri(deepLinkUri)
            AppLaunchStrategy.DIAL_ONLY -> !dialName.isNullOrBlank()
            AppLaunchStrategy.DIAL_WITH_APP_LINK_FALLBACK -> !dialName.isNullOrBlank() || isValidAppLinkUri(deepLinkUri)
            AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH -> false
        }

    companion object {
        const val USER_CONFIRMED_INSTALLED_LABEL = "User-confirmed installed"

        private val VALID_URI_SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")

        /**
         * Validates that [uri] is a genuine URI with a scheme (`scheme://...`) and not
         * a bare Android package name (`com.example.app`) or blank string.
         */
        fun isValidAppLinkUri(uri: String?): Boolean {
            if (uri.isNullOrBlank()) return false
            val trimmed = uri.trim()
            if (!VALID_URI_SCHEME_REGEX.matches(trimmed)) return false
            val scheme = trimmed.substringBefore("://").lowercase()
            return scheme.isNotBlank()
        }

        /**
         * Sanitizes a URI or endpoint for diagnostics logging so query parameters,
         * tokens, credentials, or sensitive paths are never recorded.
         */
        fun sanitizeEndpointForDiagnostics(rawUriOrEndpoint: String?): String {
            if (rawUriOrEndpoint.isNullOrBlank()) return "none"
            val trimmed = rawUriOrEndpoint.trim()
            if (trimmed.startsWith("dial:", ignoreCase = true)) {
                return trimmed.substringBefore("?").substringBefore("#")
            }
            if (!trimmed.contains("://")) {
                return "invalid_uri_format"
            }
            val scheme = trimmed.substringBefore("://").lowercase()
            val afterScheme = trimmed.substringAfter("://", "")
            val authority = afterScheme
                .substringAfter("@", afterScheme)
                .substringBefore("/")
                .substringBefore("?")
                .substringBefore("#")
            return if (authority.isBlank()) {
                "$scheme://"
            } else {
                "$scheme://$authority"
            }
        }

        /**
         * Removes duplicate catalog entries by [id] and [packageName] while preserving order.
         */
        fun deduplicateCatalog(apps: List<TvApp>): List<TvApp> {
            val seenIds = mutableSetOf<String>()
            val seenPackages = mutableSetOf<String>()
            return apps.filter { app ->
                val idKey = app.id.trim().lowercase()
                val pkgKey = app.packageName.trim().lowercase()
                val isUnique = seenIds.add(idKey) && seenPackages.add(pkgKey)
                isUnique
            }
        }

        /**
         * Filters the application catalog by search query, category filter tab, and favorites.
         */
        fun filterCatalog(
            apps: List<TvApp>,
            searchQuery: String,
            filterTab: AppCatalogFilter,
            favoriteAppIds: Set<String>
        ): List<TvApp> {
            val deduplicated = deduplicateCatalog(apps)
            val q = searchQuery.trim()
            val byQuery = if (q.isEmpty()) {
                deduplicated
            } else {
                deduplicated.filter { app ->
                    app.name.contains(q, ignoreCase = true) ||
                        app.category.contains(q, ignoreCase = true) ||
                        app.packageName.contains(q, ignoreCase = true) ||
                        (app.launcherActivity?.contains(q, ignoreCase = true) == true)
                }
            }
            return when (filterTab) {
                AppCatalogFilter.ALL -> byQuery
                AppCatalogFilter.INSTALLED -> {
                    val installed = byQuery.filter { it.isInstalledOnTv }
                    if (installed.isNotEmpty()) installed else byQuery.filter { it.dialName != null || it.isUserConfirmedInstalled }
                }
                AppCatalogFilter.SYSTEM -> byQuery.filter { it.category.equals("Sistem", ignoreCase = true) }
                AppCatalogFilter.FAVORITES -> byQuery.filter { favoriteAppIds.contains(it.id) }
            }
        }

        val TVX_APP = TvApp(
            id = "tvx",
            name = "TVX",
            packageName = "com.tvx.tv",
            dialName = null,
            deepLinkUri = "tvx://",
            isFavorite = false,
            category = "Streaming",
            discoveryState = AppDiscoveryState.USER_CONFIRMED_INSTALLED,
            isUserConfirmedInstalled = true,
            launcherActivity = "com.tvx.tv.MainActivity",
            leanbackLauncherActivity = "com.tvx.tv.MainActivity",
            versionName = "1.0.2",
            versionCode = 5,
            launchStrategy = AppLaunchStrategy.REMOTE_APP_LINK
        )

        val RECTV_APP = TvApp(
            id = "rectv",
            name = "RecTV",
            packageName = "com.rectv.shot",
            dialName = null,
            deepLinkUri = null,
            isFavorite = false,
            category = "Streaming",
            discoveryState = AppDiscoveryState.USER_CONFIRMED_INSTALLED,
            isUserConfirmedInstalled = true,
            launcherActivity = "com.rectv.shot.MainActivity",
            leanbackLauncherActivity = "com.rectv.shot.MainActivity",
            versionName = "22.0.2",
            versionCode = 161,
            launchStrategy = AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH,
            unsupportedLaunchReasonTr = "RecTV (com.rectv.shot v22.0.2) APK dosyası AndroidManifest.xml içinde uzaktan başlatmaya uygun bir Deep Link (ACTION_VIEW / BROWSABLE) şeması veya DIAL uç noktası tanımlamamaktadır. Android TV Remote v2 protokolü yalnızca paket adıyla doğrudan uygulama başlatmayı desteklemez."
        )

        val DEFAULT_CATALOG: List<TvApp> = deduplicateCatalog(
            listOf(
                TvApp(
                    id = "netflix",
                    name = "Netflix",
                    packageName = "com.netflix.ninja",
                    dialName = "Netflix",
                    deepLinkUri = "https://www.netflix.com/title/",
                    isFavorite = true,
                    category = "Streaming"
                ),
                TvApp(
                    id = "youtube",
                    name = "YouTube",
                    packageName = "com.google.android.youtube.tv",
                    dialName = "YouTube",
                    deepLinkUri = "vnd.youtube://",
                    isFavorite = true,
                    category = "Streaming"
                ),
                TvApp(
                    id = "disney_plus",
                    name = "Disney+",
                    packageName = "com.disney.disneyplus",
                    dialName = "DisneyPlus",
                    deepLinkUri = "https://www.disneyplus.com",
                    isFavorite = true,
                    category = "Streaming"
                ),
                TvApp(
                    id = "prime_video",
                    name = "Prime Video",
                    packageName = "com.amazon.amazonvideo.livingroom",
                    dialName = "AmazonInstantVideo",
                    deepLinkUri = "https://app.primevideo.com",
                    isFavorite = true,
                    category = "Streaming"
                ),
                TVX_APP,
                RECTV_APP,
                TvApp(
                    id = "exxen",
                    name = "Exxen",
                    packageName = "com.exxen.android.tv",
                    dialName = "Exxen",
                    deepLinkUri = "https://www.exxen.com",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "blutv",
                    name = "BluTV",
                    packageName = "com.blutv.android.tv",
                    dialName = "BluTV",
                    deepLinkUri = "https://www.blutv.com",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "youtube_music",
                    name = "YouTube Music",
                    packageName = "com.google.android.youtube.tvmusic",
                    dialName = "YouTubeMusic",
                    deepLinkUri = "https://music.youtube.com",
                    isFavorite = false,
                    category = "Müzik"
                ),
                TvApp(
                    id = "tabii",
                    name = "Tabii",
                    packageName = "com.trt.tabii.androidtv",
                    dialName = "tabii",
                    deepLinkUri = "https://www.tabii.com",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "trt_izle",
                    name = "TRT İzle",
                    packageName = "com.trt.trtizle.tv",
                    dialName = null,
                    deepLinkUri = "https://www.tabii.com",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "spotify",
                    name = "Spotify",
                    packageName = "com.spotify.tv.android",
                    dialName = "Spotify",
                    deepLinkUri = "spotify://",
                    isFavorite = false,
                    category = "Müzik"
                ),
                TvApp(
                    id = "plex",
                    name = "Plex",
                    packageName = "com.plexapp.android",
                    dialName = "Plex",
                    deepLinkUri = "plex://",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "kodi",
                    name = "Kodi",
                    packageName = "org.xbmc.kodi",
                    dialName = null,
                    deepLinkUri = "market://details?id=org.xbmc.kodi",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "internet",
                    name = "İnternet",
                    packageName = "com.vewd.core.integration.dia",
                    dialName = null,
                    deepLinkUri = "https://www.google.com",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "file_manager",
                    name = "Dosya Yöneticisi",
                    packageName = "com.android.documentsui",
                    dialName = null,
                    deepLinkUri = "market://search?q=file+manager&c=apps",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "media_player",
                    name = "Medya Oynatıcı",
                    packageName = "org.videolan.vlc",
                    dialName = null,
                    deepLinkUri = "market://details?id=org.videolan.vlc",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "steam_link",
                    name = "Steam Link",
                    packageName = "com.valvesoftware.steamlink",
                    dialName = null,
                    deepLinkUri = "market://details?id=com.valvesoftware.steamlink",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "tv_settings",
                    name = "Ayarlar",
                    packageName = "com.android.tv.settings",
                    dialName = null,
                    deepLinkUri = "https://support.google.com/androidtv",
                    isFavorite = false,
                    category = "Sistem"
                ),
                TvApp(
                    id = "gain",
                    name = "GAİN",
                    packageName = "com.gain.android.tv",
                    dialName = "Gain",
                    deepLinkUri = "https://www.gain.tv",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "tod",
                    name = "TOD / beIN",
                    packageName = "com.digiturk.todtv",
                    dialName = "TOD",
                    deepLinkUri = "https://www.todtv.com.tr",
                    isFavorite = false,
                    category = "Streaming"
                ),
                TvApp(
                    id = "twitch",
                    name = "Twitch",
                    packageName = "tv.twitch.android.app",
                    dialName = "Twitch",
                    deepLinkUri = "twitch://",
                    isFavorite = false,
                    category = "Streaming"
                )
            )
        )
    }
}
