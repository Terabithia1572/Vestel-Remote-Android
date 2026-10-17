package com.yunusinan.vestelremote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable design system tokens for Vestel Remote (Reference AMOLED Dark & Red Accent UI).
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

        val cardShape = RoundedCornerShape(18.dp)
        val heroCardShape = RoundedCornerShape(20.dp)
        val compactCardShape = RoundedCornerShape(16.dp)
        val buttonShape = RoundedCornerShape(12.dp)
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
        val quickActionButtonSize = 52.dp
        val remoteRoundButtonSize = 54.dp
        val numpadButtonSize = 56.dp
        val rockerWidth = 58.dp
        val rockerHeight = 148.dp
        val dpadSize = 204.dp
        val touchpadHeight = 204.dp
        val bottomNavHeight = 66.dp
    }

    object IconSize {
        val xs = 14.dp
        val sm = 18.dp
        val md = 22.dp
        val lg = 26.dp
        val xl = 32.dp
    }

    object Gradients {
        val screenBackground = Brush.verticalGradient(
            colors = listOf(DarkBackgroundTop, DarkBackground)
        )
        val cardSurface = Brush.verticalGradient(
            colors = listOf(Color(0xFF191D28), Color(0xFF13161F))
        )
    }
}

