# Maps backlog

These items are recorded for a later version, not implemented or scheduled.
They do not change the current combined test candidate `1.1.0-dev.local.4` or its
publication gate. No automatic worker, issue creation or release is authorized.

## Selected-location nearby search

- [ ] Add or restore search around an explicitly selected place or map point.
- Source: [upstream issue #1](https://github.com/bearinmindcat/morphe-patches/issues/1).
- Status: backlog; owner identified this as useful. No implementation has started.
- Intended behavior: choosing a place/point permits searching nearby businesses
  or categories relative to that selection, not the phone's current GPS position.
- Before implementation: inspect existing Maps entry points and maintained patch
  mechanisms, then agree on the user flow rather than assuming a new search engine.
- Acceptance: results are centered on the chosen place/point; ordinary current-
  location search, place details and recenter behavior remain available.

## Removed from the active backlog

Light-theme persistence ([upstream issue #11](https://github.com/bearinmindcat/morphe-patches/issues/11))
was withdrawn by the maintainer after reporting that theme changes already work
in the current application. No new theme correction was implemented, no upstream
issue was closed, and universal resolution of other users' reports is not claimed.
