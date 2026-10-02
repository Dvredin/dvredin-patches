# Updating upstream without losing personal fixes

## Policy

An upstream is a reviewed dependency pinned to a full commit, not a moving `main`
branch installed automatically on the phone. Discovery and adoption are separate.
A new upstream release can be inspected without changing our supported bundle.

For Maps, retain the common Git ancestry with `bearinmindcat/morphe-patches`.
Our modifications remain separate commits; root-level metadata/docs/build policy
belong to Dvredin Patches. The source lock records both the base and local addition.

## One-time clone setup

A new clone has only `origin`. Add the reviewed Maps upstream locally and prevent
accidental pushes to it; keep publication scoped to your own `origin`.

```bash
git remote add upstream https://github.com/bearinmindcat/morphe-patches.git
git remote set-url --push upstream DISABLED
```

If `upstream` already exists, inspect its URL instead of adding a second remote.

## Bounded update procedure

1. Start with clean `dev`; record the current source version and accepted artifact.
   Save a rollback tag/commit. Do not overwrite uncommitted work.
2. Fetch upstream without merging. Select a tag/revision and resolve it to a full
   commit. Inspect source and workflow changes, compatibility, licenses, upstream
   issue/PR policy, required dependencies, and whether our patch has been incorporated.
3. Create an update branch from `dev`. Merge only the selected full commit, retaining
   the merge commit. Do not merge a moving branch name or run `pull` blindly.
4. Resolve conflicts deliberately. Preserve our exact gate, proxy guard, fallback,
   package options, source branding, and publication gates. If upstream now includes
   the correction, compare its implementation and remove the duplicate deliberately;
   do not apply both copies or assume that a closed PR means equivalent behavior.
5. Update the reviewed component entry in `upstreams.json`, supported application
   versions, credits and changes relative to upstream. Do not use a version bump
   to conceal an untested application input.
6. Run the [development verification ladder](DEVELOPMENT.md). Preserve sanitized
   evidence and explicit limitations. Upstream CI is not our bundle/device acceptance.
7. Integrate into `dev`; publish a prerelease only through the approved release path.
   After primary-device acceptance, integrate into `main` and separately approve
   stable publication. Keep the last working release available for rollback.

```bash
git switch dev
git status --short
git fetch upstream --tags
# Select and inspect a full commit before substituting it here.
git show --stat UPSTREAM_COMMIT
git switch -c update/maps-BASE_VERSION
git merge --no-ff UPSTREAM_COMMIT
# Resolve, update upstreams.json, build and test before integration.
```

`UPSTREAM_COMMIT` and `BASE_VERSION` are explicit placeholders, not shell variables
resolved to the latest release. Never run a documented merge until those values
have been replaced with the reviewed identifiers.

## Other source families

Record repository, license/NOTICE, exact revision, imported paths, local changes,
supported inputs and build dependencies for every new component. Prefer native
Morphe patch APIs. A selective import or Git subtree may be appropriate for an
independent family; choose the maintained mechanism with the smallest ongoing
merge burden. Do not force every external project into one root-level Git merge.
Binary delta sets require separate public-distribution and reproducibility review;
local functional success alone does not approve redistribution of their payloads.

Future update automation may propose a reviewed update branch, but must never
merge, publish stable, install apps, or relax validation automatically. No such
scheduler or bot is part of the initial repository.
