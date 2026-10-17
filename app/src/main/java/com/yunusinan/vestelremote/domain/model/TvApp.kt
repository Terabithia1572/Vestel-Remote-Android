package com.yunusinan.vestelremote.domain.model

enum class AppDiscoveryState {
    /** App has not been probed yet or only uses Deep Link shortcut without DIAL verification. */
    UNVERIFIED_SHORTCUT,
    /** Verified present on TV via DIAL HTTP 200 (<state>stopped</state>). */
    VERIFIED_STOPPED,
    /** Verified currently running on TV via DIAL HTTP 200 (<state>running</state>). */
    VERIFIED_RUNNING,
    /** Probed via DIAL HTTP 404; not registered on DIAL server (still usable as Deep Link shortcut). */
    DIAL_NOT_REGISTERED
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
    val discoveryState: AppDiscoveryState = AppDiscoveryState.UNVERIFIED_SHORTCUT
) {
    val isVerifiedOnTv: Boolean
        get() = discoveryState == AppDiscoveryState.VERIFIED_STOPPED ||
            discoveryState == AppDiscoveryState.VERIFIED_RUNNING

    companion object {
        val DEFAULT_CATALOG = listOf(
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
                id = "spotify_tv",
                name = "Spotify TV",
                packageName = "com.spotify.tv.android",
                dialName = "Spotify",
                deepLinkUri = "https://open.spotify.com",
                isFavorite = false,
                category = "Müzik"
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
    }
}

