package com.music.bitchord.data.system

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.os.SystemClock

/**
 * A tiny authenticated read-only bridge from Samsung SystemUI/AOD to Vexaura.
 * It does not expose files, shell, root operations, user data or network access.
 * A process may read the Now Bar palette only if it is literally one of
 * Samsung's actual signed system-UI packages (checked by its calling UID).
 */
class VexauraSystemBridgeProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    private fun enforceCaller() {
        val caller = Binder.getCallingUid()
        val app = requireNotNull(context)
        if (caller == Process.myUid()) return
        val packages = app.packageManager.getPackagesForUid(caller).orEmpty()
        if (packages.none { it == "com.android.systemui" || it == "com.samsung.android.app.aodservice" }) {
            throw SecurityException("Not an authorized system UI process")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        enforceCaller()
        require(uri.path == "/nowbar") { "Unknown bridge path" }
        val prefs = requireNotNull(context).getSharedPreferences("bitchord_settings", 0)
        val selected = runCatching {
            NowBarAccent.valueOf(prefs.getString("vexaura_nowbar_accent", "SYSTEM") ?: "SYSTEM")
        }.getOrDefault(NowBarAccent.SYSTEM)
        return MatrixCursor(arrayOf("enabled", "accent", "argb")).apply {
            addRow(arrayOf<Any>(if (selected == NowBarAccent.SYSTEM) 0 else 1, selected.name, selected.color))
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        enforceCaller()
        if (method != "nowbar_heartbeat") return Bundle.EMPTY
        val app = requireNotNull(context)
        app.getSharedPreferences("vexaura_hook_status", 0).edit()
            .putLong("last_seen", System.currentTimeMillis())
            .putString("process", arg.orEmpty().take(80))
            .apply()
        return Bundle().apply { putBoolean("ack", true) }
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
