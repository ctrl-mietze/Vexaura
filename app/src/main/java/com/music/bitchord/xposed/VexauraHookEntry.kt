package com.music.bitchord.xposed

import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import java.util.WeakHashMap
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Optional, tightly scoped Xposed compatibility layer. No file writes,
 * no signature bypass, no method security bypass and no service disabling.
 * It only tints Samsung's existing media capsule view in memory.
 */
class VexauraHookEntry : IXposedHookLoadPackage {
    /** Keep a weak record of the OEM's initial tint so Systemstandard can restore it. */
    private val originalTints = WeakHashMap<View, ColorStateList?>()

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        when (param.packageName) {
            "ctrl.mietze.vexaura", "ctrl.mietze.vexaura.dev" -> markOwnApp(param)
            "com.android.systemui", "com.samsung.android.app.aodservice" -> installNowBarHook(param)
        }
    }

    private fun markOwnApp(param: XC_LoadPackage.LoadPackageParam) {
        runCatching {
            val state = XposedHelpers.findClassIfExists(
                "com.music.bitchord.data.system.XposedModuleStatus",
                param.classLoader,
            ) ?: return
            XposedHelpers.findAndHookMethod(
                state,
                "isActive",
                XC_MethodReplacement.returnConstant(true),
            )
        }.onFailure { XposedBridge.log("Vexaura: app status hook failed: " + it.message) }
    }

    private fun installNowBarHook(param: XC_LoadPackage.LoadPackageParam) {
        val mediaCard = XposedHelpers.findClassIfExists(
            "com.samsung.android.app.facewidget.presentation.NowBarMediaCardView",
            param.classLoader,
        ) ?: return // No matching Samsung Now Bar on this build; leave SystemUI alone.

        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(hook: MethodHookParam) {
                val card = hook.thisObject as? View ?: return
                card.post { runCatching { refreshCard(card, param.packageName) } }
            }
        }
        runCatching {
            XposedBridge.hookAllConstructors(mediaCard, callback)
            // Samsung replaces its own backgrounds on subsequent card updates.
            // The guard is per class/method; missing OEM methods are skipped.
            for (methodName in arrayOf("m0", "setBgColor")) {
                runCatching { XposedBridge.hookAllMethods(mediaCard, methodName, callback) }
            }
        }.onFailure { XposedBridge.log("Vexaura: media Now Bar hook failed: " + it.message) }
    }

    private fun refreshCard(card: View, processName: String) {
        val ctx = card.context ?: return
        val uri = Uri.parse("content://" + com.music.bitchord.BuildConfig.APPLICATION_ID + ".systembridge/nowbar")
        val resolver = ctx.contentResolver
        // Report the callback even with the default Samsung palette selected.
        runCatching { resolver.call(uri, "nowbar_heartbeat", processName, null) }
        val cursor = runCatching { resolver.query(uri, null, null, null, null) }.getOrNull() ?: return
        val selection = cursor.use {
            if (!it.moveToFirst()) return
            val enabled = it.getInt(it.getColumnIndexOrThrow("enabled")) == 1
            val color = it.getInt(it.getColumnIndexOrThrow("argb"))
            enabled to color
        }
        val layer = runCatching { XposedHelpers.getObjectField(card, "o1") as? View }.getOrNull()
            ?: card

        if (selection.first) {
            synchronized(originalTints) {
                if (!originalTints.containsKey(layer)) {
                    originalTints[layer] = layer.backgroundTintList
                }
            }
            layer.backgroundTintList = ColorStateList.valueOf(selection.second)
        } else {
            // Never leave a custom tint behind after selecting the OEM default.
            // A null stored tint is a valid original value.
            synchronized(originalTints) {
                if (originalTints.containsKey(layer)) {
                    layer.backgroundTintList = originalTints.remove(layer)
                }
            }
        }
    }
}
