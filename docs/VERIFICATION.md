# v1.0.0 verification and boundaries

Current stable is still `v1.0.0`. The later combined application was accepted,
but publication is blocked by a release-tool dependency audit, not Android
compilation. See [current update status and exact CI evidence](RELEASE_STATUS.md).
The v1.0.0 evidence below is historical and does not certify that later release.

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
