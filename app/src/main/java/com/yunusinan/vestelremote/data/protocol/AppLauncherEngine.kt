package com.yunusinan.vestelremote.data.protocol

import com.yunusinan.vestelremote.domain.model.AppLaunchDiagnostic
import com.yunusinan.vestelremote.domain.model.AppLaunchDispatchStatus
import com.yunusinan.vestelremote.domain.model.AppLaunchStrategy
import com.yunusinan.vestelremote.domain.model.TvApp

class UnsupportedAppLaunchException(
    val diagnostic: AppLaunchDiagnostic
) : Exception(diagnostic.userFeedbackTr)

class AppLaunchDispatchException(
    val diagnostic: AppLaunchDiagnostic,
    cause: Throwable? = null
) : Exception(diagnostic.userFeedbackTr, cause)

object AppLauncherEngine {

    /**
     * Executes the remote launch flow for [app] according to the verified strategy priority:
     * 1. Verified Android TV Remote v2 App Link (`RemoteAppLinkLaunchRequest`, Field 90)
     * 2. Supported DIAL REST endpoint (`Port 8008`)
     * 3. Explicit unsupported capability explanation (when neither deep link nor DIAL exists)
     */
    suspend fun executeLaunch(
        app: TvApp,
        isMockMode: Boolean,
        isRemoteConnected: Boolean,
        sendRemoteAppLink: suspend (uri: String) -> Result<Unit>,
        launchViaDial: (suspend (dialName: String) -> Result<Boolean>)? = null,
        verifyDialRunning: (suspend (dialName: String) -> Result<Boolean>)? = null,
        launchViaMock: (suspend (TvApp) -> Result<Boolean>)? = null
    ): AppLaunchDiagnostic {
        // 1. Check if direct launch is unsupported for this app (e.g., RecTV com.rectv.shot)
        if (app.launchStrategy == AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH || !app.supportsDirectLaunch) {
            val reason = app.unsupportedLaunchReasonTr
                ?: "${app.name} (${app.packageName}) APK'sı uzaktan başlatmaya uygun bir Deep Link (ACTION_VIEW / BROWSABLE) şeması veya DIAL uç noktası tanımlamamaktadır."
            val userMsg = "${app.name} (${app.packageName}) doğrudan uzaktan başlatmayı desteklemiyor: APK içinde BROWSABLE Deep Link şeması veya DIAL uç noktası bulunmuyor. TV Ana Ekranını (HOME) açarak uygulamayı seçebilirsiniz."
            return AppLaunchDiagnostic(
                appDisplayName = app.name,
                packageName = app.packageName,
                selectedStrategy = AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH,
                attemptedSchemeOrEndpoint = "none",
                dispatchResult = AppLaunchDispatchStatus.UNSUPPORTED_CAPABILITY,
                protocolAcknowledgment = "Yok (Protokol isteği gönderilmedi)",
                timeoutOrErrorReason = null,
                unsupportedCapabilityReason = reason,
                userFeedbackTr = userMsg
            )
        }

        // 2. Virtual TV Mock Mode handling
        if (isMockMode && launchViaMock != null) {
            val mockRes = launchViaMock(app)
            val endpoint = when (app.launchStrategy) {
                AppLaunchStrategy.DIAL_ONLY,
                AppLaunchStrategy.DIAL_WITH_APP_LINK_FALLBACK ->
                    if (!app.dialName.isNullOrBlank()) "dial:/apps/${app.dialName}"
                    else TvApp.sanitizeEndpointForDiagnostics(app.deepLinkUri)
                AppLaunchStrategy.REMOTE_APP_LINK ->
                    TvApp.sanitizeEndpointForDiagnostics(app.deepLinkUri)
                AppLaunchStrategy.UNSUPPORTED_DIRECT_LAUNCH -> "none"
            }
            return if (mockRes.isSuccess) {
                AppLaunchDiagnostic(
                    appDisplayName = app.name,
                    packageName = app.packageName,
                    selectedStrategy = app.launchStrategy,
                    attemptedSchemeOrEndpoint = endpoint,
                    dispatchResult = AppLaunchDispatchStatus.APPLICATION_VISIBLY_OPENED,
                    protocolAcknowledgment = "Sanal TV (Mock) uygulama açılışını onayladı",
                    userFeedbackTr = "${app.name} sanal TV üzerinde açıldı."
                )
            } else {
                val err = safeConnectionError(mockRes.exceptionOrNull() ?: Exception())
                AppLaunchDiagnostic(
                    appDisplayName = app.name,
                    packageName = app.packageName,
                    selectedStrategy = app.launchStrategy,
                    attemptedSchemeOrEndpoint = endpoint,
                    dispatchResult = AppLaunchDispatchStatus.DISPATCH_FAILED,
                    protocolAcknowledgment = "Yok",
                    timeoutOrErrorReason = err,
                    userFeedbackTr = "${app.name} başlatılamadı: $err"
                )
            }
        }

        // 3. Try DIAL first when strategy uses DIAL (Strategy 2)
        if ((app.launchStrategy == AppLaunchStrategy.DIAL_ONLY ||
                app.launchStrategy == AppLaunchStrategy.DIAL_WITH_APP_LINK_FALLBACK) &&
            !app.dialName.isNullOrBlank() && launchViaDial != null
        ) {
            val dialEndpoint = "dial:/apps/${app.dialName}"
            val dialResult = launchViaDial(app.dialName)
            if (dialResult.isSuccess && dialResult.getOrDefault(false)) {
                val isVisiblyRunning = verifyDialRunning?.invoke(app.dialName)?.getOrDefault(false) == true
                return if (isVisiblyRunning) {
                    AppLaunchDiagnostic(
                        appDisplayName = app.name,
                        packageName = app.packageName,
                        selectedStrategy = app.launchStrategy,
                        attemptedSchemeOrEndpoint = dialEndpoint,
                        dispatchResult = AppLaunchDispatchStatus.APPLICATION_VISIBLY_OPENED,
                        protocolAcknowledgment = "DIAL HTTP 2xx + <state>running</state> doğrulandı",
                        userFeedbackTr = "${app.name} televizyonda açıldı (DIAL ile doğrulandı)."
                    )
                } else {
                    AppLaunchDiagnostic(
                        appDisplayName = app.name,
                        packageName = app.packageName,
                        selectedStrategy = app.launchStrategy,
                        attemptedSchemeOrEndpoint = dialEndpoint,
                        dispatchResult = AppLaunchDispatchStatus.LAUNCH_ACCEPTED,
                        protocolAcknowledgment = "DIAL HTTP 2xx/303 başlatma isteğini kabul etti",
                        userFeedbackTr = "${app.name} başlatma isteği TV tarafından kabul edildi (DIAL)."
                    )
                }
            } else if (app.launchStrategy == AppLaunchStrategy.DIAL_ONLY) {
                val err = dialResult.exceptionOrNull()?.let { safeConnectionError(it) }
                    ?: "DIAL sunucusu başlatma isteğini reddetti veya uygulama kayıtlı değil"
                return AppLaunchDiagnostic(
                    appDisplayName = app.name,
                    packageName = app.packageName,
                    selectedStrategy = AppLaunchStrategy.DIAL_ONLY,
                    attemptedSchemeOrEndpoint = dialEndpoint,
                    dispatchResult = AppLaunchDispatchStatus.DISPATCH_FAILED,
                    protocolAcknowledgment = "DIAL HTTP 4xx/5xx veya bağlantı hatası",
                    timeoutOrErrorReason = err,
                    userFeedbackTr = "${app.name} DIAL üzerinden başlatılamadı: $err"
                )
            }
        }

        // 4. Strategy 1: Verified Android TV Remote v2 App Link (`RemoteAppLinkLaunchRequest`, Field 90)
        val rawUri = app.deepLinkUri?.trim()
        if (!TvApp.isValidAppLinkUri(rawUri)) {
            val reason = "Geçerli bir App Link URI şeması bulunamadı; paket adı (${app.packageName}) tek başına geçerli bir URI değildir."
            return AppLaunchDiagnostic(
                appDisplayName = app.name,
                packageName = app.packageName,
                selectedStrategy = AppLaunchStrategy.REMOTE_APP_LINK,
                attemptedSchemeOrEndpoint = TvApp.sanitizeEndpointForDiagnostics(rawUri),
                dispatchResult = AppLaunchDispatchStatus.UNSUPPORTED_CAPABILITY,
                protocolAcknowledgment = "Yok (Geçersiz URI şeması engellendi)",
                unsupportedCapabilityReason = reason,
                userFeedbackTr = "${app.name} başlatılamadı: $reason"
            )
        }

        val sanitizedScheme = TvApp.sanitizeEndpointForDiagnostics(rawUri)
        if (!isRemoteConnected) {
            val errReason = "Televizyona aktif Android TV Remote v2 (Port 6466) bağlantısı yok"
            return AppLaunchDiagnostic(
                appDisplayName = app.name,
                packageName = app.packageName,
                selectedStrategy = AppLaunchStrategy.REMOTE_APP_LINK,
                attemptedSchemeOrEndpoint = sanitizedScheme,
                dispatchResult = AppLaunchDispatchStatus.DISPATCH_FAILED,
                protocolAcknowledgment = "Yok",
                timeoutOrErrorReason = errReason,
                userFeedbackTr = "${app.name} başlatılamadı: Televizyona bağlı değil."
            )
        }

        val sendResult = sendRemoteAppLink(rawUri!!)
        return if (sendResult.isSuccess) {
            AppLaunchDiagnostic(
                appDisplayName = app.name,
                packageName = app.packageName,
                selectedStrategy = AppLaunchStrategy.REMOTE_APP_LINK,
                attemptedSchemeOrEndpoint = sanitizedScheme,
                dispatchResult = AppLaunchDispatchStatus.COMMAND_SENT,
                protocolAcknowledgment = "RemoteAppLinkLaunchRequest (Field 90) TLS soketine iletildi; protokol uygulama açılış ACK çerçevesi döndürmez",
                userFeedbackTr = "${app.name} başlatma komutu gönderildi ($sanitizedScheme)."
            )
        } else {
            val err = safeConnectionError(sendResult.exceptionOrNull() ?: Exception())
            AppLaunchDiagnostic(
                appDisplayName = app.name,
                packageName = app.packageName,
                selectedStrategy = AppLaunchStrategy.REMOTE_APP_LINK,
                attemptedSchemeOrEndpoint = sanitizedScheme,
                dispatchResult = AppLaunchDispatchStatus.DISPATCH_FAILED,
                protocolAcknowledgment = "Yok (TLS yazma hatası)",
                timeoutOrErrorReason = err,
                userFeedbackTr = "${app.name} başlatma komutu gönderilemedi: $err"
            )
        }
    }
}
