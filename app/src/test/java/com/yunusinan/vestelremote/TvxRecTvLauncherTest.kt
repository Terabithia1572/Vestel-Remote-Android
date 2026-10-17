package com.yunusinan.vestelremote

import com.yunusinan.vestelremote.data.protocol.AppLauncherEngine
import com.yunusinan.vestelremote.data.protocol.DialClient
import com.yunusinan.vestelremote.domain.model.AppCatalogFilter
import com.yunusinan.vestelremote.domain.model.AppDiscoveryState
import com.yunusinan.vestelremote.domain.model.AppLaunchDispatchStatus
import com.yunusinan.vestelremote.domain.model.AppLaunchStrategy
import com.yunusinan.vestelremote.domain.model.TvApp
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class TvxRecTvLauncherTest {

    @Test
    fun `TVX is registered in default catalog with verified APK metadata and user-confirmed installed state`() {
        val tvx = TvApp.DEFAULT_CATALOG.firstOrNull { it.id == "tvx" }
        assertNotNull("TVX must be present in DEFAULT_CATALOG", tvx)
        requireNotNull(tvx)

        assertEquals("TVX", tvx.name)
        assertEquals("com.tvx.tv", tvx.packageName)
        assertEquals("com.tvx.tv.MainActivity", tvx.launcherActivity)
        assertEquals("com.tvx.tv.MainActivity", tvx.leanbackLauncherActivity)
        assertEquals("1.0.2", tvx.versionName)
        assertEquals(5, tvx.versionCode)
        assertEquals("tvx://", tvx.deepLinkUri)
        assertNull(tvx.dialName)
        assertEquals(AppDiscoveryState.USER_CONFIRMED_INSTALLED, tvx.discoveryState)
        assertTrue(tvx.isUserConfirmedInstalled)
        assertTrue(tvx.isInstalledOnTv)
        assertFalse("TVX must not claim independent DIAL verification", tvx.isVerifiedOnTv)
        assertEquals("User-confirmed installed", tvx.installationStatusLabel)
        assertEquals(AppLaunchStrategy.REMOTE_APP_LINK, tvx.launchStrategy)
        assertTrue(tvx.supportsDirectLaunch)
    }

    @Test
    fun `RecTV is registered in default catalog with verified APK metadata and user-confirmed installed state`() {
        val recTv = TvApp.DEFAULT_CATALOG.firstOrNull { it.id == "rectv" }
        assertNotNull("RecTV must be present in DEFAULT_CATALOG", recTv)
        requireNotNull(recTv)

        assertEquals("RecTV", recTv.name)
        assertEquals("com.rectv.shot", recTv.packageName)
        assertEquals("com.rectv.shot.MainActivity", recTv.launcherActivity)
        assertEquals("com.rectv.shot.MainActivity", recTv.leanbackLauncherActivity)
        assertEquals("22.0.2", recTv.versionName)
        assertEquals(161, recTv.versionCode)
        assertNull("RecTV APK does not declare a BROWSABLE VIEW deep link", recTv.deepLinkUri)
        assertNull(recTv.dialName)
        assertEquals(AppDiscoveryState.USER_CONFIRMED_INSTALLED, recTv.discoveryState)
        assertTrue(recTv.isUserConfirmedInstalled)
        assertTrue(recTv.isInstalledOnTv)
        assertFalse("RecTV must not claim independent DIAL verification", recTv.isVerifiedOnTv)
        assertEquals("User-confirmed installed", recTv.installationStatusLabel)
        assertEquals(AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH, recTv.launchStrategy)
        assertFalse(recTv.supportsDirectLaunch)
    }

    @Test
    fun `search finds TVX and RecTV by display name, package name, and launcher activity`() {
        val byTvxName = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "TVX",
            filterTab = AppCatalogFilter.ALL,
            favoriteAppIds = emptySet()
        )
        assertEquals(listOf("tvx"), byTvxName.map { it.id })

        val byTvxPackage = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "com.tvx.tv",
            filterTab = AppCatalogFilter.ALL,
            favoriteAppIds = emptySet()
        )
        assertEquals(listOf("tvx"), byTvxPackage.map { it.id })

        val byRecTvName = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "rectv",
            filterTab = AppCatalogFilter.ALL,
            favoriteAppIds = emptySet()
        )
        assertEquals(listOf("rectv"), byRecTvName.map { it.id })

        val byRecTvPackage = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "com.rectv.shot",
            filterTab = AppCatalogFilter.ALL,
            favoriteAppIds = emptySet()
        )
        assertEquals(listOf("rectv"), byRecTvPackage.map { it.id })
    }

    @Test
    fun `favorites filter includes TVX and RecTV when favorited`() {
        val favIds = setOf("tvx", "rectv")
        val favorites = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "",
            filterTab = AppCatalogFilter.FAVORITES,
            favoriteAppIds = favIds
        )
        assertEquals(setOf("tvx", "rectv"), favorites.map { it.id }.toSet())
    }

    @Test
    fun `installed filter keeps user-confirmed installed apps even when DIAL returns 0 verified apps`() {
        val installed = TvApp.filterCatalog(
            apps = TvApp.DEFAULT_CATALOG,
            searchQuery = "",
            filterTab = AppCatalogFilter.INSTALLED,
            favoriteAppIds = emptySet()
        )
        val installedIds = installed.map { it.id }.toSet()
        assertTrue("TVX must remain visible in INSTALLED filter", installedIds.contains("tvx"))
        assertTrue("RecTV must remain visible in INSTALLED filter", installedIds.contains("rectv"))

        // Also verify DialClient.probeApp preserves USER_CONFIRMED_INSTALLED when dialName is null
        runTest {
            val dialClient = DialClient("127.0.0.1")
            val tvxProbe = dialClient.probeApp(TvApp.TVX_APP)
            val recTvProbe = dialClient.probeApp(TvApp.RECTV_APP)
            assertEquals(AppDiscoveryState.USER_CONFIRMED_INSTALLED, tvxProbe.state)
            assertEquals(AppDiscoveryState.USER_CONFIRMED_INSTALLED, recTvProbe.state)
            assertFalse(tvxProbe.isVerifiedOnTv)
            assertFalse(recTvProbe.isVerifiedOnTv)
        }
    }

    @Test
    fun `duplicate prevention ensures unique app IDs and package names in catalog`() {
        val ids = TvApp.DEFAULT_CATALOG.map { it.id.lowercase() }
        val packages = TvApp.DEFAULT_CATALOG.map { it.packageName.lowercase() }
        assertEquals("Catalog IDs must be unique", ids.size, ids.distinct().size)
        assertEquals("Catalog package names must be unique", packages.size, packages.distinct().size)

        val duplicatedList = listOf(
            TvApp.TVX_APP,
            TvApp.TVX_APP.copy(name = "TVX Duplicate ID"),
            TvApp.TVX_APP.copy(id = "tvx_2", name = "TVX Duplicate Package"),
            TvApp.RECTV_APP,
            TvApp.RECTV_APP.copy(id = "rectv_dup")
        )
        val deduplicated = TvApp.deduplicateCatalog(duplicatedList)
        assertEquals(2, deduplicated.size)
        assertEquals(listOf("tvx", "rectv"), deduplicated.map { it.id })
    }

    @Test
    fun `URI validation rejects bare package names and accepts valid URI schemes`() {
        assertTrue(TvApp.isValidAppLinkUri("tvx://"))
        assertTrue(TvApp.isValidAppLinkUri("vnd.youtube://"))
        assertTrue(TvApp.isValidAppLinkUri("https://www.netflix.com/title/"))
        assertFalse("Bare package name is not a valid app-link URI", TvApp.isValidAppLinkUri("com.tvx.tv"))
        assertFalse("Bare package name is not a valid app-link URI", TvApp.isValidAppLinkUri("com.rectv.shot"))
        assertFalse(TvApp.isValidAppLinkUri(""))
        assertFalse(TvApp.isValidAppLinkUri(null))
    }

    @Test
    fun `launch strategy for TVX dispatches RemoteAppLinkLaunchRequest and records COMMAND_SENT`() = runTest {
        val dispatchedUris = mutableListOf<String>()
        val diagnostic = AppLauncherEngine.executeLaunch(
            app = TvApp.TVX_APP,
            isMockMode = false,
            isRemoteConnected = true,
            sendRemoteAppLink = { uri ->
                dispatchedUris.add(uri)
                Result.success(Unit)
            }
        )

        assertEquals(listOf("tvx://"), dispatchedUris)
        assertEquals("TVX", diagnostic.appDisplayName)
        assertEquals("com.tvx.tv", diagnostic.packageName)
        assertEquals(AppLaunchStrategy.REMOTE_APP_LINK, diagnostic.selectedStrategy)
        assertEquals("tvx://", diagnostic.attemptedSchemeOrEndpoint)
        assertEquals(
            "RemoteAppLinkLaunchRequest must record COMMAND_SENT, not fake APPLICATION_VISIBLY_OPENED",
            AppLaunchDispatchStatus.COMMAND_SENT,
            diagnostic.dispatchResult
        )
        assertNull(diagnostic.timeoutOrErrorReason)
        assertNull(diagnostic.unsupportedCapabilityReason)
    }

    @Test
    fun `unsupported launch for RecTV does not send fake command and returns Turkish explanation`() = runTest {
        var remoteCalled = false
        var dialCalled = false
        val diagnostic = AppLauncherEngine.executeLaunch(
            app = TvApp.RECTV_APP,
            isMockMode = false,
            isRemoteConnected = true,
            sendRemoteAppLink = {
                remoteCalled = true
                Result.success(Unit)
            },
            launchViaDial = {
                dialCalled = true
                Result.success(true)
            }
        )

        assertFalse("Must not send remote app-link for unsupported app", remoteCalled)
        assertFalse("Must not send DIAL request for unsupported app", dialCalled)
        assertEquals("RecTV", diagnostic.appDisplayName)
        assertEquals("com.rectv.shot", diagnostic.packageName)
        assertEquals(AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH, diagnostic.selectedStrategy)
        assertEquals("none", diagnostic.attemptedSchemeOrEndpoint)
        assertEquals(AppLaunchDispatchStatus.UNSUPPORTED_CAPABILITY, diagnostic.dispatchResult)
        assertNotNull(diagnostic.unsupportedCapabilityReason)
        assertTrue(diagnostic.userFeedbackTr.contains("RecTV"))
        assertTrue(diagnostic.userFeedbackTr.contains("Deep Link"))
    }

    @Test
    fun `error handling records sanitized timeout or connection failure and strips URI secrets`() = runTest {
        val disconnectedDiag = AppLauncherEngine.executeLaunch(
            app = TvApp.TVX_APP,
            isMockMode = false,
            isRemoteConnected = false,
            sendRemoteAppLink = { Result.success(Unit) }
        )
        assertEquals(AppLaunchDispatchStatus.DISPATCH_FAILED, disconnectedDiag.dispatchResult)
        assertTrue(disconnectedDiag.userFeedbackTr.contains("Televizyona bağlı değil"))

        val secretUriApp = TvApp.TVX_APP.copy(
            deepLinkUri = "tvx://watch/stream?token=SUPER_SECRET_TOKEN_123&pin=998877#frag"
        )
        val timeoutDiag = AppLauncherEngine.executeLaunch(
            app = secretUriApp,
            isMockMode = false,
            isRemoteConnected = true,
            sendRemoteAppLink = { Result.failure(SocketTimeoutException("secret_host_timeout")) }
        )
        assertEquals(AppLaunchDispatchStatus.DISPATCH_FAILED, timeoutDiag.dispatchResult)
        assertEquals("tvx://watch", timeoutDiag.attemptedSchemeOrEndpoint)
        val exported = timeoutDiag.export()
        assertFalse("Sanitized diagnostics must never leak query tokens", exported.contains("SUPER_SECRET_TOKEN_123"))
        assertFalse("Sanitized diagnostics must never leak PIN parameters", exported.contains("998877"))
        assertFalse("Sanitized diagnostics must never leak raw exception text", exported.contains("secret_host_timeout"))
    }
}
