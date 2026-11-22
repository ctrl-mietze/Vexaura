# Vexaura releases

Every future Vexaura milestone has a **GitHub Release containing a signed, installable APK**, a SHA-256 checksum, and release notes. A source-only tag is not considered a complete app release.

## One-time signing setup

The permanent Vexaura signing key was created on the authorized Surface at:

    C:\Users\Mietze\Documents\Vexaura-Signing\vexaura-release.jks

The corresponding local configuration is excluded from Git:

    C:\Users\Mietze\Documents\Vexaura\keystore.properties

On the authorized Surface, after checking the connected GitHub account, run the **repository script**:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Configure-VexauraReleaseSigning.ps1

It reads the **existing** key, securely configures these four GitHub Actions Secrets without logging their values, **and immediately starts the signed release workflow for the current main version**:

- VEXAURA_KEYSTORE_B64
- VEXAURA_STORE_PASSWORD
- VEXAURA_KEY_ALIAS
- VEXAURA_KEY_PASSWORD

**Never publish, regenerate, attach, commit or paste the private keystore or passwords.** Back up the original key securely. A changed signing certificate prevents normal in-place Android app updates.

## Release workflow

The workflow at [vexaura-android.yml](../.github/workflows/vexaura-android.yml) implements these gates:

1. Check out the exact immutable version tag (for example, v0.2.0).
2. Ensure the tag matches the Android versionName in the Gradle source.
3. Fail if the permanent signing credentials are not configured.
4. Assemble the full **ProdRelease** build, including native audio libraries.
5. Verify the APK signature, Android package **ctrl.mietze.vexaura**, version and universal ABI support.
6. Upload the signed APK plus SHA-256 checksum as a GitHub Actions artifact.
7. Create the GitHub Release **only after all checks pass**, attaching both files.

Existing releases are never silently overwritten.

### Making a version

Update the Gradle versionName and increment versionCode. Add optional release notes under `docs/releases/vX.Y.Z.md` and **merge the changes into main**. GitHub Actions automatically builds the signed ProdRelease APK and creates the corresponding tag and GitHub Release **only after signature, package and version verification succeed**.

Existing releases are detected and skipped, never silently overwritten. Explicit tag pushes and manually dispatched releases with an existing tag are also supported. The permanent signing secrets must be configured before the release can succeed; in their absence the workflow **fails rather than publishing an incorrectly signed APK**. The one-time signing setup script launches a new, current-main release job automatically; no manual tag or failed-job rerun is needed. The App can always be built and tested from source separately.

Pull requests receive a separate ProdDebug Kotlin/Android compile check with no signing secrets.

## Experimental features

Root, LSPosed/Xposed and Samsung Now Bar behavior depends on the actual device, OS build and granted scopes. A passing APK build **does not prove** the privileged features work on every device.

No workflow or release step overwrites Android system files, replaces Samsung components or enables elevated access without user approval.
