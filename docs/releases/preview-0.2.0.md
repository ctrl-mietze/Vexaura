# Vexaura 0.2.0 — Development Preview

This is a **testable Android APK, not the final production-signed v0.2.0 release**. It installs separately as `ctrl.mietze.vexaura.dev` so it does not replace the production Vexaura package or any pre-existing media app.

## Included

- Original Vexaura-style music interface with Liquid Glass navigation and integrated Search.
- Settings → Styles with Standard, Veyra OLED Purple, Purple Red and slow animated Cloud presets.
- Settings → System & Root with a real superuser (`su -c id -u`) check and LSPosed/Xposed status.
- Session-local BassBoost and digital gain controls, plus existing source quality and USB/audio routing options.
- Home-screen player widget shortcuts (Compact, Wide and Capsule).
- Experimental Samsung Now Bar color controls. Their effectiveness requires LSPosed/Xposed scope setup and compatible One UI internals.

## Essential limitations

- This preview uses **Android's temporary debug signing key**. It cannot update the future production-signed `ctrl.mietze.vexaura` build. Future debug previews generated on different runners may need a clean install.
- **Now Bar hooks are experimental**, not validated on a particular Samsung/One UI build.
- Root access is not granted automatically: authorize `ctrl.mietze.vexaura.dev` in the root manager before using UID-0 features. The package may also need to be added to the LSPosed scope manually.
- The BassBoost and digital loudness processing affect only this app's audio session and cannot guarantee exceeding hardware output limits.
- The Wear OS companion APK is omitted from this temporary preview to keep builds isolated.
- Android installer may warn about apps outside the Play Store; only install APKs from this official repository.

**Production release:** Vexaura v0.2.0 requires uploading the pre-existing permanent signing key on the Surface to GitHub Actions' encrypted secrets. That is a separate, still-unfinished step; do not confuse this preview with a final signed release.

## Install

Download `Vexaura-0.2.0-PREVIEW-debug.apk` below and install it through Android's standard package installer. It should be visible as **Vexaura Dev**.
