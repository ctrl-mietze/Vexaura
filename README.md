<div align="center">
  <img src=".github/assets/vexaura-mark.svg" width="116" alt="Vexaura logo" />
  <h1>V E X A U R A</h1>
  <p><strong>Feel every frequency. Explore every layer.</strong></p>
  <p>A new Android audio experience, designed to go beyond the ordinary player.</p>
  <p>
    <img src="https://img.shields.io/badge/PROJECT-VEXAURA-8B5CF6?style=for-the-badge&amp;labelColor=101018" alt="Vexaura" />
    <img src="https://img.shields.io/badge/STAGE-FOUNDATION-64748B?style=for-the-badge&amp;labelColor=101018" alt="Foundation" />
    <img src="https://img.shields.io/badge/PLATFORM-ANDROID-53D7EC?style=for-the-badge&amp;labelColor=101018" alt="Android" />
    <img src="https://img.shields.io/badge/LICENSE-GPLv3-A78BFA?style=for-the-badge&amp;labelColor=101018" alt="GPLv3" />
  </p>
</div>

---

## A different kind of audio space

**Vexaura** is an evolving Android music application with one ambition: combine a powerful listening experience, precise audio control and an optional, carefully permissioned system-integration layer.

The foundation is in place. Our distinctive experience is what we build next.

### The direction

| Experience | Vision |
| :-- | :-- |
| **Immersive playback** | Beautiful music-first navigation and a richer now-playing experience. |
| **Advanced audio** | Flexible playback behavior, audio routing and customization. |
| **Extensible architecture** | Clean boundaries between the player, UI, plugins and privileged services. |
| **Power-user tools** | Optional system-aware diagnostics and device integrations. |
| **Visual identity** | Dark-first, focused, precise and unmistakably Vexaura. |

## Beyond the sandbox

The future architecture distinguishes three Android execution contexts. These are separate privilege levels, not interchangeable app permissions.

| Context | Android UID | Future integration |
| :-- | :-- | :-- |
| **Superuser** | **0** | User-authorized root services, narrowly scoped and mediated through a privileged bridge. |
| **Shell** | **2000** | User-authorized shell integrations and diagnostics through supported interfaces. |
| **System** | **1000** | Capabilities exposed by a legitimately privileged system component, where the platform allows it. |

**Planned, not implemented:** No Vexaura-specific UID 0/2000/1000 bridge has been completed or verified. A normal app cannot simply assume UID 1000. Everyday playback should remain available without elevated permissions.

## Vexaura visual profiles

A first Vexaura-specific UI milestone is now in development:

- **Settings → Styles** with in-app previews and confirmation before applying a profile.
- **Standard** retains the original dark-first interface.
- **Veyra OLED Lila** and **Veyra OLED Lila Red** introduce tailored true-black palettes.
- **Veyra Cloud OLED Purple** and **Veyra Cloud Red Purple OLED** add relaxed, continuously drifting ambient lighting, respecting reduced-motion preferences.
- Liquid Glass is enabled by default on supported fresh installations.
- Search shares the **same floating glass island** as the other navigation tabs, including in compact mode.

These are UI changes, not a release announcement. Android build validation is performed separately; the package identity and optional system integrations remain future work.

## Foundation status

> **Early development.** The initial source has been imported. App-internal rebranding, new privileged features, validation and stable releases are still ahead.

### Roadmap

1. Establish Vexaura's package identity, UI and repeatable build.
2. Decouple playback from optional device integrations.
3. Introduce capability-based, consent-driven shell/root bridges.
4. Design system integrations around genuine Android security boundaries.
5. Test, document and release the first Vexaura build.

## Build

This is an Android/Gradle codebase. Install Android Studio, a compatible JDK and SDK, then use the included Gradle wrapper.

    git clone https://github.com/ctrl-mietze/Vexaura.git
    cd Vexaura
    ./gradlew :app:assembleDebug

On Windows use **gradlew.bat :app:assembleDebug**. Build success and device compatibility have not been verified for this initial snapshot.

## Development principles

**Device choice. Explicit permission. Real functionality.**

Elevated operations must be optional, inspectable and reversible. The player should not require root just to play music. See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines and [SECURITY.md](SECURITY.md) for security notes.

---

<div align="center">
  <strong>V E X A U R A</strong>
  <p>More than a player. A platform for what comes next.</p>
  <sub>GPL-3.0 licensed. Copyright and source notices are preserved in the source and legal directory.</sub>
</div>