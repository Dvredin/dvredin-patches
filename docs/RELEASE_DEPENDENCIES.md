# Pinned release-tool security correction

The maintainer authorized checking and applying the upstream correction to remove
the release blocker. Android patch and extension sources are unchanged by this
release-tool dependency change.

## Reviewed component

- Component: `braces`, maintained source [micromatch/braces](https://github.com/micromatch/braces).
- Advisory: [GHSA-vfj7-8cjw-p6xm](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm).
- Upstream correction: [PR #72](https://github.com/micromatch/braces/pull/72),
  **open/unmerged at review**, not represented as an upstream release.
- Base: `e53730e6f935498326c72d768889ac194eedc0e0`.
- Reviewed pin: `28d440b5dd449dbf1fe6f3506cf94ecca4d02660`.
- License: [MIT at that revision](https://github.com/micromatch/braces/blob/28d440b5dd449dbf1fe6f3506cf94ecca4d02660/LICENSE).
- The original package version stays `3.0.3`; no metadata rename is used to
  pretend npm published a fixed version.

The source caps parse nesting and each recursive compile/expand/stringify walker
at depth 100, including caller-supplied larger or infinite limits. Expansion also
rejects cyclic parent chains. Normal brace/range expansion stays covered by tests.
The pin is a direct development dependency and a transitive override; the frozen
lock records the exact Git tarball SHA-512 integrity. No Android runtime dependency,
new release engine, vendor library or application feature is introduced.

## Verification and audit limitations

A vulnerable registry `3.0.3` control reproduced `RangeError: Maximum call stack
size exceeded` for deeply nested parenthesis and branching-brace patterns below
10,000 characters. The correction rejects these at the documented depth boundary.
The upstream test suite passed: **904 tests**. The project's seven Node regression
checks verify all seven production-source file hashes, active micromatch resolution,
ordinary globs, deep input, independent AST guards, option ceilings and parent cycles.
A bounded independent static review found no blocking defect for string patterns.
Malformed externally supplied ASTs (for example cyclic arrays in a node value)
are outside this correction and not accepted by the release-tool string-glob flow.
Both Verify and Release run these tests on the installed frozen dependency graph.

The normal `pnpm audit --audit-level=high` remains enabled with no advisory waiver.
It passed locally after installing this graph. **Registry audit cannot certify an
unpublished Git-source correction by version number**; source review, immutable
pin/integrity and direct regression tests are separate necessary evidence. A passing
audit alone must not be used as proof that this Git implementation fixes the advisory.

pnpm blocks Git-sourced transitive packages during fresh resolution by default.
The maintainer-authorized reviewed pin was added to the lock in one bounded local
resolution using `--config.blockExoticSubdeps=false`. That flag is **not stored**
in project configuration or either CI workflow. Ordinary frozen install then
passed with pnpm's normal defaults. Future regeneration must review every changed
source and integrity; do not turn off the default globally or copy this exception
into routine CI installs. No package lifecycle/build scripts were allowed.

## Maintenance boundary

Prefer a maintained fixed npm version once available, after comparing its code
and re-running source/behavior/audit gates. Do not track moving PR/main references
or infer a merge from an issue closure. This reviewed commit is not blanket
permission to adopt arbitrary unpublished dependencies or relax security gates.
