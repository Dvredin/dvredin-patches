<p align="center"><img src="docs/assets/logo.svg" width="112" alt="Dvredin Patches"></p>
<h1 align="center">Dvredin Patches</h1>
<p align="center">Personal patches · compatible with Morphe · upstream-friendly</p>
<p align="center">
  <a href="LICENSE"><img alt="License GPLv3" src="https://img.shields.io/badge/license-GPLv3-2563eb"></a>
  <a href="https://morphe.software"><img alt="For use with Morphe" src="https://img.shields.io/badge/for-Morphe-14b8a6"></a>
</p>

A personal collection of application patches maintained by [Dvredin](https://github.com/Dvredin).
This is a **modified fork of third-party patches**, not an official Morphe, Google,
or upstream-author release. Original developers retain their authorship.

## Applications

| Application | Original version | Current scope |
|---|---|---|
| Google Maps → Maps | `26.36.04.973607363` | Accepted no-Black selection with settings cleanup, optional location smoothing and GrapheneOS search compatibility: 34 patches |

Yandex applications and other patch families are not included yet.
An application version is supported only after it has been identified and tested;
there is no promise that a newer APK will work.

The accepted update adds a targeted [GrapheneOS search compatibility fix](docs/SEARCH_CRASH_FIX.md),
cleaner settings/account navigation and optional **Smooth location updates**
(OFF by default). It retains [personal Maps identity](docs/MAPS_IDENTITY.md),
[native My Location](docs/NATIVE_RECENTER.md), [native heading](docs/NATIVE_HEADING.md),
and [safe import/photo/proxy behavior](docs/PR_SAFETY_CANDIDATE.md).
Black theme and Satellite diagnostic/experimental patches are excluded.
**Satellite startup imagery remains unresolved**; keep Start app in satellite view
OFF if affected. Manual Satellite remains available.

The maintainer accepted the candidate and approved stable publication.
The generated catalog below identifies the published version; exact artifact
verification is recorded in [Verification](docs/VERIFICATION.md).

Default app name is **Maps**, installed separately as `io.github.dvredin.maps`.
It stays on the original Maps card in Manager 1.33.0 without a new Clone card;
historical installs are retained. Update the same package with the same Manager
signing key. Changing from older Ungoogled Maps packages does not migrate data
or signing identity automatically: preserve the old app and use normal backup/import.

The [backlog](docs/BACKLOG.md) keeps selected-location nearby search and theme
persistence out of scope: the maintainer reported them already working.
The separate [startup investigation](docs/COLD_START.md) remains unresolved;
no Satellite startup fix is claimed.

<!-- PATCHES_START -->
> **[v1.1.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.1.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;33 patches total
<details>
<summary><img src="docs/icons/pin-google.png" width="20" height="20" align="top"> Google Maps&nbsp;&nbsp;-&gt;&nbsp;&nbsp;<img src="docs/icons/pin-ungoogled.png" width="20" height="20" align="top"> Maps&nbsp;&nbsp;•&nbsp;&nbsp;33 patches</summary>
<br>

<p>
<img src="docs/screenshots/com.google.android.apps.maps/1-account-menu.png" width="19%" alt="Account menu" title="Account menu">
<img src="docs/screenshots/com.google.android.apps.maps/2-customization.png" width="19%" alt="Customization" title="Customization">
<img src="docs/screenshots/com.google.android.apps.maps/3-offline-maps.png" width="19%" alt="Offline maps" title="Offline maps">
<img src="docs/screenshots/com.google.android.apps.maps/4-navigation.png" width="19%" alt="Navigation" title="Navigation">
<img src="docs/screenshots/com.google.android.apps.maps/5-navigation-zoomed-out.png" width="19%" alt="Navigation zoomed out" title="Navigation zoomed out">
</p>

**Supported version(s):** 26.36.04.973607363

| Patch | Description | Options |
|----------|----------------|-----------|
| 120 refresh rate | Lifts the 60 Hz limit Maps puts on itself, on the app and on the map, so it can run at your screen's full refresh rate (such as 120 Hz). Uses more battery, most of all while navigating. Off by default: switch it on on the Customization screen. |  |
| Better offline maps | Reworks the offline area picker: zooming out really selects more instead of being shrunk to Google's size cap, the box can be resized by dragging its edges and corners, a large area is split into several downloads whose true total size is shown, and areas already downloaded are drawn on the map. Can be turned off on the Customization screen. |  |
| Black theme | AMOLED-black theme. Pins Maps' own dark mode and its separate navigation colour scheme, and remaps colour resources, drawable fills and draw-time paints so no surface is left grey. |  |
| Blue pin | Chromium-coloured flat map pin on every in-app product logo and the search bar's leading icon. |  |
| Bypass Play Services checks | Makes Maps' bundled Play services signature and availability checks always pass, so it runs re-signed and with Play services disabled or absent. |  |
| Change app name | Sets the launcher and in-app app name. | • App name |
| Customization screen | Adds a Customization row under Settings on the account sheet, with switches for the patches here that can be turned back off inside the app. Also applies Trim account menu, whose freed row builder it takes over. |  |
| Hide ads | Hides promoted map pins and "Sponsored" search result rows. |  |
| Hide explore feed | Hides the home tab's Explore feed sheet ("Local vibe"). Can be switched back on on the Customization screen. |  |
| Hide login promo | Hides the full-screen "Make it your map" page shown on first launch. |  |
| Hide navigation tabs | Hides the Explore / Contribute / You strip at the bottom of the home screen. Can be switched back on on the Customization screen. |  |
| Hide section title | Removes the "More from this app" label from the account sheet. |  |
| Hide sign-in button | Removes the "Sign in" pill from the account sheet. |  |
| Hide suggestions | Hides the row of businesses under an address on its place sheet: a preview of the address's Directory (the restaurants, shops and offices at that address). The Directory button still lists them. Can be switched off on the Customization screen. |  |
| Keep account sheet open | Returning from Settings or Customization, or tapping "Your profile", leaves the account sheet open instead of dropping back to the map. |  |
| Legacy icon | Uses the flat multicolour pin Maps had before the 2025 gradient icon as the launcher icon. |  |
| Location provider toggle | Adds a Location source choice to the Customization screen: Android's own location providers, or Google Play services' fused provider. With Android, Play services is never asked for a location. Play services is never used while it is missing or disabled, so location keeps working on phones without it. | • Default to Play services location |
| Maps identity | Installs Maps under a separate personal package while tracking it on the original Maps card in Morphe Manager 1.33. Existing tracked clones are retained. | • Maps package |
| Network location fallback | Keeps the network (Wi-Fi/cell) location provider registered when no fused location provider answers, instead of GPS-only, so a fix does not go stale indoors. |  |
| Offline saved places | Save places without a Google account, kept only on the phone: Save opens Maps' own "Place saved" sheet (Want to go, Travel plans, Starred places, Favorites, your own lists, a note), and a "Local saved" row on the account sheet rebuilds Maps' You tab -- your recent places (looked at, routed to, called, shared or saved), your lists and labels (Home, Work, your own) -- with export and import (backup file, KML, Google Takeout's Saved Places.json). |  |
| Offline timeline | Adds a Timeline to the Local saved screen: a record of where the phone has been, grouped into days and visits, kept only on the phone, with GPX export. Recording is off until switched on there; it shows a notification while it runs. |  |
| Power saving mode | Brings the Pixel-only power saving mode to every phone: while driving with navigation, press the power button and Maps shows only key information such as the next turn on a black screen. Turn it on or off in Settings > Navigation > Power saving mode. Pixels that have it built in keep Google's own version unless Customization > Power saving mode is turned on. |  |
| Proxy | Adds a Proxy screen to Customization that sends Maps' own traffic, map data included, through an HTTP proxy -- for example Orbot's (127.0.0.1:8118) to use Tor. Map data never falls back to a direct connection: if the proxy stops, Maps stops loading. Needs a recent Play services network engine (Cronet); Maps warns when it cannot take the proxy. |  |
| Rectangle shapes | Squares off rounded corners across the UI, including the two round navigation buttons. |  |
| Remove permissions | Removes permissions that only serve Google-account features or Google's data collection: background location, physical activity, contacts, microphone (voice search stops working), camera (Lens and Live View stop working), car speed, advertising ID, push messages and Google services settings. |  |
| Remove sign-in promo | Removes the "Tired of typing?" sign-in card from the search screen. |  |
| Remove telemetry | Points the Firebase Installations and Play services compliance check-ins at an unresolvable host, stops every ad impression and click ping from being sent, and deregisters Google's logging, performance-monitoring, survey and Location History libraries and the on-device federated-learning services. |  |
| Restore map data | Lets a re-signed Maps load tiles, search and routing, by sending Google's own package and certificate in the identity headers the Maps backend checks, and by degrading instead of crashing when Play services rejects the re-signed app -- including skipping a view property that fails for that reason instead of crashing the screen. |  |
| Sign-in toast | The "Sign in" pill shows a "Can't sign in" toast instead of failing silently. |  |
| Trim account menu | Removes Your Timeline, Location sharing, Your data in Maps and Help & feedback from the account sheet. |  |
| Use system Cronet fallback | Prefers Android's system HttpEngine when Maps falls back to Java Cronet. Keeps working Play services engines and the original fallback when the system provider is unavailable or an app proxy is configured. |  |
| Your profile toast | Tapping "Your profile" shows a "Can't sign in" toast instead of opening nothing. |  |
| Zoom controls in navigation | Adds +, − and reset tiles during turn-by-turn that change the navigation zoom while the camera keeps following the car. |  |

</details>

<!-- PATCHES_END -->

## What differs from upstream

The additional **Use system Cronet fallback** patch prefers Android's system
HttpEngine when Maps requests the Java Cronet fallback. A working Play services
engine is left alone. The original fallback remains available when the system
provider is unavailable or an application proxy is configured.

The implementation is also submitted as [upstream PR #16](https://github.com/bearinmindcat/morphe-patches/pull/16).
The accepted update also adopts [safe imports, imported-photo privacy and
fail-closed Cronet proxy](docs/PR_SAFETY_CANDIDATE.md) from contributions #12–14,
alongside native recenter and heading selection. Imported remote photos use
placeholders; existing local photos are retained. A required proxy that cannot
be applied blocks that Cronet engine rather than silently continuing directly.
This is not a whole-app firewall or zero-telemetry claim.

The [Maps identity profile](docs/MAPS_IDENTITY.md) changes the output name/package
separately from source branding. Use the same Manager signing key for updates
of the same package. Upstream contribution links do not imply upstream merge.

## Use with Morphe

Add the published stable source to Morphe Manager:

[**Add Dvredin Patches to Morphe**](https://morphe.software/add-source?github=Dvredin/dvredin-patches)

1. Install an official compatible [Morphe Manager](https://github.com/MorpheApp/morphe-manager/releases).
2. Add the source and select the original application of the exact supported version.
3. Select patches from this source only; do not combine overlapping Maps bundles.
4. Patch and install using Manager. Do not uninstall or clear a working application
   to bypass a signing error. Export your Manager signing key through its normal backup UI.

This repository distributes patch code, not original or patched application APKs.
Maps still accesses Google's servers for its core map, search, and routing functions.
Google account-dependent features are not supported by this profile. This is not a
claim of zero telemetry or offline availability of every feature.

## Build and contribute

Use the maintained Morphe Gradle plugin and Semantic Release integration, rather
than a custom compiler or release service. See:

- [Contribution entry point](CONTRIBUTING.md)
- [Development and verification](docs/DEVELOPMENT.md)
- [Maps backlog status](docs/BACKLOG.md)
- [Pinned upstream updates](docs/UPSTREAM_UPDATES.md)
- [Release and publication](docs/PUBLISHING.md)
- [Current combined update status](docs/RELEASE_STATUS.md)
- [Provenance and credits](docs/PROVENANCE.md)
- [Published release verification and limits](docs/VERIFICATION.md)

The current source is inherited from the original public Git history. Exact
component revisions and the local addition are recorded in [upstreams.json](upstreams.json).

## Credits and license

Thank you to **[bearinmindcat](https://github.com/bearinmindcat)** for Ungoogled Maps
and the original [bearinmind patches](https://github.com/bearinmindcat/morphe-patches),
and to **[MorpheApp](https://github.com/MorpheApp)** and the original ReVanced
contributors for the patching infrastructure and template.

Distributed under [GNU GPL v3](LICENSE), with the additional naming terms retained
in [NOTICE](NOTICE). The project's primary name is **Dvredin Patches**; Morphe is
mentioned only to describe compatibility. See [provenance](docs/PROVENANCE.md).
