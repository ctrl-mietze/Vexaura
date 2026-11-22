package com.music.bitchord.data.system

/** Now Bar accent profiles. Only an opted-in Samsung Xposed hook reads them. */
enum class NowBarAccent(val title: String, val color: Int) {
    SYSTEM("Systemstandard", 0),
    PURPLE("Veyra OLED Lila", 0xFF8755E8.toInt()),
    PURPLE_RED("Veyra Red Purple", 0xFFB44987.toInt()),
}
