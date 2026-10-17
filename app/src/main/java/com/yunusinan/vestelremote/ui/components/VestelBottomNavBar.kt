package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusinan.vestelremote.R
import com.yunusinan.vestelremote.ui.theme.DarkBorder
import com.yunusinan.vestelremote.ui.theme.DarkSurface
import com.yunusinan.vestelremote.ui.theme.DarkTextSecondary
import com.yunusinan.vestelremote.ui.theme.VestelRed

data class BottomNavDestination(
    val index: Int,
    val icon: ImageVector,
    val labelResId: Int
)

private val Destinations = listOf(
    BottomNavDestination(0, Icons.Default.GridView, R.string.nav_dashboard),
    BottomNavDestination(1, Icons.Default.SettingsRemote, R.string.nav_remote),
    BottomNavDestination(2, Icons.Default.Apps, R.string.nav_apps),
    BottomNavDestination(3, Icons.Default.Input, R.string.nav_inputs),
    BottomNavDestination(4, Icons.Default.Settings, R.string.nav_settings)
)

/**
 * 5-destination bottom navigation bar matching the reference design:
 * Panel, Kumanda, Uygulamalar, Girişler, Ayarlar.
 */
@Composable
fun VestelBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = DarkBorder.copy(alpha = 0.85f)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 6.dp)
            ) {
                val fontScale = LocalDensity.current.fontScale.coerceAtLeast(0.85f)
                val slotWidth = maxWidth / Destinations.size
                val effectiveSlotWidth = slotWidth / fontScale

                val showAllLabels = effectiveSlotWidth >= 46.dp
                val adaptiveFontSize = when {
                    effectiveSlotWidth >= 68.dp -> 11.sp
                    effectiveSlotWidth >= 56.dp -> 10.5.sp
                    else -> 9.5.sp
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Destinations.forEach { destination ->
                        val isSelected = selectedTab == destination.index
                        val label = stringResource(destination.labelResId)
                        val itemWeight = if (!showAllLabels && isSelected) 1.45f else 1f

                        VestelBottomNavItem(
                            icon = destination.icon,
                            label = label,
                            isSelected = isSelected,
                            showLabel = showAllLabels || isSelected,
                            labelFontSize = adaptiveFontSize,
                            onClick = { onTabSelected(destination.index) },
                            modifier = Modifier
                                .weight(itemWeight)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VestelBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    showLabel: Boolean,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val inactiveColor = Color(0xFFA6ADBB)
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) VestelRed else inactiveColor,
        label = "navIconTint"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) VestelRed else inactiveColor,
        label = "navTextColor"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.34f else 0f,
        label = "navGlowAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        label = "navItemScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                role = Role.Tab
                this.selected = isSelected
                contentDescription = label
            }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VestelRed.copy(alpha = glowAlpha),
                            VestelRed.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = labelFontSize,
                    lineHeight = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = (-0.1).sp
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

