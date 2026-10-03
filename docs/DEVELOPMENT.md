# Development and verification

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
installed graph normally; do not ignore failed audit exits.

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

The repository's actual Gradle/CI build and Semantic Release publication now pass.
Stable v1.0.0 includes 33 patches. The downloaded source was checked against the
uploaded digest, applied to the exact supported input, and compared semantically
with the accepted application. The new GitHub remote source passed actual Manager
import/coexistence/cold launch. See [verification and limits](VERIFICATION.md).
Earlier local GitHub registry access failures are not current CI failures; keep
local package-read credentials separate from successful repository-write auth.
The original acceptance covered cold My Location, not background/resume stability.
The later reproducible failure and unreleased correction are documented in
[Native My Location completion](NATIVE_RECENTER.md). Future changes must pass their
own relevant gates; this release's evidence does not certify arbitrary new inputs.

The unreleased [Maps identity candidate](MAPS_IDENTITY.md) has its own resource,
Manager tracking and in-place rebuild evidence. Its local patch compilation
reused the unchanged accepted runtime extension; it is not a fresh full Gradle
release build or primary-device acceptance.
The later [native heading candidate](NATIVE_HEADING.md) adds only a source gate
to Maps' existing compass and preserves the stock Play selector. It has separate
forced-branch runtime, clean regression, exact-negative-input and Manager ART
loader evidence. After delivery, the maintainer reported heading working on the
primary device; this user acceptance is distinct from laboratory measurement,
exact installed-artifact readback and the full regression checklist.
Local plugin resolution is still blocked, so this is not a new official CI release.
The documentation-only acceptance update changes no patch code or source bundle
and does not authorize public push, merge or stable publication.

The later [combined safety candidate](PR_SAFETY_CANDIDATE.md) explicitly adopts
PR #12/#13/#14 while retaining #16 and accepted identity/recenter/heading behavior.
Unlike the heading-only route, it freshly compiles all production Java extension
sources. Its combined 20 extension behavior tests and 23 source/repository tests
passed, and actual Manager import plus Pixel 8 regressions are recorded separately.
Both maintained CI workflows run `./gradlew -p tests test --no-daemon`; the next
release still needs the official build and owner acceptance of this exact candidate.

## Reuse-first and diagnostics

Use Morphe's source/import/build/sign/install UI; do not create another patcher.
Use the official Gradle plugin and Semantic Release configuration; change the
smallest required integration point rather than writing a new release engine.
A diagnostic experiment is not production code: keep test providers, fault hooks,
tracing, APKs and raw logs out of the source bundle and public tree.
Network claims require runtime evidence. No string scan proves zero telemetry.
