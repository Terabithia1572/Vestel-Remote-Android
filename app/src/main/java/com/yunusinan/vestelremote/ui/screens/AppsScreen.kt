package com.yunusinan.vestelremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.domain.model.TvApp
import com.yunusinan.vestelremote.ui.theme.*

@Composable
fun AppsScreen(
    favoriteAppIds: Set<String>,
    onLaunchApp: (TvApp) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val allApps = TvApp.DEFAULT_CATALOG

    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val favorites = remember(filteredApps, favoriteAppIds) {
        filteredApps.filter { favoriteAppIds.contains(it.id) }
    }

    val others = remember(filteredApps, favoriteAppIds) {
        filteredApps.filterNot { favoriteAppIds.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Uygulama ara...", color = DarkTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VestelRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Favorite Section Header
            if (favorites.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = stringResource(R.string.favorite_apps),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
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

            // All / Other Apps Header
            if (others.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = stringResource(R.string.all_apps),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                items(others, key = { "app-${it.id}" }) { app ->
                    AppCardItem(
                        app = app,
                        isFavorite = false,
                        onLaunch = { onLaunchApp(app) },
                        onToggleFavorite = { onToggleFavorite(app.id) }
                    )
                }
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onLaunch),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo Placeholder / Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(getAppAccentColor(app.id).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = app.name,
                        tint = getAppAccentColor(app.id),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Favorite Star
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) StatusYellow else DarkTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Vestel TV Başlat",
                style = MaterialTheme.typography.labelSmall,
                color = DarkTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

private fun getAppAccentColor(id: String): Color {
    return when (id) {
        "youtube" -> Color(0xFFFF0000)
        "netflix" -> Color(0xFFE50914)
        "prime_video" -> Color(0xFF00A8E1)
        "disney_plus" -> Color(0xFF113CCF)
        "spotify" -> Color(0xFF1DB954)
        "exxen" -> Color(0xFFFFE600)
        "blutv" -> Color(0xFF00C3FF)
        "gain" -> Color(0xFFFF0055)
        "tod" -> Color(0xFF6B21A8)
        "twitch" -> Color(0xFF9146FF)
        else -> VestelRed
    }
}
