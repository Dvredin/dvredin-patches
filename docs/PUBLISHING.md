# Release and publication

## Scope and channels

`dev` is integration/testing; Semantic Release publishes it as a prerelease.
`main` is accepted source; Semantic Release publishes it as stable. Publication is
an explicit workflow dispatch, not a side effect of every push or documentation edit.
The initial repository publication does not grant unlimited future stable releases.

Use the maintained Morphe template's `.releaserc` and the official Semantic Release
CLI, with the project's explicitly dispatched `release.yml`. The pinned pnpm
dependency graph removes only the unused npm publisher; it does not bypass the
normal dependency audit or replace the release engine. They produce the `.mpp`, catalog, changelog and bundle metadata.
Do not invent another release service or hand-edit generated catalog/metadata to
make a failed build look publishable. Semantic Release tags/releases are append-only:
fix a bad release with a new version, never force-push published release history.

## Before publication

1. Confirm exact repository, source commit, branch, channel and maintainer approval.
   `main` requires explicit primary-device acceptance of the relevant behavior.
2. Pass the applicable [development gates](DEVELOPMENT.md), including actual Manager
   source import. Report unavailable device/upgrade/network gates honestly.
3. Verify provenance/license/NOTICE, fresh build output, patch/application counts,
   supported input declarations and unchanged package/signing options.
4. Scan the tracked public tree and release archive for secrets, keys, original or
   patched APKs/XAPKs, raw device logs, caches and private host/account information.
   The repository verifier is a baseline check, not a substitute for diff review.
5. Inspect README, changelog/release notes and source metadata as public artifacts.
   Original authors receive credit; the source must not imply Morphe endorsement.
6. Ensure build/CI authentication can read required public GitHub Packages. Store
   credentials only using protected secret storage; never widen auth silently.

## Dispatch the maintained release workflow

Select `dev` for a prerelease or `main` for stable. Stable dispatch requires the
explicit `owner_verified=true` input in addition to the maintainer's real approval.
This input is an operational guard, not proof that a device test happened.

```bash
# Test channel after the required candidate checks.
gh workflow run release.yml --ref dev -f owner_verified=false
# Stable only after explicit acceptance and all applicable gates.
gh workflow run release.yml --ref main -f owner_verified=true
```

Follow the concrete run to completion. If compilation, publication or attestation
fails, inspect which external objects actually exist before retrying. A successful
workflow launch is not a published release. A failed attestation after publication
is not an unpublished release.

## Recover an existing, partially published stable tag

If Semantic Release pushed the accepted tag but failed before uploading its asset,
a plain rerun can consider that tag the last release and skip publication. Inspect
the exact tag, generated metadata, release and assets first. Never delete/move the
tag, guess another version or replace a published asset.

After explicit maintainer recovery approval, use `recover-release.yml` from `main`
with `owner_verified=true`, the existing stable `tag`, and its approved full
`expected_sha`. Resolve that commit from the **remote repository**, not an inherited
local/upstream tag with the same version. This is a narrow repair adapter around
GitHub CLI and git, not another versioning engine: the ordinary Semantic Release
workflow and configuration remain unchanged.

Recovery checks out the exact tagged source, validates its already generated
version/catalog/download URL, runs the existing source, behavior and dependency
gates, and uses the maintained Gradle Android build. It stages a draft and its exact
asset without clobbering existing files, attests that artifact, publishes/readbacks
the same version, then restores the missing default-channel semantic note without
force or overwriting a conflicting note. Existing mismatched assets/notes fail
closed for review. A successful prior phase is not repeated unnecessarily; a
partially failed recovery must be inspected before rerunning.

The recovery artifact is retained even if a subsequent network operation fails.
Finish the same remote Manager upgrade and cleanup gates below. Source/runtime
acceptance remains attached to the immutable release commit, not the later commit
that added the recovery workflow.

## Read back the exact result

Verify the remote source commit/tag, release `draft`/`prerelease` flags, release
notes, asset names/size/hash and downloaded `.mpp` manifest/content. Fetch the
remote `patches-bundle.json` and confirm its version/download URL points to this
repository's exact uploaded asset, never an inherited upstream URL.

Add the GitHub source in a real compatible Manager and verify remote discovery,
download/import, patch/application counts, coexistence and cold launch. The older
local-source import test does not prove the new remote update path.

Only report a stable release after these checks. Publish patch sources and `.mpp`
files, not vendor or patched application APKs and not Manager signing keys.
Preserve signing continuity when the maintainer generates an application update.

## Initial publication completed

Stable [v1.0.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.0.0) passed
its own build, dependency audit, artifact readback and actual Manager remote-source
gates. See [verification and limits](VERIFICATION.md). Earlier preparation blockers
were resolved; do not substitute those historical failures for current release state.
This one-time approval does not authorize future stable dispatches automatically.

## Accepted combined update

The maintainer accepted `.local.4`, approved publication, and then explicitly
authorized the reviewed dependency correction after the initial audit blocker.
The maintained frozen install, source/behavior regression gate and audit passed,
followed by official stable Release and attestation. **v1.1.0 is published**;
asset/metadata readback and actual Manager Remote 1.0.0-to-1.1.0 update passed.
See [verification and limits](VERIFICATION.md) and [status history](RELEASE_STATUS.md).
Do not recreate the historical blocker or treat approval as an automatic release loop.
