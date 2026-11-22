package com.music.bitchord.data.system

/**
 * A real in-process signal. The Xposed module replaces this method's result
 * with true only after the framework actually loads the module into this app.
 * Framework classloader probing alone is not proof of authorization.
 */
object XposedModuleStatus {
    @JvmStatic fun isActive(): Boolean = false
}
