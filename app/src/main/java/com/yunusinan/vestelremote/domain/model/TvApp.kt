package com.yunusinan.vestelremote.domain.model

data class TvApp(
    val id: String,
    val name: String,
    val packageName: String,
    val dialName: String? = null,
    val deepLinkUri: String? = null,
    val isFavorite: Boolean = false,
    val category: String = "Streaming"
) {
    companion object {
        val DEFAULT_CATALOG = listOf(
            TvApp(
                id = "youtube",
                name = "YouTube",
                packageName = "com.google.android.youtube.tv",
                dialName = "YouTube",
                deepLinkUri = "vnd.youtube://",
                isFavorite = true
            ),
            TvApp(
                id = "netflix",
                name = "Netflix",
                packageName = "com.netflix.ninja",
                dialName = "Netflix",
                deepLinkUri = "https://www.netflix.com/title/",
                isFavorite = true
            ),
            TvApp(
                id = "prime_video",
                name = "Prime Video",
                packageName = "com.amazon.amazonvideo.livingroom",
                dialName = "AmazonInstantVideo",
                deepLinkUri = "https://app.primevideo.com",
                isFavorite = true
            ),
            TvApp(
                id = "disney_plus",
                name = "Disney+",
                packageName = "com.disney.disneyplus",
                dialName = "DisneyPlus",
                deepLinkUri = "https://www.disneyplus.com",
                isFavorite = true
            ),
            TvApp(
                id = "spotify",
                name = "Spotify",
                packageName = "com.spotify.tv.android",
                dialName = "Spotify",
                deepLinkUri = "spotify://",
                isFavorite = false
            ),
            TvApp(
                id = "exxen",
                name = "Exxen",
                packageName = "com.exxen.android.tv",
                dialName = "Exxen",
                deepLinkUri = "https://www.exxen.com",
                isFavorite = false
            ),
            TvApp(
                id = "blutv",
                name = "BluTV",
                packageName = "com.blutv.android.tv",
                dialName = "BluTV",
                deepLinkUri = "https://www.blutv.com",
                isFavorite = false
            ),
            TvApp(
                id = "gain",
                name = "Gain",
                packageName = "com.gain.android.tv",
                dialName = "Gain",
                deepLinkUri = "https://www.gain.tv",
                isFavorite = false
            ),
            TvApp(
                id = "tod",
                name = "TOD / beIN",
                packageName = "com.digiturk.todtv",
                dialName = "TOD",
                deepLinkUri = "https://www.todtv.com.tr",
                isFavorite = false
            ),
            TvApp(
                id = "twitch",
                name = "Twitch",
                packageName = "tv.twitch.android.app",
                dialName = "Twitch",
                deepLinkUri = "twitch://",
                isFavorite = false
            )
        )
    }
}
