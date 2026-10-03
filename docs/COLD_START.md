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
No critical-path trace of the owner's reported delay is available. The bounded
QA measurements below did not reproduce that delay.

## Dedicated-device swipe test

A Pixel 8 / Android 16 test on 2026-10-03 used the clean local recenter QA candidate
with Google Play services temporarily disabled. Actual recent-task swipes were
validated against fresh UI hierarchy and task state; the measured trigger was
not replaced with force-stop. All three swipe/reopen runs recreated the process
and were classified as COLD. Two Home/reopen controls retained the process and
were HOT.

| Scenario | Runs | Android first-window time |
|---|---:|---|
| Swipe recent task, reopen | 3 | 234–240 ms |
| Home, reopen | 2 | 91–111 ms |

These are Android `am start -W` first-window times, not fully loaded map latency.
Recorded frames separately showed a loaded map within one second of the visible
opening transition; a 2–3-second blank gray screen was not reproduced. One video's
initial Home animation makes its transition time only an upper bound. The map
viewport was at broad zoom with existing cached tiles: this does not measure
uncached requests, street-level restoration or the primary GrapheneOS device.
No later blank-map relapse or relevant package-PID fatal/ANR entry was observed.
App data/caches were retained; temporary GMS/screen settings were restored and
the dedicated device was put back to sleep. No startup runtime code was changed.
Raw device videos, logs and receipts remain private.

A primary-device swipe/reopen recording is the next evidence boundary: distinguish
the OS splash, blank Maps activity and a visible interface waiting for tiles.
The GMS/Cronet suggestion remains unconfirmed, not an established root cause.

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

No startup runtime patch, new installation or release was performed. Temporary
device-QA settings for the subsequently authorized test were restored. Stable
`v1.0.0` is unchanged. Local `1.1.0-dev.local.2` remains a behavioral candidate,
not a fresh official release.
