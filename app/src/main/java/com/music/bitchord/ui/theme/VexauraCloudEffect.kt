package com.music.bitchord.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.music.bitchord.data.settings.VexauraStyle

/**
 * Low-contrast ambient lighting for the animated OLED profiles.
 *
 * This modifier paints *after* page content so it stays visible on screens
 * with opaque backgrounds. The tints are deliberately faint: text remains
 * legible, controls keep their usual shapes and the base is true black.
 * It does not intercept touch input and it stops animating in reduced-motion
 * mode. The original and the two static profiles get no extra drawing.
 */
@Composable
fun Modifier.vexauraCloudEffect(
    style: VexauraStyle,
    reduceMotion: Boolean,
): Modifier {
    if (!style.animated) return this

    val drift = if (reduceMotion) {
        0.5f
    } else {
        val motion = rememberInfiniteTransition(label = "VexauraCloudDrift")
        val phase by motion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 28_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "CloudPosition",
        )
        phase
    }

    val hasRed = style == VexauraStyle.VEYRA_CLOUD_RED_PURPLE_OLED
    return drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@drawWithContent

        // The dark scene is always at least 85% untouched at the brightest
        // point, and transparent elsewhere. Drift takes 28 seconds per pass.
        val first = if (hasRed) Color(0xFFCF335C) else Color(0xFF8054DF)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(first.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(
                    x = width * (0.20f + drift * 0.43f),
                    y = height * (0.20f + drift * 0.13f),
                ),
                radius = width * 0.95f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8757D8).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(
                    x = width * (0.86f - drift * 0.38f),
                    y = height * (0.76f - drift * 0.20f),
                ),
                radius = width * 0.92f,
            ),
        )
    }
}
