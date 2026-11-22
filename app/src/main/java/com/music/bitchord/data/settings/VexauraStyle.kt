package com.music.bitchord.data.settings

/**
 * User-selectable Vexaura visual profiles.
 *
 * The original interface stays the default. Animated profiles only add
 * a restrained ambient effect; layouts, touch targets and navigation stay
 * identical across all profiles.
 */
enum class VexauraStyle(
    val label: String,
    val description: String,
    val animated: Boolean,
) {
    STANDARD(
        label = "Standard",
        description = "Das originale, ruhige Liquid-Glass-Design.",
        animated = false,
    ),
    VEYRA_OLED_PURPLE(
        label = "Veyra OLED Lila",
        description = "Tiefes OLED-Schwarz mit violetten Akzenten.",
        animated = false,
    ),
    VEYRA_OLED_PURPLE_RED(
        label = "Veyra OLED Lila Red",
        description = "OLED-Schwarz, Violett und ein Hauch Rot.",
        animated = false,
    ),
    VEYRA_CLOUD_OLED_PURPLE(
        label = "Veyra Cloud OLED Purple",
        description = "Sanft wandernde violette Lichtwolken.",
        animated = true,
    ),
    VEYRA_CLOUD_RED_PURPLE_OLED(
        label = "Veyra Cloud Red Purple OLED",
        description = "Langsam fließende rote und violette OLED-Clouds.",
        animated = true,
    ),
}
