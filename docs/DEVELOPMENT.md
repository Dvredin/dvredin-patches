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
Node.js LTS/npm for release tooling. Exact dependencies are pinned by the Gradle
catalog/wrapper and `package-lock.json`. Use `npm ci`, not ad-hoc dependency upgrades.
Semantic Release includes an unused default npm-publishing plugin. This collection
does not enable it or publish to npm, but its bundled dependencies still belong
to the release-tool dependency audit. Keep that audit honest: even a latest npm
parent can retain vulnerable bundled libraries. Do not add ineffective overrides,
ignore failed audit exits, or claim an unreviewed exception as a passing check.

The Morphe Gradle plugin and patcher artifacts use GitHub Packages. Local access
requires approved read access (`read:packages` for a classic PAT); use protected
credential storage, not committed `gradle.properties`, shell history, or chat.
The supported environment names are `GITHUB_ACTOR` and `GITHUB_TOKEN` (or the
upstream Gradle properties `gpr.user` and `gpr.key`). Repository write authentication
alone does not prove package-registry access. CI declares `packages: read`.

```bash
./gradlew :patches:buildAndroid --no-daemon
./gradlew generatePatchesList --no-daemon
npm ci
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

## Current bootstrap evidence

The Maps correction has already been compiled from all actual Kotlin sources,
applied to the supported input, tested on Android ART, and compared with the
accepted application output. The existing Manager source was tested and accepted
on the maintainer's primary phone. No additional My Location patch was needed.
This evidence establishes the correction, not a fresh Gradle/CI build of this new
repository. A repository or release must report its own current build status.

## Reuse-first and diagnostics

Use Morphe's source/import/build/sign/install UI; do not create another patcher.
Use the official Gradle plugin and Semantic Release configuration; change the
smallest required integration point rather than writing a new release engine.
A diagnostic experiment is not production code: keep test providers, fault hooks,
tracing, APKs and raw logs out of the source bundle and public tree.
Network claims require runtime evidence. No string scan proves zero telemetry.
