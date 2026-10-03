# Native My Location completion — unreleased correction

The local `1.1.0-dev.local.2` candidate retains the [Maps identity](MAPS_IDENTITY.md)
and corrects recenter after Home/resume. It does not replace stable `v1.0.0`.
Original supported input remains `com.google.android.apps.maps`, version
`26.36.04.973607363`. No new public patch or runtime extension is added: the
correction is a dependency of **Location provider toggle**; the catalog remains
33 patches / 1 application.

## Confirmed cause

Both the previous accepted application and the identity candidate reproducibly
centered on cold launch, then lost recenter after Home/resume. Force-stop restored
it. Normal launcher and foreground-time controls ruled out the QA launch intent
and mere elapsed foreground time.

A private, metadata-only trace established that Android GPS callbacks, location
engine emission, event delivery, My Location subscriptions and marker-model
updates continued after resume. The location permission callback also completed.
The button then entered the ImproveLocationDialog settings path, but its completion
callback no longer ran. This is a control-flow dependency, not missing GPS fixes.

The inherited provider toggle excludes fused location and redirects Play location
service binds while the source is Android. However, the stock settings path still
uses `LocationServices.getSettingsClient(...).checkLocationSettings(...)`. The
bypassed generic Play availability check selects that path even without usable
Play services. Cold-start failure can complete the request; the resumed request
can stay pending. The blocked completion callback is the boundary verified by
trace; the internal Play API-manager retry state was not exhaustively audited.

## Minimal correction

`NativeLocationSettingsPatch.kt` gates the exact original settings-method body,
register layout, constructors and fields for the supported input. It injects
sixteen instructions before that body:

- When `Shapes.playLocation()` is true, branch directly to the unchanged stock
  method. No change to working fused providers or their settings client.
- Otherwise, reuse Maps' existing synchronous Android GPS-settings implementation
  and existing completion-wrapper callback. Its GPS-only branch preserves the
  native disabled-GPS/settings dialog and does not demand Google network location.
- Select the path on each invocation, not from a cached delegate selected before
  a location-source preference changes. Do not force an optimized result, grant
  permissions, reset the provider, restart Maps or clear application data.

No trace extension, reflective logger, mock coordinates or private APK is included
in the source bundle. Original authorship, GPL/NOTICE and upstream pins remain.

## Verification performed

- All actual Kotlin patch sources compiled to JVM classes and Android DEX; catalog
  discovered as 33 public patches. Morphe Desktop applied all 33 to the supported
  original, with zero failures.
- Independent DEX inspection accepted the original gate, rejected a modified body
  and register-count mutation, and proved the original GMS tail bytecode-equivalent
  behind the sixteen-instruction guarded prefix.
- A diagnostic proof build restored the previously missing completion callback
  and recenter after Home/resume and a changed public mock GPS point.
- The final build without diagnostics was installed on a separate dedicated QA
  package. With Play services disabled it passed cold recenter, three consecutive
  Home/resume cycles, changed coordinates, screen-off/unlock, master-location
  off/on, GPS-provider off/on and cold restart. PID remained stable through the
  lifecycle cycles. Fresh hierarchies and screenshots confirmed blue-dot recenter;
  no diagnostic trace, relevant fatal exception or ANR was present.
- With GPS disabled, the application displayed its native location/settings dialog
  instead of falsely centering. Dismissing that dialog and re-enabling GPS restored
  recenter without restarting. A QA helper initially missed uppercase `IGNORE`;
  its corrected fresh-hierarchy dismissal passed. This was a test-helper defect.
- Temporary mock provider, permissions, GMS enabled state, master location and
  shell mock app-op were restored. Prior accepted packages/data were untouched.
- The final QA APK signature verified. The existing optional 16-KiB ZIP alignment
  issue remains for `libandroidx.graphics.path.so`; the tested Pixel 8 uses 4-KiB
  pages. No 16-KiB-page device compatibility claim is made.

## Build and release boundary

Local full Gradle build is still blocked before compilation: Morphe Gradle plugin
`1.3.4` is unavailable in the offline cache. The local candidate contains freshly
compiled patch code and the unchanged accepted `v1.0.0` extension. It is a verified
local behavioral candidate, not a fresh full Gradle/CI release build. No credentials
or registry scopes were changed to bypass this boundary.

Primary Pixel 10 / GrapheneOS acceptance, a fresh official release build, and
explicit publication approval remain separate gates. No remote branch, source,
upstream PR or stable release was updated by this correction.

## Owner test

Import local `1.1.0-dev.local.2`; use its Maps patches only, including **Location
provider toggle**, with **Location source: Android**. Repatch the supported original
APK, retaining **Maps identity** and the existing Manager signing key. Update the
same `io.github.dvredin.maps` installation without uninstalling or clearing data.

Pan away, press My Location, then repeat after Home/return and screen lock/unlock.
The blue dot and recenter should continue working without force-stop. Report the
exact failed transition if primary-device behavior differs.
