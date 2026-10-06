# Development and verification

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
is published and independently read back. The maintainer accepted the search-fix
candidate and explicitly authorized both publication and recovery of the interrupted
release. The original tag/commit was preserved. Official build, source/behavior and
dependency gates, provenance attestation and the actual Manager Remote **1.1.0 to
1.2.0** update passed. See [Verification](VERIFICATION.md) for artifact evidence.


## Architecture and scope

The initial collection contains the original Maps patch family plus the native
system-Cronet correction. Kotlin patches live under `patches/src/main/kotlin`;
Java runtime code and compile-only stubs live under `extensions/extension`.
Keep existing `org.ungoogled` namespaces: they are public upstream provenance,
not an instruction to rename installed applications.

The repository inherits the public Maps Git history. `upstreams.json` records the
reviewed upstream base, the local correction, and supported application input.
Later application families get separate directories/namespaces and independently
verified provenance. Do not merge an unrelated upstream repository into the root.

## Maintained build path

Requirements: JDK 21, Android SDK platform 36, the project Gradle wrapper, and
Node.js 24 and pnpm12.8.1 for release tooling. Exact dependencies are pinned by
`packageManager`, `pnpm-lock.yaml`, and the Gradle catalog/wrapper. Use a frozen
lockfile, not ad-hoc dependency upgrades.

This collection supplies an explicit Semantic Release plugin list and never
publishes npm packages. `pnpm-workspace.yaml` uses pnpm's maintained removal
feature to exclude the unused default `@semantic-release/npm` dependency. This
removes its vulnerable bundled npm CLI instead of allowing advisory exceptions
or replacing libraries with stubs. The repository guard rejects an npm publishing
plugin or inherited configuration while this removal is active. Audit the actual
installed graph normally; do not ignore failed audit exits. The separately
authorized [release-tool security pin](RELEASE_DEPENDENCIES.md) has immutable
source/integrity and direct behavioral gates because registry audit alone cannot
certify an unpublished Git correction.

The official Semantic Release CLI uses the original Morphe `.releaserc`. The
extra npm-installing action wrapper is not used because it creates an independent,
unlocked copy of the removed dependencies. Release outputs use the already
configured official exec plugin, not a new release engine.

The Morphe Gradle plugin and patcher artifacts use GitHub Packages. Local access
requires approved read access (`read:packages` for a classic PAT); use protected
credential storage, not committed `gradle.properties`, shell history, or chat.
The supported environment names are `GITHUB_ACTOR` and `GITHUB_TOKEN` (or the
upstream Gradle properties `gpr.user` and `gpr.key`). Repository write authentication
alone does not prove package-registry access. CI declares `packages: read`.

```bash
./gradlew :patches:buildAndroid --no-daemon
./gradlew generatePatchesList --no-daemon
pnpm install --frozen-lockfile --ignore-scripts
pnpm audit --audit-level=high
python3 scripts/verify_repository.py
```

The `.mpp` output is under `patches/build/libs/`. The Gradle plugin builds both JVM
classes and Android DEX and assembles the runtime extension. Do not substitute
cached executable code for a fresh source build without explicitly reporting it
as an imported artifact; it is not a newly verified release build.

## Verification ladder

1. Inspect source diff, provenance, exact matchers, permissions, and public export.
2. Compile the actual patch/extension sources and discover the generated patch list.
3. Apply the exact generated bundle to the original supported application using
   maintained Morphe Desktop; test tampered/unsupported/already modified inputs
   where a patch's exact gate requires rejection.
4. Inspect the resulting method/resource changes. For a transport-only fix, require
   only the intended method changes; compare behavior/code with the accepted baseline.
5. In actual Manager, import the source alongside other sources, inspect app/patch
   counts, disable/update/reimport, and cold-start Home. Loader success is not UI success.
6. Generate/install through Manager on an authorized test device. Verify signature,
   alignment, installed package, foreground/PID, relevant fatal/ANR markers, and the
   changed own-UI behavior. Do not confuse installer, lockscreen, and app screenshots.
7. Separately verify an in-place upgrade with persisted data and the existing signer.
   If unavailable, label this gate unverified; do not uninstall to manufacture a pass.
8. Obtain maintainer acceptance on the primary device before stable promotion.

A metadata-only source rebrand does not require repeating unchanged application
behavior if executable entries are proven byte-identical to the accepted artifact.
It still requires actual Manager source/UI/coexistence checks. A failed GUI attempt
is not a passed gate; inspect the resulting state before retrying.

## Current release evidence

Stable **v1.2.0** is published from the unchanged accepted release tag. The
explicitly approved [recovery path](PUBLISHING.md) completed the interrupted
publication using the maintained Gradle build. Source/behavior/dependency gates,
frozen install, audit, attestation and semantic-note restoration passed. The
independently downloaded asset matches its uploaded digest and all 125 executable
entries of the accepted official candidate, which applied all 34 patches to the
supported original input. Actual Manager Remote update from 1.1.0 to 1.2.0,
coexistence and cold Home passed. No Maps reinstall was part of this source update.
See [verification and explicit limits](VERIFICATION.md).
Earlier local GitHub registry access failures are not current CI failures; keep
local package-read credentials separate from successful repository-write auth.
The earlier v1.0.0 acceptance covered cold My Location, not background/resume.
v1.1.0 includes the separately tested [native completion](NATIVE_RECENTER.md),
heading, identity and import/privacy/proxy changes. Future changes need their own
gates; this release does not certify arbitrary inputs or exhaustive proxy traffic.

### Historical local candidate evidence

The [Maps identity candidate](MAPS_IDENTITY.md) had its own resource,
Manager tracking and in-place rebuild evidence. Its local patch compilation
reused the unchanged accepted runtime extension; it is not a fresh full Gradle
release build or primary-device acceptance.
The later [native heading candidate](NATIVE_HEADING.md) adds only a source gate
to Maps' existing compass and preserves the stock Play selector. It has separate
forced-branch runtime, clean regression, exact-negative-input and Manager ART
loader evidence. After delivery, the maintainer reported heading working on the
primary device; this user acceptance is distinct from laboratory measurement,
exact installed-artifact readback and the full regression checklist.
That heading-only local route could not resolve the plugin offline and was not
an official release build. Its documentation-only acceptance did not itself
authorize publication. Subsequent combined acceptance, official CI and explicit
publication approval are separate evidence, now recorded for v1.1.0.

The later [combined safety candidate](PR_SAFETY_CANDIDATE.md) explicitly adopts
PR #12/#13/#14 while retaining #16 and accepted identity/recenter/heading behavior.
Unlike the heading-only route, it freshly compiles all production Java extension
sources. Its combined 20 extension behavior tests and 23 source/repository tests
passed, and actual Manager import plus Pixel 8 regressions are recorded separately.
Both maintained CI workflows run `./gradlew -p tests test --no-daemon`. The
maintainer subsequently accepted this exact candidate and approved publication.
The first official Verify passed compilation but failed the release-tool audit.
After the separately approved security pin, fresh Verify and stable Release passed.
That historical update became v1.1.0. See [status history and exact CI evidence](RELEASE_STATUS.md);
the initial audit failure and local registry failure are not current blockers.

## Reuse-first and diagnostics

Use Morphe's source/import/build/sign/install UI; do not create another patcher.
Use the official Gradle plugin and Semantic Release configuration; change the
smallest required integration point rather than writing a new release engine.
A diagnostic experiment is not production code: keep test providers, fault hooks,
tracing, APKs and raw logs out of the source bundle and public tree.
Network claims require runtime evidence. No string scan proves zero telemetry.
