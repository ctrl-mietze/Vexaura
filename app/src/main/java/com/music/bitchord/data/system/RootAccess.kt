package com.music.bitchord.data.system

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

/** The presence of su is not proof that Vexaura has been granted UID 0. */
enum class RootGrant { NOT_CHECKED, GRANTED, DENIED, NO_SU, TIMEOUT }

data class RootCheck(val grant: RootGrant, val detail: String)

/**
 * Read-only proof of superuser authorization via a constant id command.
 * Never remounts or modifies system files. Called only in an explicit
 * System & Root user flow and may prompt through the root manager.
 */
object RootAccess {
    suspend fun check(): RootCheck = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = ProcessBuilder("su", "-c", "id -u")
                .redirectErrorStream(true)
                .start()
            if (!process.waitFor(6, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                RootCheck(RootGrant.TIMEOUT, "Der Root-Manager hat nicht rechtzeitig geantwortet.")
            } else {
                val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
                if (process.exitValue() == 0 && output.lineSequence().any { it.trim() == "0" }) {
                    RootCheck(RootGrant.GRANTED, "UID 0 bestätigt. Vexaura ist als Superuser autorisiert.")
                } else {
                    RootCheck(RootGrant.DENIED, "Kein UID-0-Zugriff. Bitte Vexaura im Root-Manager freigeben.")
                }
            }
        } catch (_: IOException) {
            RootCheck(RootGrant.NO_SU, "Kein ausführbares su gefunden.")
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            RootCheck(RootGrant.TIMEOUT, "Die Root-Prüfung wurde unterbrochen.")
        } catch (_: SecurityException) {
            RootCheck(RootGrant.DENIED, "Der Root-Zugriff wurde vom System blockiert.")
        } finally {
            process?.destroy()
        }
    }
}
