# Combined Maps update — publication status

## Accepted Maps update

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
Official compilation, remote artifact readback and Manager source update are
publication gates, not implied by this acceptance statement.


The maintainer accepted the application built from `1.1.0-dev.local.4` and
explicitly approved publishing the current changes and updating documentation.
The accepted integration source is on `dev`. The separately authorized
[release-tool correction](RELEASE_DEPENDENCIES.md) has removed the audit blocker:
[Verify run 37129320924](https://github.com/Dvredin/dvredin-patches/actions/runs/37129320924)
passed every gate on `5c7b0692760c428800536abc5d66fed63ed4f03e`, including ordinary
frozen install, seven security regressions, audit, extension behavior tests,
official Gradle compilation and downloadable candidate upload.
Stable [v1.1.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.1.0)
was subsequently published by [Release run 37130046571](https://github.com/Dvredin/dvredin-patches/actions/runs/37130046571).
Exact tag, asset/metadata readback and actual Manager version-to-version source
update passed; see [Verification](VERIFICATION.md) for results and limits.

## Historical official build and blocker

[Verify run 37127801386](https://github.com/Dvredin/dvredin-patches/actions/runs/37127801386),
source `ccace92dcd92ae56e4211e75451a046c6c724bfd`:

- Repository/source regressions passed; 23 Python tests also passed locally.
- The isolated extension behavior regressions passed in the actual CI runner.
- Fresh official `:patches:buildAndroid` compilation passed with the maintained
  Morphe plugin and GitHub Packages access. The earlier local offline-resolution
  failure is not a current CI compilation failure.
- The overall workflow **failed** its unchanged `pnpm audit --audit-level=high`
  gate. The later artifact-upload step was skipped, so this run does not supply
  a downloadable officially verified release artifact.

The failure is [GHSA-vfj7-8cjw-p6xm](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm):
`braces <=3.0.3`, high-severity recursive AST stack exhaustion, reached through
release-tool dependencies. It is a Node.js release-tool issue, not an Android
Maps dependency or evidence of an application regression. At this check, npm's
latest version is `3.0.3` and the advisory has no first patched version.
Upstream [PR #72](https://github.com/micromatch/braces/pull/72), head
`28d440b5dd449dbf1fe6f3506cf94ecca4d02660`, proposes a fix but remains open/unmerged.
At that failed-run checkpoint it was inspected only, not adopted. The separately
authorized adoption and subsequent successful gates are recorded above.

The maintainer subsequently explicitly authorized verifying and applying the
upstream correction. The immutable reviewed commit is now installed through the
maintained pnpm override with source/integrity and direct behavioral gates.
The audit remains enabled, no advisory waiver or fake version is used, and fresh
CI has passed. The PR is still not represented as merged or published by upstream.
There is no automatic stable promotion or background release executor.

## Accepted scope and deferred work

The accepted source retains Maps identity, native recenter, native heading and
system-Cronet PR #16; it adopts safe-import/photo/proxy PR #12/#13/#14. It still
contains 33 public patches for original Google Maps `26.36.04.973607363` only.
No upstream merge or whole-app network firewall is claimed.

The maintainer withdrew selected-location nearby search (#1) and theme persistence
(#11), reporting that both already work. The [active backlog is empty](BACKLOG.md);
no additional nearby-search/theme patch, upstream issue closure or automatic
scheduling followed from this feedback.
The separate startup investigation stays deferred. Existing Maps data and
Manager signing identities were not changed during publication preparation.
