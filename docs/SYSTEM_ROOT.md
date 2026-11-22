# Vexaura · System & Root

## Architecture and permission boundaries

Vexaura never assumes that an installed root manager has granted UID 0. The System & Root page checks the effective identity by executing **su -c id -u** from a short-lived process with a six-second timeout. No privileged write command is issued, and failure, denial or timeout is shown instead of a fake green check.

LSPosed/Xposed is a separate capability. An in-process module hook replaces the return value of XposedModuleStatus.isActive(); an inactive framework or a module that is not in the app's scope therefore reports **not active**. Enabling the module is a user operation in the framework manager, not something Vexaura secretly grants itself.

### Suggested Xposed scopes

- ctrl.mietze.vexaura — confirm that the Vexaura module is actually loaded.
- com.android.systemui — allow the Samsung media Now Bar hook, where the target class is available.
- com.samsung.android.app.aodservice — allow the alternative Samsung FaceWidget/Now Bar host.

Enable only scopes supported by the installed framework. Reboot/restart the target processes after changing module scopes. Never force-close SystemUI during playback.

## Audio

The two Sound Settings sliders are **actual session-local Android DSP effects**:

| Slider | Android API | Applied to | Range |
| --- | --- | --- | --- |
| Bass Boost | BassBoost.setStrength | Current Vexaura audio session | 0–1000 |
| Audio Gain | LoudnessEnhancer.setTargetGain | Current Vexaura audio session | 0–1200 mB (0–12 dB) |

The audio service tracks the Media3 audio session, updates effects when preferences change and releases them when the service closes. The effects never attach to audio session **0** (global output mix). Unsupported effects are shown as unavailable, and no system audio file is overwritten. They cannot guarantee output above hardware or OEM safety limits; headphone and hearing safety still matters. Bass is a dedicated effect, **not** the manual multiband equalizer.

Bit-perfect modes disable both DSP effects because they modify samples. High-quality source selection and output-format settings reuse Vexaura's real existing playback configuration; **32-bit float output is a request, not proof of hardware bit-perfect playback**.

## Home-screen widgets

Vexaura already ships three functional media widget providers: Compact (square), Wide and Capsule. System & Root exposes direct Android widget pin requests for all three. Android's launcher always decides whether to show the approval prompt and whether to place the widget.

Lock-screen widgets are **not** universally exposed through the standard Android pin API. Vexaura does not claim that third-party widgets can be placed on every Samsung lock screen.

## Samsung media Now Bar

The optional legacy-Xposed hook is restricted to the known Samsung **NowBarMediaCardView**. It hooks Samsung's media-card constructors and certain known update methods if those members exist. It changes **in-memory background tint only**, not Samsung packages, system files, persistent resources, signatures or protected permissions.

The app and Samsung SystemUI exchange a limited color setting through the ContentProvider authority:

    content://ctrl.mietze.vexaura.systembridge/nowbar

The provider validates the Android **binder calling UID** and corresponding installed package identities before returning the color. Other apps are denied. No secrets, command execution, shell access or filesystem access are exposed through this bridge.

A heartbeat is written only when the host actually receives the media-card hook. App-scope activation alone does **not** prove a SystemUI/Now Bar hook is live. A missing heartbeat means **not verified**, not silently enabled.

This uses private OEM classes. Different One UI versions can rename or omit them; the module simply does not hook where unavailable. For initial device testing, use one Samsung device at a time and keep an easy way to disable the module in the LSPosed manager.

## Release policy

Every user-facing Vexaura milestone gets a tagged GitHub Release with an installable signed APK, a changelog and the exact scope of completed vs experimental functionality. The Android app uses the package **ctrl.mietze.vexaura**, separate from the source application's package. Keep the release signing key safe; users cannot update an installed APK from a different signing key without uninstalling.

## Not implemented yet

There is **no** UID-1000 identity spoofing, no generic system-server hook catalogue, no system-wide arbitrary amplifier limit bypass and no universal lock-screen widget injector. Further controls require individual Android/One UI version gates and tests, not a promise made by a menu label.
