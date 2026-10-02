# Contributing to Dvredin Patches

Start with [Development](docs/DEVELOPMENT.md), then the relevant
[upstream-update](docs/UPSTREAM_UPDATES.md) or [publication](docs/PUBLISHING.md) guide.
These documents are the shared contract for maintainers and development tools.

## Non-negotiable rules

- Prefer existing Morphe APIs, the official template, and maintained libraries.
  Explain a concrete gap before introducing custom infrastructure.
- Change one application behavior at a time. Preserve unrelated work and a known
  good source/application for rollback. A build alone is not functional acceptance.
- Preserve exact supported versions, source identity gates, package identity, and
  signing continuity. Do not relax a matcher just to accept an untested APK.
- Keep upstream code and personal changes traceable. Update `upstreams.json` only
  after inspecting the diff and verifying the chosen revision.
- Do not replace a failed build or test with plausible output or a success flag.
  Continue necessary fixes; stop only at an unavailable external prerequisite,
  a protected operation, or an explicit maintainer stop.
- Never commit APKs, XAPKs, account data, device logs, keys, credentials, build
  caches, or private operational notes. Public evidence must be redacted.
- Keep original copyright, license, NOTICE, and attribution. A copied component
  requires license/provenance review before it enters a public source or release.
- No automatic upstream-to-stable promotion. No public issue/comment or upstream
  PR is implied by a local fix. Stable publication needs explicit maintainer approval.
- Do not add application families, rename installed packages, clear app data, or
  introduce a new scheduler/patcher/UI as incidental cleanup.

## Continuity between tasks

1. Read README, this contract, `upstreams.json` and the relevant development,
   upstream-update, publication and verification documents. Inspect the current
   branch, scoped diff and actual remote/release state before acting on an old note.
2. Separate three kinds of information: project source/build truth belongs here;
   private device acceptance, exact internal evidence and task status belong in
   the maintainer's private project record; reusable procedures belong in the
   development tooling's generic workflow documentation, without project identifiers.
3. Choose one bounded improvement from the current explicit request. Record its
   acceptance criteria, baseline and allowed public/device actions before changing
   code. Future intent or a successful source-add confirmation is not another task.
4. At completion or a necessary pause, preserve the exact source/branch, changed
   behavior, real checks and limitations, outstanding prerequisite or next-action
   boundary, and whether an executor is running. Preserve rollback and user data.
5. Update the narrowest existing documents, validate their local links and public
   contents, and distinguish docs-only commits from application changes or release
   publication. Do not create a new stable version just to save a work summary.

The normal cycle remains: reviewed baseline → isolated change → affected tests
and real application/source checks → maintainer acceptance → separately approved
publication. Use existing Morphe infrastructure; do not add a project-specific
scheduler, patcher or orchestration framework to enforce this contract.

## Commits and branches

Use small Conventional Commits (`fix:`, `feat:`, `chore:`, `docs:`, `test:`).
`dev` holds integration/test work; `main` is the accepted source. Use short-lived
`fix/...`, `feat/...`, or `update/...` branches for bounded changes.
Upstream merge commits are retained: do not squash away the ancestry used to track
upstream revisions. Own coherent feature/fix commits may be squashed before integration.

For a bug, provide the reproducing state, exact original application/source versions,
expected behavior, actual behavior, changed files, tests run, and remaining limitations.
Never upload private application data to make a bug report convenient.
