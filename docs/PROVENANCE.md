# Provenance, licenses and attribution

## Initial component

- Original project: [bearinmind patches](https://github.com/bearinmindcat/morphe-patches).
- Original author: [bearinmindcat](https://github.com/bearinmindcat).
- Reviewed base: `v1.3.0`, commit `35421159e2149d1ca972ffd0f16b79666569035c`.
- Local addition: native system-Cronet fallback, commit
  `666135f8fccb6404bffba8a74d58c41d50309ebd`, submitted as
  [upstream PR #16](https://github.com/bearinmindcat/morphe-patches/pull/16).
- Source file: `patches/src/main/kotlin/org/ungoogled/patches/maps/network/SystemCronetFallbackPatch.kt`.
- The prior import, external-photo, and proxy patches are not part of this collection's
  initial executable changes. Source branding is separate from app behavior.

The repository preserves the original public Git ancestry and original source
copyright. Our README, maintenance contract and publication policy describe this
modified derivative honestly rather than replacing authorship.

## Accepted follow-up changes

The accepted settings cleanup, optional location smoothing and narrow GrapheneOS
search compatibility correction are promoted as one reviewed net-change commit.
The original public ancestry and upstream notices remain intact; private
investigation history and raw device logs are not published. Current source hashes
and public commits are pinned in `upstreams.json`. Black theme source is retained
for provenance but excluded from the release build to match the accepted selection.

## Infrastructure

MorpheApp's patcher, Gradle plugin, official patches template and Semantic Release
integration are maintained upstream infrastructure. Original Morphe/ReVanced
contributors retain their copyright and license/NOTICE terms. Links:

- https://github.com/MorpheApp/morphe-patches-template
- https://github.com/MorpheApp/morphe-patches-gradle-plugin
- https://github.com/MorpheApp/morphe-patcher
- https://github.com/MorpheApp/morphe-manager

## Naming and redistribution

Preserve [LICENSE](../LICENSE) and [NOTICE](../NOTICE). The bundle includes them
as root resources, because the maintained Gradle plugin intentionally excludes
`META-INF/LICENSE*` and `META-INF/NOTICE*` when filtering dependency metadata. The inherited NOTICE forbids
Morphe as a derivative project's primary name or part of the project name; it is
used here only as a secondary compatibility reference. The primary name is
**Dvredin Patches** with distinct original branding. Application-level names and
public upstream namespaces are not silently renamed by source branding.

Review each additional source's actual terms before import. Do not assign MIT to
GPL-derived code, discard notices, claim third-party implementation as original,
or assume that a public GitHub repository grants rights to every bundled binary.
Record corresponding sources for every distributed executable component.

No original/patched APK, account material, signing key or private diagnostic data
belongs in the public source or release. Existing screenshots/icons inherited
from the original repository retain their upstream provenance; they are not
represented as new maintainer-device screenshots.
