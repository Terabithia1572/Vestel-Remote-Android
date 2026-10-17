package com.yunusinan.vestelremote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Reusable design system tokens for Vestel Remote (AMOLED Dark & Red Accent UI).
 */
object VestelDesignTokens {

    object Spacing {
        val xxs = 4.dp
        val xs = 6.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
    }

    object Radius {
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 20.dp
        val xl = 24.dp
        val pill = 34.dp

        val cardShape = RoundedCornerShape(lg)
        val heroCardShape = RoundedCornerShape(22.dp)
        val compactCardShape = RoundedCornerShape(md)
        val buttonShape = RoundedCornerShape(sm)
        val pillShape = RoundedCornerShape(pill)
    }

    object Elevation {
        val none = 0.dp
        val low = 2.dp
        val medium = 6.dp
        val high = 12.dp
    }

    object Dimensions {
        val minTouchTarget = 48.dp
        val quickActionButtonSize = 48.dp
        val remoteRoundButtonSize = 52.dp
        val numpadButtonSize = 56.dp
        val rockerWidth = 66.dp
        val rockerHeight = 144.dp
        val dpadSize = 228.dp
        val touchpadHeight = 236.dp
        val bottomNavHeight = 68.dp
    }

    object IconSize {
        val xs = 14.dp
        val sm = 18.dp
        val md = 22.dp
        val lg = 26.dp
        val xl = 32.dp
    }
}
