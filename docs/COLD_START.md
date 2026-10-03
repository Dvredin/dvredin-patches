# Slow startup after application eviction

## Report and scope

On 2026-10-03 the maintainer reported a prolonged gray screen when reopening Maps,
initially described as occurring after unloading it. The subsequent clarification
identifies the actual action: **swipe Maps away from the recent-apps overview,
then reopen it**. This is task removal, not a request to clear disk cache/data or
force-stop. Task removal does not itself prove process death. Android killing a
cached process remains a distinct event; process recreation is still a working
hypothesis, not a measured PID transition.

This report is separate from the [native recenter correction](NATIVE_RECENTER.md).
The maintainer reports that the recenter error no longer occurs. That fix does
not establish a cause or a solution for the gray startup screen.

## Existing-code assessment

The existing system-Cronet patch changes the Java fallback's `createBuilder()`.
It does not remove the earlier provider enumeration performed by
`CronetEngine.Builder.createBuilderDelegate()` / `CronetProvider.getAllProviderInfos()`.
Static inspection of the local QA APK shows Google Play services providers are
considered in that enumeration before Java fallback selection. This is a candidate
startup dependency to time, **not proof that it causes this report's delay**.
The renderer, initial map requests and state restoration remain other candidates.
No measured cold-start latency or critical-path trace is available for this report.

## Required diagnosis before a runtime change

1. Use the confirmed swipe-away/reopen action and verify the exact installed
   source/options.
2. Compare a warm return with a relaunch after that action. Record PID transitions
   without clearing application data or disk caches.
3. Time the first application frame separately from the first usable map. Inspect
   the startup trace for synchronous provider discovery, service-binding waits,
   renderer initialization and initial network requests.
4. Change only the demonstrated blocking dependency. If unavailable GMS probing
   is the critical path, bypass it only when genuinely unavailable, preserve the
   working GMS path, application proxy and original fallback. Do not merely mask
   the gray screen, force the process to stay alive or remove unrelated checks.
5. Repeat cold and warm measurements and recenter lifecycle/GPS-off regression.

No runtime patch, installation, device setting change or release is authorized or
performed by this documentation update. Stable `v1.0.0` is unchanged. Local
`1.1.0-dev.local.2` remains a behavioral candidate, not a fresh official release.
