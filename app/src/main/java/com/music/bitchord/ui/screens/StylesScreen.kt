package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.VexauraStyle
import com.music.bitchord.ui.theme.vexauraCloudEffect

/**
 * Vexaura profile selector. Uses the app's standard settings navigation and
 * low-contrast surfaces rather than a separate-looking custom dashboard.
 * A tap shows a confirmation; no setting changes until Apply is pressed.
 */
@Composable
fun StylesScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val applied by AppSettings.vexauraStyle.collectAsStateWithLifecycle()
    val reducedMotion by AppSettings.reduceAnimation.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<VexauraStyle?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        Text(
            text = "Styles",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 7.dp),
        )
        Text(
            text = "Dein Sound. Dein Look. Alle Bedienelemente bleiben vertraut.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        )

        StyleSectionLabel("STANDARD & OLED")
        listOf(
            VexauraStyle.STANDARD,
            VexauraStyle.VEYRA_OLED_PURPLE,
            VexauraStyle.VEYRA_OLED_PURPLE_RED,
        ).forEach { option ->
            VexauraStyleCard(
                option = option,
                selected = applied == option,
                reducedMotion = reducedMotion,
                onClick = { pending = option },
            )
        }

        Spacer(Modifier.height(18.dp))
        StyleSectionLabel("ANIMATION STYLES")
        Text(
            text = "Langsame, weiche Lichtwolken über echtem OLED-Schwarz.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 11.dp),
        )
        listOf(
            VexauraStyle.VEYRA_CLOUD_OLED_PURPLE,
            VexauraStyle.VEYRA_CLOUD_RED_PURPLE_OLED,
        ).forEach { option ->
            VexauraStyleCard(
                option = option,
                selected = applied == option,
                reducedMotion = reducedMotion,
                onClick = { pending = option },
            )
        }
        Text(
            text = "Die Animation pausiert, wenn „Animationen reduzieren“ aktiv ist.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 28.dp),
        )
    }

    pending?.let { selected ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Style anwenden?") },
            text = {
                Text(
                    text = "„" + selected.label + "“ als Vexaura-Design verwenden?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppSettings.setVexauraStyle(selected)
                        pending = null
                    },
                ) {
                    Text("Anwenden")
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) {
                    Text("Abbrechen")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun StyleSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun VexauraStyleCard(
    option: VexauraStyle,
    selected: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit,
) {
    val accent = option.styleAccent()
    val border = if (selected) accent.copy(alpha = 0.58f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            if (selected) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Aktiv",
                    tint = accent,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(13.dp))
        VexauraStylePreview(option, reducedMotion)
    }
}

private fun VexauraStyle.styleAccent(): Color = when (this) {
    VexauraStyle.STANDARD -> Color(0xFFE8E8EC)
    VexauraStyle.VEYRA_OLED_PURPLE, VexauraStyle.VEYRA_CLOUD_OLED_PURPLE -> Color(0xFFA883FA)
    VexauraStyle.VEYRA_OLED_PURPLE_RED, VexauraStyle.VEYRA_CLOUD_RED_PURPLE_OLED -> Color(0xFFDE638E)
}

@Composable
private fun VexauraStylePreview(style: VexauraStyle, reducedMotion: Boolean) {
    val accent = style.styleAccent()
    val purple = Color(0xFF8753C2)
    val gradientEnd = when (style) {
        VexauraStyle.STANDARD -> Color(0xFF303037)
        VexauraStyle.VEYRA_OLED_PURPLE, VexauraStyle.VEYRA_CLOUD_OLED_PURPLE -> Color(0xFF211331)
        else -> Color(0xFF28111F)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(133.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .vexauraCloudEffect(style, reducedMotion),
    ) {
        Column(Modifier.padding(start = 13.dp, end = 13.dp, top = 10.dp)) {
            Text(
                text = "Bibliothek",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(57.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    if (style == VexauraStyle.STANDARD) Color(0xFF3B373E) else accent.copy(alpha = 0.37f),
                                    gradientEnd,
                                ),
                            ),
                        ),
                ) {
                    Column(Modifier.padding(start = 10.dp, top = 9.dp)) {
                        Text("DEIN HÖRERLEBNIS", fontSize = 8.sp, letterSpacing = 0.5.sp, color = Color.White.copy(alpha = 0.78f))
                        Spacer(Modifier.height(4.dp))
                        Text("185", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(57.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    if (style == VexauraStyle.STANDARD) Color(0xFF454149) else accent.copy(alpha = 0.59f),
                                    if (style == VexauraStyle.STANDARD) Color(0xFF242428) else purple.copy(alpha = 0.31f),
                                ),
                            ),
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp, end = 12.dp, bottom = 9.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF28282C).copy(alpha = 0.92f))
                .border(0.75.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("Start", "Entdecken", "Bibliothek", "Suche").forEach { tab ->
                val active = tab == "Bibliothek"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (active) {
                                if (style == VexauraStyle.STANDARD) Color(0xFF57575C) else accent.copy(alpha = 0.36f)
                            } else Color.Transparent,
                        )
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab,
                        fontSize = 9.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Color.White else Color(0xFFBBBBBE),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
