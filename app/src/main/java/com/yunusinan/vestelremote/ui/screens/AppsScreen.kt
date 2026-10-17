package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.AppDiscoveryState
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvApp
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun AppsScreen(
    apps: List<TvApp>,
    favoriteAppIds: Set<String>,
    connectionStatus: ConnectionStatus,
    isAppDiscoveryRunning: Boolean,
    onLaunchApp: (TvApp) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefreshInstalledApps: () -> Unit,
    onLaunchCustomDeepLink: (title: String, uri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCustomDeepLinkDialog by remember { mutableStateOf(false) }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val verifiedOnTvApps = remember(filteredApps) {
        filteredApps.filter { it.isVerifiedOnTv }
    }

    val favorites = remember(filteredApps, favoriteAppIds) {
        filteredApps.filter { favoriteAppIds.contains(it.id) }
    }

    val shortcutApps = remember(filteredApps) {
        filteredApps.filterNot { it.isVerifiedOnTv }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = VestelDesignTokens.Spacing.lg, vertical = VestelDesignTokens.Spacing.sm)
    ) {
        // Screen Header + Action Row
        Text(
            text = stringResource(R.string.apps_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.apps_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = DarkTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRefreshInstalledApps,
                enabled = !isAppDiscoveryRunning && connectionStatus.isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = VestelRed),
                shape = VestelDesignTokens.Radius.buttonShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.weight(1.15f)
            ) {
                if (isAppDiscoveryRunning) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.apps_probing),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.apps_probe_button),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedButton(
                onClick = { showCustomDeepLinkDialog = true },
                shape = VestelDesignTokens.Radius.buttonShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.weight(0.95f)
            ) {
                Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp), tint = VestelRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.apps_custom_launch),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Uygulama veya kategori ara…", color = DarkTextMuted, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VestelRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Protocol Transparency Banner
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = StatusBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.apps_protocol_note),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                        color = DarkTextSecondary
                    )
                }
            }

            // Section 1: Verified on TV via DIAL (only shown when at least 1 app is verified on the connected TV)
            if (verifiedOnTvApps.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeaderRow(
                        title = stringResource(R.string.apps_verified_section),
                        badgeText = "${verifiedOnTvApps.size} Doğrulandı",
                        badgeColor = StatusGreen
                    )
                }

                items(verifiedOnTvApps, key = { "verified-${it.id}" }) { app ->
                    AppCardItem(
                        app = app,
                        isFavorite = favoriteAppIds.contains(app.id),
                        onLaunch = { onLaunchApp(app) },
                        onToggleFavorite = { onToggleFavorite(app.id) }
                    )
                }
            }

            // Section 2: Favorite Apps
            if (favorites.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeaderRow(
                        title = stringResource(R.string.favorite_apps),
                        badgeText = "${favorites.size} Favori",
                        badgeColor = StatusYellow
                    )
                }

                items(favorites, key = { "fav-${it.id}" }) { app ->
                    AppCardItem(
                        app = app,
                        isFavorite = true,
                        onLaunch = { onLaunchApp(app) },
                        onToggleFavorite = { onToggleFavorite(app.id) }
                    )
                }
            }

            // Section 3: Quick Launch Shortcuts (Deep Link / Catalog)
            if (shortcutApps.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeaderRow(
                        title = stringResource(R.string.apps_shortcuts_section),
                        badgeText = "${shortcutApps.size} Kısayol",
                        badgeColor = DarkTextSecondary
                    )
                }

                items(shortcutApps, key = { "shortcut-${it.id}" }) { app ->
                    AppCardItem(
                        app = app,
                        isFavorite = favoriteAppIds.contains(app.id),
                        onLaunch = { onLaunchApp(app) },
                        onToggleFavorite = { onToggleFavorite(app.id) }
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showCustomDeepLinkDialog) {
        CustomDeepLinkDialog(
            onDismiss = { showCustomDeepLinkDialog = false },
            onLaunch = { title, uri ->
                showCustomDeepLinkDialog = false
                onLaunchCustomDeepLink(title, uri)
            }
        )
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    badgeText: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = badgeColor
            )
        }
    }
}

@Composable
private fun AppCardItem(
    app: TvApp,
    isFavorite: Boolean,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val accentColor = getAppAccentColor(app.id)
    val isVerified = app.isVerifiedOnTv
    val isRunning = app.discoveryState == AppDiscoveryState.VERIFIED_RUNNING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = when {
                    isRunning -> StatusGreen.copy(alpha = 0.45f)
                    isVerified -> StatusBlue.copy(alpha = 0.3f)
                    else -> DarkBorder
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onLaunch),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = app.name,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.favorite_apps),
                        tint = if (isFavorite) StatusYellow else DarkTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val (statusLabel, statusColor) = when (app.discoveryState) {
                AppDiscoveryState.VERIFIED_RUNNING -> Pair("TV'de Açık • DIAL", StatusGreen)
                AppDiscoveryState.VERIFIED_STOPPED -> Pair("TV'de Yüklü • DIAL", StatusGreen)
                AppDiscoveryState.DIAL_NOT_REGISTERED -> Pair("Deep Link Kısayolu", DarkTextSecondary)
                AppDiscoveryState.UNVERIFIED_SHORTCUT -> Pair("${app.category} • Kısayol", DarkTextSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = statusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CustomDeepLinkDialog(
    onDismiss: () -> Unit,
    onLaunch: (title: String, uri: String) -> Unit
) {
    var appTitle by remember { mutableStateOf("") }
    var deepLinkUri by remember { mutableStateOf("market://details?id=") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Özel Uygulama / Deep Link Başlat",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Android TV Remote v2 (Field 90) üzerinden televizyonda yüklü herhangi bir uygulamanın URI bağlantısını veya Google Play sayfasını doğrudan açabilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
                OutlinedTextField(
                    value = appTitle,
                    onValueChange = { appTitle = it },
                    label = { Text("Uygulama Adı (İsteğe Bağlı)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deepLinkUri,
                    onValueChange = { deepLinkUri = it },
                    label = { Text("Deep Link URI (örn. vnd.youtube://)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLaunch(appTitle.ifBlank { "Özel Uygulama" }, deepLinkUri) },
                enabled = deepLinkUri.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VestelRed)
            ) {
                Text("TV'de Başlat", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = DarkTextSecondary)
            }
        }
    )
}

private fun getAppAccentColor(id: String): Color {
    return when (id) {
        "youtube" -> Color(0xFFFF0000)
        "netflix" -> Color(0xFFE50914)
        "prime_video" -> Color(0xFF00A8E1)
        "disney_plus" -> Color(0xFF113CCF)
        "spotify" -> Color(0xFF1DB954)
        "tabii" -> Color(0xFF00D18B)
        "exxen" -> Color(0xFFFFE600)
        "blutv" -> Color(0xFF00C3FF)
        "gain" -> Color(0xFFFF0055)
        "tod" -> Color(0xFF6B21A8)
        "twitch" -> Color(0xFF9146FF)
        "youtube_music" -> Color(0xFFFF004F)
        else -> VestelRed
    }
}
