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

## Bootstrap readiness

The pre-existing Maps correction is accepted, but a new repository build and its
remote-source path have independent gates. While naming approval or package access
is pending, keep the preparation local and do not claim a public release exists.
