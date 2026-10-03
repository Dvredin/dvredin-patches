# Native heading for the Android location source

Status: unreleased local candidate `1.1.0-dev.local.3`; the maintainer reports
that heading works after delivery. Published stable remains `v1.0.0`.
Implementation commit: `e4e8582c3bc6ba47ac85ed88fcf560e067ae51e0`.

## Symptom and conditional defect

The blue location dot's directional sector can remain pointed in one direction
instead of following the phone. Coordinates and My Location recenter are separate
from heading; the previous [native settings correction](NATIVE_RECENTER.md) does
not certify the compass. The [startup investigation](COLD_START.md) remains deferred.

Maps chooses its orientation backend in `Laipl;->o()Z`, independently of
`Shapes.playLocation()`. The bundled availability bypass can report working GMS
when no orientation service exists. With the stock orientation feature flag
and sensor prerequisites enabled, the selector returns true and native sensor
registration is skipped. The older Google orientation request's failure callback
does not activate the native fallback. This is a concrete conditional defect,
not a verified runtime diagnosis of the maintainer's primary installation.

Unforced Pixel 8 QA already received native sensor events. To exercise the
problematic configuration, private QA explicitly forced the GMS-availability
and fused-orientation flags true. These injected flags are not observations of
the primary device and are not present in the candidate.

## Minimal change

`NativeHeadingPatch.kt` adds a five-instruction source gate to the existing
selector. In Android mode it returns false so Maps uses its own SensorManager
compass. In Play services mode it executes the complete original selector.
The stock UI restarts Maps when the source changes, rebuilding the clients.

The correction is an internal dependency of **Location provider toggle**, not a
new public patch or option: the source still contains 33 public patches.
It adds no compass engine, sensor permission, forced calibration, GPS bearing
substitution, logging or application resets. Name `Maps`, personal package
`io.github.dvredin.maps`, identity tracking, recenter and proxy behavior are unchanged.

The exact method fingerprint is
`d000355961ccd3d23d5c1c1969f07462f52068307d6d1fde95e0c78106ddd12e`.
A different or already corrected method fails closed; it is not patched by an
approximate obfuscated-class match.

## Verified local gates

- Real Kotlin patch compilation and D8 compilation succeeded. The unchanged
  runtime extension was imported byte-identically from stable `v1.0.0`.
- Morphe Desktop applied 33 patches with zero failures to the supported original
  `com.google.android.apps.maps` / `26.36.04.973607363`. Its Google signing
  certificate matched the trusted stock installation before patching.
- Tampered and already-corrected selector fixtures retained the original input
  identity and were both rejected by the heading fingerprint gate.
- The candidate's 33 patches and exact heading fingerprint loaded successfully
  using installed Manager's real ART/dexlib API. This is a loader/API check,
  not a claim that the full Manager UI patch/install flow was repeated.
- Independent DEX comparison against the clean local recenter candidate found
  exactly one changed application method: `Laipl;->o()Z`. The original Play tail
  and register layout are identical; no private diagnostic classes remain.
- Under forced problematic flags and disabled GMS, the uncorrected selector
  chose Google and received zero native/Google orientation events. The correction
  chose native, received events through Home/resume, computed a valid heading,
  and published it to the existing listener. Only validity/change counts were
  logged, never coordinates, heading values or raw sensor values.
- With Play mode and GMS enabled, the corrected selector retained Google mode
  and received Google orientation callbacks through Home/resume.
- The clean QA APK passed signature and 16-KiB ZIP-alignment verification and
  upgraded the existing recenter QA installation in place with an identical signer.
  Recenter passed cold, Home/resume, public mock movement, screen lock/unlock and
  GPS re-enable. GPS-disabled correctly did not claim a usable centered fix.
  The foreground process survived; no relevant fatal exception was found.
- Temporary GMS/location/grants/mock state was restored and the QA screen slept.

The local official Gradle attempt still cannot resolve Morphe plugin `1.3.4`.
This is not a fresh full Gradle/CI release build. A forced-branch runtime test and
stationary sensor/heading publication do not establish accurate physical rotation
or GrapheneOS behavior on their own. The maintainer subsequently reported the
heading working, providing user acceptance of the reported symptom as described
below; this does not turn the laboratory test into a physical-turn measurement.

## Maintainer acceptance

After delivery of `1.1.0-dev.local.3`, the maintainer reported that the heading
works and authorized a documentation update. The frozen-sector symptom is now
recorded as resolved by maintainer report, not awaiting initial user feedback.
The exact installed artifact/options were not independently read back, and the
report does not separately certify every Home/resume step or numerical compass
accuracy. This is acceptance of the reported behavior, not authorization to
publish, merge or promote a stable release. No application code or bundle was
changed in response to this feedback.

## Candidate and regression procedure

Local source: `Dvredin_Patches_Maps_Heading_1.1.0-dev.local.3.mpp`.
Size: 605299 bytes. SHA-256:
`6b1810069ada072af9170b2045335ed878d0556f1eb781fe9275469be7a75673`.

Import the candidate in Manager, select only this Maps source, and patch the
original supported APK with the same Maps identity and existing Manager signing
key. Update the current Maps installation without uninstalling or clearing data.
Keep Location source set to Android and Sensors permission enabled.

While standing still, rotate the phone through a substantial angle: the blue
sector should follow the phone, not GPS movement. Repeat after Home/resume.
If it remains fixed, record the actual installed candidate/source choice and
observed behavior rather than treating this conditional correction as complete
primary-device diagnosis. No stable release or public push is authorized by this
local fix or its QA result.
