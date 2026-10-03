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

## Persist the selected light theme

- [ ] Make an explicit light-theme choice survive reopening Maps.
- Source: [upstream issue #11](https://github.com/bearinmindcat/morphe-patches/issues/11).
- Status: backlog; owner usually uses dark mode but wants light mode to work too.
- Reported symptom: with a dark system theme, choosing always-light Maps reverts
  to dark after reopening. This is an upstream report, not a newly reproduced
  defect in the owner's current build.
- Before implementation: reproduce it and inspect the interaction with Black
  theme and the app's theme settings. Do not claim a root cause from the report.
- Acceptance: explicit light choice persists through cold reopen and Home/resume,
  including with dark system theme; existing dark/AMOLED behavior remains available
  when selected. Define precedence between these choices before changing the UI.
