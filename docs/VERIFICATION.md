# Published release verification and boundaries

## Current stable v1.2.0

The maintainer accepted the current no-Black candidate and explicitly authorized
stable publication. The runtime includes the targeted [GrapheneOS search fix](SEARCH_CRASH_FIX.md),
accepted settings/profile cleanup with original transitions, and optional
**Smooth location updates** (OFF by default). The release catalog contains
**34 patches**. Black theme, Satellite diagnostics and experimental Satellite
restoration are not shipped. Satellite startup imagery remains unresolved;
manual Satellite is retained and startup OFF remains the recommended workaround.

Local acceptance evidence: 51 extension tests, 29 repository regressions, controlled
ART baseline/fixed checks, ordinary search/upgrade checks and maintainer feedback.
The accepted-source manifest guards exact runtime continuity; clean release history
contains the reviewed net changes rather than private diagnostic sessions.
Stable [v1.2.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.2.0)
is published, not a draft or prerelease. The original Semantic Release attempt
created its tag before GitHub rejected the channel-note push. After explicit
recovery approval, the guarded [recovery workflow](PUBLISHING.md) completed as
[run 37388960475](https://github.com/Dvredin/dvredin-patches/actions/runs/37388960475).
The tag was not moved, no new version was invented and no published asset replaced.

- Original immutable source/tag: `f529a1366af9242bab3b658ffd3d0e3435fbd022`.
- Fresh maintained Gradle Android build, source/behavior regressions, frozen
  dependency installation, security regressions and ordinary audit passed.
  Recovery guards have 12 focused failure/idempotency regressions. The catalog
  presentation test now reads the actual generated count instead of assuming 33.
- Published asset `patches-1.2.0.mpp`: **578652 bytes**, SHA-256
  `e6c5020e2ad53098ccdf5caef5515e9bb739333e42d32d48ccc2c01709fc2caa`.
  An unauthenticated download matches the GitHub uploaded-asset size/digest.
- Every **125 executable class/DEX/extension entry** is byte-identical to the
  accepted official CI candidate. Only the top-level bundle manifest differs
  (version/timestamp). That candidate passed actual 34/34 patch application and
  the accepted runtime/closed-code equivalence gates. GPL/NOTICE and source
  references remain; no application APK, key or private diagnostic payload shipped.
- Remote generated catalog has 34 patches including **Fix GrapheneOS search crash**,
  excluding Black/diagnostics. Its download URL resolves to the exact verified asset.
- Provenance attestation generation passed and its remote statement names the
  published SHA-256. The missing default-channel git note was restored and read
  back. Independent local cryptographic verification of the attestation is not claimed.
- Actual Manager Remote source **1.1.0 to 1.2.0** update passed on a dedicated Pixel:
  34 patches, one app, nine sources retained, Disabled and prerelease OFF preserved.
  Cold Home remained foreground with a stable PID and no relevant fatal/duplicate-key
  exception. Temporary device settings were restored and the device put to sleep.

This recovery did not change Maps runtime, signing options or security settings,
and did not repeat full app patch/sign/install on the owner's phone. Primary-device
acceptance is the owner's local.16 report; the official bundle's executable parity
connects it to the tested candidate. Satellite startup remains separately unresolved.


## Previous stable v1.1.0

Stable [v1.1.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.1.0)
is published, not a prerelease or local candidate. The maintainer accepted the
combined `.local.4` application and explicitly approved publication and the
reviewed [release-tool dependency correction](RELEASE_DEPENDENCIES.md).

- Source/tag: `9959031b61b58b2c39f1439de917804df60c1cb3`.
- [Official Verify](https://github.com/Dvredin/dvredin-patches/actions/runs/37129320924)
  and [Semantic Release](https://github.com/Dvredin/dvredin-patches/actions/runs/37130046571)
  passed, including fresh Gradle compilation, extension behavior regressions,
  frozen install, unchanged audit, seven dependency-source/behavior checks and
  the publication/provenance-attestation steps.
- Asset `patches-1.1.0.mpp`: **572798 bytes**, SHA-256
  `693621114ff3ff858f7b68c83b561591f6b2b636ccc82eaa07046c0881873209`.
  The independently downloaded size/hash equal the uploaded asset receipt.
  ZIP integrity, manifest/name/version, GPL/NOTICE and source reference passed;
  no APK, signing key or private diagnostic payload is included.
- All **124 executable class/DEX/extension archive entries** exactly match the
  officially built and tested CI candidate. Generated source metadata points to
  this repository's exact stable asset. All 33 patches applied to supported
  original Google Maps `26.36.04.973607363`, with zero failures.
- Independent generated-APK verification found **355841 closed Maps method
  shapes** identical to the accepted heading candidate, retaining native heading,
  recenter and Cronet. Ten selected PR implementation methods match the fresh
  official extension in the generated APK. The official AGP R shell was proved
  empty/unreferenced. The official extension is not claimed byte-identical to
  manual `.local.4`: compiler-generated lambda classes differ between build routes.
- Real Manager `1.33.0` on Pixel 8/Android 16 updated the **existing GitHub Remote
  source from 1.0.0 to 1.1.0**, showing 33 patches/one app. The eight existing
  sources, disabled state and local candidates were retained. Cold Home remained
  foreground with a stable PID and no relevant fatal/duplicate-key exception.
  This is a real source-version update, not an application installation test.

Default output name/package: **Maps** / `io.github.dvredin.maps`; the application
keeps the original Maps version `26.36.04.973607363`. Source-bundle version `1.1.0`
is separate. Use the same Manager signing key for same-package updates; older
Ungoogled Maps packages have separate data, not automatic migration.

The publication task did not repeat full Manager patch/sign/install or an in-place
application upgrade using this official artifact. Primary-device acceptance is
owner feedback on `.local.4`, not an independently read-back install or quantitative
compass/proxy audit. Existing device evidence is documented in the candidate guides.
Attestation generation passed; independent local cryptographic attestation
verification is not claimed. Artifact integrity was independently checked.

The [active backlog is empty](BACKLOG.md). The maintainer withdrew both nearby
search and theme requests after reporting that the existing behavior works.
No additional patch or upstream issue closure is claimed. No startup
fix, exhaustive traffic audit, arbitrary future Maps/Android compatibility or
automatic release/background feature worker is claimed.

## Historical v1.0.0 evidence

The following evidence describes the earlier release only.

Stable source release: [v1.0.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.0.0).
Input: original Google Maps `26.36.04.973607363` only. Output app/package options
remain the original upstream defaults; source branding does not change them.

## Performed checks

- [CI verification](https://github.com/Dvredin/dvredin-patches/actions/runs/37043451013):
  actual Gradle patch/extension build, frozen dependency installation, normal
  dependency audit with no known vulnerabilities, repository checks and regressions.
- [Official Semantic Release run](https://github.com/Dvredin/dvredin-patches/actions/runs/37043767653):
  stable publication and build-provenance attestation steps succeeded.
- Downloaded exact release: ZIP integrity, manifest, supported input, licenses,
  NOTICE, corresponding-source reference, absence of APK/credential/log payloads,
  and size/SHA256 equality with GitHub's uploaded asset digest passed.
- All 33 actual patches applied to the original input and rebuilt successfully.
- Semantic application comparison: the native exact matcher accepts the original
  and rejects the modified fallback; generated fallback instructions match the
  accepted implementation. All 356987 runtime method digests equal the maintainer's
  accepted application baseline. All 122 compiled patch/DEX/extension entries in
  the published bundle equal the tested CI candidate; non-executable notices and
  source metadata are the additional packaging changes.
- Actual Manager1.33.0 on a stock Android16 Pixel8 imported both the CI local source
  and the published GitHub remote source. The remote source displays the original
  D icon, version1.0.0, 33 patches and one app. Both coexist with retained upstream
  and historical sources. Cold Home restarts retained a stable foreground/PID
  without duplicate-key/fatal markers. The existing current-version update action
  was exercised; this does not claim a future version-to-version update test.
- The runtime correction was previously accepted on the maintainer's primary
  device. No new My Location patch, extra feature, or unrelated upstream PR fix
  is part of this release.

## Limits

This release distributes a patch source, not a newly signed application APK.
There was no new app install, primary-device data change, or signing-key migration
in the collection-publication task. Prior application behavior evidence is reused
because runtime methods are proven equivalent, not because a build flag says so.
An independent fresh in-place application upgrade from this collection's bundle
is not claimed; retain Manager signing continuity.

The attestation-producing workflow passed. This report does not claim an
independent local cryptographic verification of that attestation. Artifact
integrity was independently checked against the exact uploaded asset digest.

No exhaustive network/telemetry audit, arbitrary future Maps version, or broader
Android/platform compatibility is certified. No automatic upstream adoption,
stable publication, app install, update scheduler, or agent daemon is enabled.
