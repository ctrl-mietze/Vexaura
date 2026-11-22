package com.music.bitchord.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.BitPerfectMode
import com.music.bitchord.data.settings.OutputPcmMode
import com.music.bitchord.data.system.NowBarAccent
import com.music.bitchord.data.system.RootAccess
import com.music.bitchord.data.system.RootCheck
import com.music.bitchord.data.system.RootGrant
import com.music.bitchord.data.system.VexauraAudioFxRuntime
import com.music.bitchord.data.system.XposedModuleStatus
import com.music.bitchord.widget.MediaWidgetPill
import com.music.bitchord.widget.MediaWidgetSquare
import com.music.bitchord.widget.MediaWidgetWide
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A native Settings page: same type scale, inset groups, rounded surfaces and
 * row behavior as the rest of Vexaura. No global SYSTEM_UID impersonation or
 * system-file patching. Each capability reports its real support state.
 */
@Composable
fun SystemRootScreen(
    contentPadding: PaddingValues,
    onWifiQuality: () -> Unit,
    onCellularQuality: () -> Unit,
    onBitPerfect: () -> Unit,
    onPlaybackSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val samsung = remember { Build.MANUFACTURER.equals("samsung", ignoreCase = true) }
    val xposedApp = remember { XposedModuleStatus.isActive() }
    val pinManager = remember { AppWidgetManager.getInstance(context) }
    val pinSupported = remember { pinManager.isRequestPinAppWidgetSupported }
    val bass by AppSettings.vexauraBassStrength.collectAsStateWithLifecycle()
    val gain by AppSettings.vexauraGainMb.collectAsStateWithLifecycle()
    val nowBarAccent by AppSettings.vexauraNowBarAccent.collectAsStateWithLifecycle()
    val pcm by AppSettings.outputPcmMode.collectAsStateWithLifecycle()
    val bitPerfect by AppSettings.bitPerfectMode.collectAsStateWithLifecycle()
    val fx by VexauraAudioFxRuntime.status.collectAsStateWithLifecycle()
    var root by remember { mutableStateOf(RootCheck(RootGrant.NOT_CHECKED, "Noch nicht geprüft.")) }
    var checkingRoot by remember { mutableStateOf(false) }
    var selectNowBar by remember { mutableStateOf<NowBarAccent?>(null) }

    // Refreshing is an explicit in-page action; the root manager may display
    // its native Superuser authorization dialog on the very first check.
    fun checkRoot() {
        if (checkingRoot) return
        checkingRoot = true
        scope.launch {
            root = RootAccess.check()
            checkingRoot = false
        }
    }
    LaunchedEffect(Unit) { checkRoot() }

    val lastHook = remember(nowBarAccent, checkingRoot) {
        context.getSharedPreferences("vexaura_hook_status", 0).getLong("last_seen", 0L)
    }
    val systemUiHookConfirmed = xposedApp && lastHook > 0L &&
        System.currentTimeMillis() - lastHook in 0L..600_000L

    fun pinWidget(kind: String) {
        if (!pinSupported) {
            Toast.makeText(context, "Launcher unterstützt keine Widget-Anfrage. Lange auf den Startbildschirm tippen → Widgets.", Toast.LENGTH_LONG).show()
            return
        }
        val component = when (kind) {
            "pill" -> ComponentName(context, MediaWidgetPill::class.java)
            "wide" -> ComponentName(context, MediaWidgetWide::class.java)
            else -> ComponentName(context, MediaWidgetSquare::class.java)
        }
        val requested = runCatching {
            pinManager.requestPinAppWidget(component, null, null)
        }.getOrDefault(false)
        Toast.makeText(
            context,
            if (requested) "Widget-Anfrage an deinen Launcher gesendet." else "Widget konnte nicht angefordert werden.",
            Toast.LENGTH_SHORT,
        ).show()
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(contentPadding),
    ) {
        SettingsPageTitle("System & Root")
        Text(
            text = "Erweiterte Audio- und Systemfunktionen – mit klarer Berechtigungsprüfung.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
        )

        if (root.grant != RootGrant.GRANTED || !xposedApp) {
            SettingsGroup(
                header = "Berechtigungen",
                footer = "KernelSU, Magisk und andere SU-Manager müssen die Freigabe ausdrücklich erteilen. Xposed braucht die Vexaura- und Samsung-SystemUI-Scope im Modulmanager.",
            ) {
                SettingsRow(
                    icon = Icons.Rounded.Security,
                    title = "Superuser / UID 0",
                    subtitle = if (checkingRoot) "Root wird geprüft …" else root.detail,
                    value = if (root.grant == RootGrant.GRANTED) "Aktiv" else "Prüfen",
                    onClick = ::checkRoot,
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Rounded.Extension,
                    title = "Xposed / LSPosed",
                    subtitle = if (xposedApp) "Vexaura-Modul im App-Prozess aktiv." else "Noch nicht aktiviert. Modul in deinem Xposed-Manager einschalten und Vexaura neu starten.",
                    value = if (xposedApp) "Aktiv" else "Fehlt",
                )
            }
        } else {
            SettingsGroup(header = "Systemzugriff") {
                SettingsRow(
                    icon = Icons.Rounded.VerifiedUser,
                    title = "Root & Xposed verbunden",
                    subtitle = "UID 0 erteilt, Xposed-Modul in Vexaura aktiv.",
                    value = "Bereit",
                    onClick = ::checkRoot,
                )
            }
        }

        SettingsGroup(
            header = "Sound Settings",
            footer = "Die Effekte gelten für Vexauras eigene Wiedergabesitzung. Sie umgehen keine Hardware-Schutzgrenze und werden bei Bit-perfect automatisch deaktiviert.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Bass Boost",
                subtitle = "Ein Bass-Stärkeregler, kein Equalizer. Bis 1000/1000, abhängig vom Audio-Treiber.",
                value = (bass / 10).toString() + "%",
            )
            Slider(
                value = bass.toFloat(),
                onValueChange = { AppSettings.setVexauraBassStrength((it / 10f).roundToInt() * 10) },
                valueRange = 0f..1000f,
                steps = 99,
                enabled = bitPerfect == BitPerfectMode.OFF,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 23.dp),
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.VolumeUp,
                title = "Audio-Verstärkung",
                subtitle = "Session-Loudness-Enhancer mit Dynamikkompression, unabhängig von der Systemlautstärke.",
                value = "+" + (gain / 100f).toString() + " dB",
            )
            Slider(
                value = gain.toFloat(),
                onValueChange = { AppSettings.setVexauraGainMb((it / 50f).roundToInt() * 50) },
                valueRange = 0f..1200f,
                steps = 23,
                enabled = bitPerfect == BitPerfectMode.OFF,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 23.dp),
            )
            Text(
                text = fx.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 23.dp, vertical = 12.dp),
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Restore,
                title = "Audioeffekte zurücksetzen",
                subtitle = "Bass und zusätzliche Verstärkung auf null setzen.",
                onClick = {
                    AppSettings.setVexauraBassStrength(0)
                    AppSettings.setVexauraGainMb(0)
                },
            )
        }

        SettingsGroup(
            header = "Qualität & Ausgabe",
            footer = "Lossless betrifft die Quellqualität; die tatsächlich ausgehandelte Hardware-Ausgabe kann abweichen. Für unveränderte Samples bitte einen kompatiblen Bit-perfect-Modus wählen.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.Wifi,
                title = "WLAN-Qualität",
                value = AppSettings.audioQualityWifi.collectAsStateWithLifecycle().value.name,
                onClick = onWifiQuality,
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.SignalCellularAlt,
                title = "Mobilfunk-Qualität",
                value = AppSettings.audioQualityCellular.collectAsStateWithLifecycle().value.name,
                onClick = onCellularQuality,
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.HighQuality,
                title = "PCM-Ausgabe",
                subtitle = "16-bit PCM oder 32-bit Float – je nach Ausgabepfad.",
                value = pcm.label,
                onClick = {
                    AppSettings.setOutputPcmMode(
                        if (pcm == OutputPcmMode.PCM_16) OutputPcmMode.FLOAT_32 else OutputPcmMode.PCM_16,
                    )
                },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Headphones,
                title = "Bit-perfect / USB DAC",
                subtitle = "Vorhandene echte USB-, DAP- und Android-Mixer-Ausgabemodi.",
                value = bitPerfect.name,
                onClick = onBitPerfect,
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Tune,
                title = "Weitere Audio-Einstellungen",
                subtitle = "Dolby, 3D-Audio, Dithering, Normalisierung und Ausgabe.",
                onClick = onPlaybackSettings,
            )
        }

        SettingsGroup(
            header = "Widgets – Startbildschirm",
            footer = if (pinSupported) "Das System fragt nach, bevor ein Widget hinzugefügt wird." else "Zum Hinzufügen die Widget-Auswahl des Launchers öffnen.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.Widgets,
                title = "Vexaura Compact",
                subtitle = "Kompakte Mediensteuerung mit Cover.",
                onClick = { pinWidget("square") },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.ViewAgenda,
                title = "Vexaura Wide",
                subtitle = "Breite Player-Karte mit Titel und Steuerung.",
                onClick = { pinWidget("wide") },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.LinearScale,
                title = "Vexaura Capsule",
                subtitle = "Schlanke Player-Pille mit Musik-Steuerung.",
                onClick = { pinWidget("pill") },
            )
        }

        SettingsGroup(
            header = "Samsung – Sperrbildschirm & Now Bar",
            footer = if (!samsung) "Samsung-exklusiv: Auf anderen Herstellern deaktiviert." else
                "Der Xposed-Hook betrifft nur die Darstellung der vorhandenen Samsung-Medienkapsel. Andere Systemkarten bleiben unverändert.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.Lock,
                title = "Samsung Sperrbildschirm",
                subtitle = if (samsung) "Der Launcher und One UI entscheiden, welche Widgets auf dem Sperrbildschirm erlaubt sind." else "Nur für Samsung One UI.",
                enabled = samsung,
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Palette,
                title = "Now Bar – Farbstil",
                subtitle = when {
                    !samsung -> "Samsung One UI erforderlich."
                    !xposedApp -> "Zuerst Vexaura als Xposed-Modul aktivieren."
                    !systemUiHookConfirmed -> "SystemUI-Hook noch nicht bestätigt. AOD/SystemUI in Xposed freigeben."
                    else -> "Hook aktiv – vorhandene Medienkapsel wird angepasst."
                },
                value = if (samsung) nowBarAccent.title else null,
                enabled = samsung && xposedApp,
                onClick = { selectNowBar = nowBarAccent },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Info,
                title = "Hook-Diagnose",
                subtitle = if (!samsung) "Nicht verfügbar." else if (systemUiHookConfirmed)
                    "SystemUI/AOD-Medienkapsel hat den Hook bestätigt." else
                    "Kein aktueller Hook-Heartbeat. Erst nach Xposed-Neustart und beim Erscheinen der Now Bar prüfbar.",
                value = if (systemUiHookConfirmed) "Aktiv" else "Offen",
                enabled = samsung,
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    selectNowBar?.let { selection ->
        AlertDialog(
            onDismissRequest = { selectNowBar = null },
            title = { Text("Now Bar Farbstil") },
            text = {
                Column {
                    NowBarAccent.entries.forEach { accent ->
                        TextButton(
                            onClick = { selectNowBar = accent },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = (if (selection == accent) "✓  " else "") + accent.title,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Text(
                        "Nur die Medien-Now-Bar wird eingefärbt. Kein Austausch von Systemdateien.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AppSettings.setVexauraNowBarAccent(selection)
                    selectNowBar = null
                }) { Text("Anwenden") }
            },
            dismissButton = {
                TextButton(onClick = { selectNowBar = null }) { Text("Abbrechen") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}
