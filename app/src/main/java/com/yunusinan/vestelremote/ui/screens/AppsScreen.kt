package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.AppDiscoveryState
import com.yunusinan.vestelremote.domain.model.ConnectionStatus
import com.yunusinan.vestelremote.domain.model.TvApp
import com.yunusinan.vestelremote.ui.theme.*

private enum class AppFilterTab {
    ALL,
    INSTALLED,
    SYSTEM,
    FAVORITES
}

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
    var selectedFilter by remember { mutableStateOf(AppFilterTab.ALL) }
    var showCustomDeepLinkDialog by remember { mutableStateOf(false) }

    val displayedApps = remember(apps, searchQuery, selectedFilter, favoriteAppIds) {
        val byQuery = if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedFilter) {
            AppFilterTab.ALL -> byQuery
            AppFilterTab.INSTALLED -> {
                val verified = byQuery.filter { it.isVerifiedOnTv }
                if (verified.isNotEmpty()) verified else byQuery.filter { it.dialName != null }
            }
            AppFilterTab.SYSTEM -> byQuery.filter { it.category.equals("Sistem", ignoreCase = true) }
            AppFilterTab.FAVORITES -> byQuery.filter { favoriteAppIds.contains(it.id) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VestelDesignTokens.Gradients.screenBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Screen Header: "Uygulamalar"
        Text(
            text = stringResource(R.string.nav_apps),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Box + "Yenile" Red Button Row (Matching Reference)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact Search Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkControlBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Uygulama ara...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = DarkTextSecondary,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(VestelRed),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // "Yenile" Red Button
            Button(
                onClick = onRefreshInstalledApps,
                enabled = !isAppDiscoveryRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VestelRed,
                    disabledContainerColor = VestelRed.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(42.dp)
            ) {
                if (isAppDiscoveryRunning) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Yenile",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills Row: "Tümü", "Yüklüler", "Sistem", "Favoriler", "+ Özel URI"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterPill(
                label = "Tümü",
                selected = selectedFilter == AppFilterTab.ALL,
                onClick = { selectedFilter = AppFilterTab.ALL }
            )
            FilterPill(
                label = "Yüklüler",
                selected = selectedFilter == AppFilterTab.INSTALLED,
                onClick = { selectedFilter = AppFilterTab.INSTALLED }
            )
            FilterPill(
                label = "Sistem",
                selected = selectedFilter == AppFilterTab.SYSTEM,
                onClick = { selectedFilter = AppFilterTab.SYSTEM }
            )
            FilterPill(
                label = "Favoriler",
                selected = selectedFilter == AppFilterTab.FAVORITES,
                onClick = { selectedFilter = AppFilterTab.FAVORITES }
            )
            FilterPill(
                label = "+ Özel URI",
                selected = false,
                onClick = { showCustomDeepLinkDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3-Column Branded App Grid (Matching Reference)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedApps, key = { it.id }) { app ->
                AppGridTile(
                    app = app,
                    isFavorite = favoriteAppIds.contains(app.id),
                    showFavoriteBadge = selectedFilter == AppFilterTab.FAVORITES,
                    onLaunch = { onLaunchApp(app) },
                    onToggleFavorite = { onToggleFavorite(app.id) }
                )
            }

            // Compact Protocol Transparency Note at Bottom of Grid
            item(span = { GridItemSpan(3) }) {
                val verifiedCount = apps.count { it.isVerifiedOnTv }
                val noteText = if (verifiedCount > 0) {
                    "Yeşil işaretli $verifiedCount uygulama TV üzerinde DIAL (Port 8008) ile doğrulandı. Diğer kısayollar Deep Link ile başlatılır."
                } else {
                    "DIAL (Port 8008) destekli uygulamaları TV'de doğrulamak için 'Yenile' butonunu kullanın."
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        ),
                        color = DarkTextSecondary
                    )
                }
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
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) VestelRed else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (selected) VestelRed else DarkControlBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Color.White else DarkTextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun AppGridTile(
    app: TvApp,
    isFavorite: Boolean,
    showFavoriteBadge: Boolean,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isVerified = app.isVerifiedOnTv
    val isRunning = app.discoveryState == AppDiscoveryState.VERIFIED_RUNNING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = when {
                    isRunning -> StatusGreen.copy(alpha = 0.55f)
                    isVerified -> StatusGreen.copy(alpha = 0.35f)
                    else -> DarkBorder
                },
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onLaunch)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle DIAL verification dot in top-left corner when verified on TV
        if (isVerified) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusGreen)
            )
        }

        // Top-right corner tap target for favorite toggling
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleFavorite),
            contentAlignment = Alignment.Center
        ) {
            if (showFavoriteBadge) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.favorite_apps),
                    tint = if (isFavorite) StatusYellow.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandedAppIcon(appId = app.id, appName = app.name)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 12.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BrandedAppIcon(appId: String, appName: String) {
    when (appId) {
        "netflix" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0A0D))
                    .border(0.5.dp, Color(0xFF2B2D38), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color(0xFFE50914)
                )
            }
        }
        "youtube" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1014)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = appName,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        "disney_plus" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A1945), Color(0xFF163888))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Disney+",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )
            }
        }
        "prime_video" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00A8E1)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "prime",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 9.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "video",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 9.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
        "exxen" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFD600)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EXXEN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color(0xFF111111)
                )
            }
        }
        "blutv" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F265C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "blutv",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )
            }
        }
        "youtube_music" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF0033)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = appName,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        "tabii" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0E2923)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = appName,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        "trt_izle" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10131C))
                    .border(0.5.dp, Color(0xFF2D3344), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TRT İZLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
            }
        }
        "spotify", "spotify_tv" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF102419)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DB954)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(
                            color = Color(0xFF101418),
                            startAngle = 205f,
                            sweepAngle = 130f,
                            useCenter = false,
                            topLeft = Offset(0f, 2.dp.toPx()),
                            size = Size(size.width, size.height),
                            style = stroke
                        )
                        drawArc(
                            color = Color(0xFF101418),
                            startAngle = 210f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(2.dp.toPx(), 6.dp.toPx()),
                            size = Size(size.width - 4.dp.toPx(), size.height),
                            style = stroke
                        )
                        drawArc(
                            color = Color(0xFF101418),
                            startAngle = 215f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(4.dp.toPx(), 10.dp.toPx()),
                            size = Size(size.width - 8.dp.toPx(), size.height),
                            style = stroke
                        )
                    }
                }
            }
        }
        "plex" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14161E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = appName,
                    tint = Color(0xFFE5A00D),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        "kodi" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF112236)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = appName,
                    tint = Color(0xFF17B2E7),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        "internet" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1967D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        "file_manager" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF282214)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = appName,
                    tint = Color(0xFFFBC02D),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        "media_player" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5E35B1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        "steam_link" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF15437E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        "tv_settings" -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF252934)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VestelRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(4).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = VestelRed
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

