# Combined Maps update — publication status

The maintainer accepted the application built from `1.1.0-dev.local.4` and
explicitly approved publishing the current changes and updating documentation.
The accepted integration source is on `dev`; stable `main` and the published
[v1.0.0](https://github.com/Dvredin/dvredin-patches/releases/tag/v1.0.0) remain unchanged.
**No new stable release was dispatched or published.**

## Actual official build and blocker

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
It was inspected for status and scope, not adopted or certified by this project.

The audit was not disabled, ignored, or made successful by replacing metadata.
No unmerged dependency fork, local vendor patch or advisory waiver was installed.
Dependency resolution/publication needs a separate explicit decision or an
available maintained fixed release, followed by fresh audit/build/release gates.
There is no automatic stable promotion or background release executor.

## Accepted scope and deferred work

The accepted source retains Maps identity, native recenter, native heading and
system-Cronet PR #16; it adopts safe-import/photo/proxy PR #12/#13/#14. It still
contains 33 public patches for original Google Maps `26.36.04.973607363` only.
No upstream merge or whole-app network firewall is claimed.

Selected-location nearby search (#1) and light-theme persistence (#11) remain in
[the backlog](BACKLOG.md), with no implementation or automatic scheduling.
The separate startup investigation stays deferred. Existing Maps data and
Manager signing identities were not changed during publication preparation.
