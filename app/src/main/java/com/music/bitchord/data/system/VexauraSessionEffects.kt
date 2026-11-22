package com.music.bitchord.data.system

import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import kotlinx.coroutines.flow.MutableStateFlow

data class SessionFxStatus(
    val sessionId: Int = 0,
    val bassActive: Boolean = false,
    val gainActive: Boolean = false,
    val note: String = "Warten auf aktive Wiedergabe.",
)

/**
 * Platform DSP effects attached ONLY to Vexaura's current audio session.
 * BassBoost is not a multiband EQ; LoudnessEnhancer amplifies/compresses
 * digital audio but cannot bypass a physical amplifier's hardware limits.
 * No system-file writes and no global output-mix effect.
 */
object VexauraAudioFxRuntime {
    val status = MutableStateFlow(SessionFxStatus())
}

class VexauraSessionEffects {
    val status = MutableStateFlow(SessionFxStatus())
    private var session = 0
    private var bass: BassBoost? = null
    private var gain: LoudnessEnhancer? = null
    private var appliedBass = -1
    private var appliedGain = -1

    fun update(sessionId: Int, strength: Int, gainMb: Int, bitPerfect: Boolean) {
        if (bitPerfect || sessionId <= 0) {
            close()
            status.value = SessionFxStatus(
                note = if (bitPerfect) "Bei Bit-perfect deaktiviert: unveränderte Audiodaten." else "Warten auf aktive Wiedergabe.",
            )
            VexauraAudioFxRuntime.status.value = status.value
            return
        }
        if (session != sessionId) {
            close()
            session = sessionId
        }
        val targetBass = strength.coerceIn(0, 1000)
        val targetGain = gainMb.coerceIn(0, 1200)
        val bassReady = if (targetBass == 0) {
            runCatching { bass?.enabled = false }
            false
        } else {
            if (bass == null) {
                bass = runCatching { BassBoost(0, sessionId) }.getOrNull()
                appliedBass = -1
            }
            bass?.let { effect ->
                runCatching {
                    if (appliedBass != targetBass) {
                        effect.setStrength(targetBass.toShort())
                        appliedBass = targetBass
                    }
                    effect.enabled = true
                    effect.enabled
                }.getOrDefault(false)
            } ?: false
        }
        val gainReady = if (targetGain == 0) {
            runCatching { gain?.enabled = false }
            false
        } else {
            if (gain == null) {
                gain = runCatching { LoudnessEnhancer(sessionId) }.getOrNull()
                appliedGain = -1
            }
            gain?.let { effect ->
                runCatching {
                    if (appliedGain != targetGain) {
                        effect.setTargetGain(targetGain)
                        appliedGain = targetGain
                    }
                    effect.enabled = true
                    effect.enabled
                }.getOrDefault(false)
            } ?: false
        }
        status.value = SessionFxStatus(
            sessionId = sessionId,
            bassActive = bassReady,
            gainActive = gainReady,
            note = when {
                (targetBass > 0 && !bassReady) || (targetGain > 0 && !gainReady) ->
                    "Mindestens ein DSP-Effekt wird von diesem Ausgabepfad nicht unterstützt."
                targetBass == 0 && targetGain == 0 ->
                    "Keine zusätzliche Audioverarbeitung aktiviert."
                else -> "Audioeffekte auf Vexauras Wiedergabesitzung aktiv.",
            },
        )
        VexauraAudioFxRuntime.status.value = status.value
    }

    fun close() {
        runCatching { bass?.enabled = false }
        runCatching { gain?.enabled = false }
        runCatching { bass?.release() }
        runCatching { gain?.release() }
        bass = null
        gain = null
        session = 0
        appliedBass = -1
        appliedGain = -1
        status.value = SessionFxStatus()
        VexauraAudioFxRuntime.status.value = status.value
    }
}
