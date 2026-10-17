package com.yunusinan.vestelremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
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
import com.yunusinan.vestelremote.ui.theme.DarkSurfaceElevated
import com.yunusinan.vestelremote.ui.theme.DarkTextSecondary
import com.yunusinan.vestelremote.ui.theme.VestelRed

data class BottomNavDestination(
    val index: Int,
    val icon: ImageVector,
    val labelResId: Int
)

private val Destinations = listOf(
    BottomNavDestination(0, Icons.Default.Dashboard, R.string.nav_dashboard),
    BottomNavDestination(1, Icons.Default.SettingsRemote, R.string.nav_remote),
    BottomNavDestination(2, Icons.Default.Apps, R.string.nav_apps),
    BottomNavDestination(3, Icons.Default.Input, R.string.nav_inputs),
    BottomNavDestination(4, Icons.Default.Devices, R.string.nav_devices),
    BottomNavDestination(5, Icons.Default.Settings, R.string.nav_settings)
)

/**
 * Adaptive 6-destination bottom navigation bar designed so long Turkish labels
 * such as "Uygulamalar" stay on a single line with aligned icons and labels.
 *
 * - On standard/wide screens (including Samsung Galaxy S24 Ultra), all 6 destinations
 *   render single-line labels with adaptive typography and minimal horizontal cell padding.
 * - On ultra-compact widths or very large accessibility font scales (`effectiveSlotWidth < 50.dp`),
 *   it switches to an adaptive compact mode where the active destination displays its full
 *   single-line label and inactive tabs preserve full TalkBack accessibility semantics.
 */
@Composable
fun VestelBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurfaceElevated,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = DarkBorder.copy(alpha = 0.7f)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 4.dp)
            ) {
                val fontScale = LocalDensity.current.fontScale.coerceAtLeast(0.85f)
                val slotWidth = maxWidth / Destinations.size
                val effectiveSlotWidth = slotWidth / fontScale

                // Deliberate adaptive fallback when 6 full labels cannot fit readably
                val showAllLabels = effectiveSlotWidth >= 48.dp
                val adaptiveFontSize = when {
                    effectiveSlotWidth >= 62.dp -> 10.5.sp
                    effectiveSlotWidth >= 54.dp -> 10.sp
                    else -> 9.5.sp
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Destinations.forEach { destination ->
                        val isSelected = selectedTab == destination.index
                        val label = stringResource(destination.labelResId)
                        val itemWeight = if (!showAllLabels && isSelected) 1.55f else 1f

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
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) VestelRed else DarkTextSecondary,
        label = "navIconTint"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) VestelRed else DarkTextSecondary,
        label = "navTextColor"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.16f else 0f,
        label = "navIndicatorAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
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
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(VestelRed.copy(alpha = indicatorAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(21.dp)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = labelFontSize,
                    lineHeight = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = (-0.15).sp
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
