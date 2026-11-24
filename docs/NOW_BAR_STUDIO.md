# Now Bar Studio

Integrated into Settings → System & Root → Now Bar Studio. Existing media tint and Vexaura features remain available.

Ported Now Bar Color's Java engine (tavataan, MIT, upstream revision f253f3639ba969738c15a1b1035b9a5f97e47cd2). The native engine supports solid colors, two-stop horizontal gradients, Samsung SemBlurInfo where available, wallpaper/Monet palettes, ten category toggles and individual color/opacity overrides. The Compose preview simulates color/opacity; native blur is not rendered inside the preview.

The advanced engine is opt-in and only registers on Samsung devices with AOD Service build 896548000 (8.9.65.48), as required by upstream. Unsupported builds keep the existing media hook; no Samsung system app or root suite is bundled or installed. This does not force lockscreen notification visibility. Wallpaper bottom-area selection uses Samsung palette information where exposed; otherwise it approximates from the dominant color. Config is loaded through Vexaura's existing UID-checked provider; no exported config broadcasts or Settings.System writes. Disable Studio to return to the previous media accent. Restart AOD/SystemUI if switching between engines does not immediately restore OEM styles.

Scopes: Vexaura app, com.android.systemui, com.samsung.android.app.aodservice. Runtime compatibility is not proven until tested on the target device. Credits and license: legal/NowBarColor-MIT.txt.
