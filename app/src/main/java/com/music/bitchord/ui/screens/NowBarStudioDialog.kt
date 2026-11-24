package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.bitchord.xposed.nowbar.ConfigManager
import com.music.bitchord.xposed.nowbar.WallpaperColorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val categoryNames = linkedMapOf(
    "media" to "Musik", "timer_clock" to "Timer", "recorder" to "Aufnahme",
    "flashlight" to "Taschenlampe", "battery" to "Akku", "brief" to "Now Brief",
    "call" to "Anrufe", "modes" to "Modi", "sports_maps_health" to "Sport / Maps",
    "other_ongoing" to "Live-Aktivitäten",
)

@Composable
internal fun NowBarStudioDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val compatible = remember { ConfigManager.isCompatible(context) }
    var json by remember { mutableStateOf(ConfigManager.loadFromAppPrefs(context).toJsonString()) }
    val config = remember(json) { ConfigManager.Config.fromJsonString(json) }
    var selected by remember { mutableStateOf<String?>(null) }
    var wallpaper by remember { mutableStateOf<WallpaperColorHelper.PaletteResult?>(null) }
    LaunchedEffect(Unit) {
        wallpaper = withContext(Dispatchers.IO) { WallpaperColorHelper.extractLockWallpaperPalette(context, true) }
    }
    fun change(block: (ConfigManager.Config) -> Unit) {
        val next = ConfigManager.Config.fromJsonString(json)
        block(next)
        json = next.toJsonString()
    }
    val id = selected ?: "media"
    val category = config.getCategory(id)
    val liveColor = wallpaper?.let { WallpaperColorHelper.resolveForVariant(it, config.wallpaperVariant) }
    val colors = (if (selected == null) config.resolveGlobalGradientArgb() else config.resolveGradientArgbForCategory(id))
        ?.map { Color(it) }
        ?: listOf(Color(if (selected == null) config.resolveGlobalFinalArgb(liveColor) else config.resolveFinalArgbForCategory(id, liveColor))).let { it + it }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Now Bar Studio") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (compatible) "AOD-Build 896548000 erkannt. LSPosed-Scope für SystemUI und AOD erforderlich."
                    else "Vorschau verfügbar. Der erweiterte Hook benötigt AOD-Service 8.9.65.48 (896548000). Der bestehende Farbstil bleibt nutzbar.",
                    style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Erweiterte Styles", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Switch(checked = config.masterEnabled && compatible, enabled = compatible,
                        onCheckedChange = { checked -> change { it.masterEnabled = checked } })
                }
                // The preview draws the same color stops and opacity as the hook.
                // Samsung's native SemBlurInfo is only available inside its real view.
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SPERRBILDSCHIRM · VORSCHAU", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(colors), RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(selected?.let { categoryNames[it] } ?: "Vexaura · Now Playing",
                                color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                            Text("▂▅▃▆▂", color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(if (config.styleMode == ConfigManager.STYLE_BLUR) "Blur-Farbvorschau (native Unschärfe erst im System)" else "Live-Farbvorschau",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected == null, { selected = null }, { Text("Global") })
                    categoryNames.forEach { (key, label) ->
                        FilterChip(selected == key, { selected = key }, { Text(label) })
                    }
                }
                if (selected == null) {
                    Text("Darstellung", fontWeight = FontWeight.SemiBold)
                    ChoiceRow(listOf("Vollton", "Wallpaper", "Verlauf"), config.colorMode) { mode -> change { it.colorMode = mode } }
                    ChoiceRow(listOf("Solid", "Frosted Blur"), config.styleMode) { mode -> change { it.styleMode = mode } }
                    when (config.colorMode) {
                        ConfigManager.MODE_FIXED_COLOR -> HexColorField("Farbe", config.globalColor) { v -> change { it.globalColor = v } }
                        ConfigManager.MODE_GRADIENT -> {
                            HexColorField("Verlauf – Start", config.gradientStartColor) { v -> change { it.gradientStartColor = v } }
                            HexColorField("Verlauf – Ende", config.gradientEndColor) { v -> change { it.gradientEndColor = v } }
                        }
                        else -> ChoiceRow(listOf("Dominant", "Sekundär", "Monet", "Unten"), config.wallpaperVariant) { v -> change { it.wallpaperVariant = v } }
                    }
                    Text("Deckkraft: ${config.opacity}%")
                    Slider(config.opacity.coerceIn(0,100).toFloat(), { v -> change { it.opacity = v.toInt() } }, valueRange = 0f..100f)
                    TextButton(onClick = {
                        change { it.colorMode = ConfigManager.MODE_GRADIENT; it.gradientStartColor = 0xFF8755E8.toInt(); it.gradientEndColor = 0xFFB44987.toInt() }
                    }) { Text("Veyra Purple / Red Preset") }
                } else {
                    Text(categoryNames[id].orEmpty(), fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Kapsel anpassen", Modifier.weight(1f))
                        Switch(category.enabled, { v -> change { it.getCategory(id).enabled = v } })
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Eigene Farbe", Modifier.weight(1f))
                        Switch(category.useCustomColor, { v -> change { it.getCategory(id).useCustomColor = v } })
                    }
                    if (category.useCustomColor) {
                        ChoiceRow(listOf("Vollton", "Verlauf"), if (category.customColorMode == ConfigManager.MODE_GRADIENT) 1 else 0) { v ->
                            change { it.getCategory(id).customColorMode = if (v == 1) ConfigManager.MODE_GRADIENT else ConfigManager.MODE_FIXED_COLOR }
                        }
                        if (category.customColorMode == ConfigManager.MODE_GRADIENT) {
                            HexColorField("Start", category.customGradientStart) { v -> change { it.getCategory(id).customGradientStart = v } }
                            HexColorField("Ende", category.customGradientEnd) { v -> change { it.getCategory(id).customGradientEnd = v } }
                        } else HexColorField("Farbe", category.customColor) { v -> change { it.getCategory(id).customColor = v } }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Eigene Deckkraft", Modifier.weight(1f))
                        Switch(category.useCustomOpacity, { v -> change { it.getCategory(id).useCustomOpacity = v } })
                    }
                    if (category.useCustomOpacity) {
                        Text("Deckkraft: ${category.customOpacity}%")
                        Slider(category.customOpacity.coerceIn(0,100).toFloat(), { v -> change { it.getCategory(id).customOpacity = v.toInt() } }, valueRange = 0f..100f)
                    }
                }
                TextButton(onClick = { json = ConfigManager.Config().toJsonString() }) { Text("Studio zurücksetzen") }
                Text("Basierend auf Now Bar Color von tavataan · MIT", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { TextButton(onClick = {
            if (!compatible) config.masterEnabled = false
            ConfigManager.saveAndSync(context, config)
            onDismiss()
        }) { Text("Anwenden") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

@Composable
private fun ChoiceRow(labels: List<String>, selection: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { i, label ->
            FilterChip(selected = selection == i, onClick = { onSelect(i) }, label = { Text(label) })
        }
    }
}

@Composable
private fun HexColorField(label: String, argb: Int, onColor: (Int) -> Unit) {
    var value by remember(argb) { mutableStateOf("#%06X".format(argb and 0xFFFFFF)) }
    OutlinedTextField(value, { input ->
        value = input.take(7)
        val clean = value.removePrefix("#")
        if (clean.length == 6) clean.toIntOrNull(16)?.let { onColor(it or 0xFF000000.toInt()) }
    }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}
