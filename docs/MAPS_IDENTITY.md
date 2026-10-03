# Personal Maps identity and one Manager card

This is an unreleased candidate profile, not a change to the accepted v1.0.0 source.

## Requested behavior

- Visible launcher/app-switcher name: **Maps**.
- Default separate Android package: `io.github.dvredin.maps`.
- Morphe source name: **Dvredin Patches**.
- The renamed output is the original Maps target's main tracked result, rather than
  a user-requested clone on an additional card. Android still installs it separately
  from `com.google.android.apps.maps`.

## Maintained Manager contract

Manager 1.33.0 distinguishes purpose-renamed patched apps from explicitly requested
clones. Its `producesClone` decision requires a changed output package AND an
explicitly selected patch declaring an option key literally `packageName`.
A purpose-renamed main result retains `originalPackageName` as its tracking key;
`homeAppSlots` places that result on the original target's slot.

Source references pinned to the tested Manager tag:

- [PatchRunTargeting](https://github.com/MorpheApp/morphe-manager/blob/2ba7bd57f24b3677b72b5487c9512cc0df77eed7/app/src/main/java/app/morphe/manager/ui/model/PatchRunTargeting.kt)
- [HomeAppItem](https://github.com/MorpheApp/morphe-manager/blob/2ba7bd57f24b3677b72b5487c9512cc0df77eed7/app/src/main/java/app/morphe/manager/ui/model/HomeAppItem.kt)

The source-owned **Maps identity** patch uses `mapsPackageName`, not the generic
clone option. Its existing manifest/provider/permission/process/alias/shortcut
transformation and validation are unchanged. The app-name patch changes only the
default value to Maps and retains the locale-safe string binding.
No Manager fork, database edits, source aliases, runtime/GPS/Cronet changes, or
modified-output input declarations are part of this candidate.

## Migration and limits

Changing from `org.ungoogled.android.apps.maps` to `io.github.dvredin.maps` creates
a new installation and private-data boundary. Existing bookmarks, timeline,
settings and grants do not migrate automatically. Keep the working old app and
use its normal export/import facilities for supported data; do not uninstall or
clear it to make the new card look tidy. Subsequent updates of the personal
package require the same Manager signing identity.

Manager preserves historical tracked clones or multiple existing main results.
This patch does not hide/delete those records: one new main result does not mean
removing every old Maps card. The guarantee is scoped to the new result and its
original target slot, not arbitrary old installs or untested Manager versions.

Existing saved v1.0.0 `Change package name`/`packageName` overrides are a different
option contract. Inspect the new source selection and Maps identity default rather
than treating the old configuration as automatically migrated. Choose this source
only for Maps; another selected source with a clone option can still request a clone.

## Acceptance state

The local candidate `1.1.0-dev.local.1` passed the changed-path checks on a
dedicated Pixel 8 / Android 16 / Manager 1.33.0:

- Fourteen source/maintenance regressions passed. The five identity checks cover
  the defaults, option contract, unchanged transformations and original-only input.
- All 52 real Kotlin patch sources compiled, and the actual generated catalog
  declared 33 patches / one original input. GPL/NOTICE and accepted extension
  resources were retained in the candidate bundle.
- Morphe Desktop applied all 33 patches successfully; output package and visible
  name are `io.github.dvredin.maps` / Maps.
- Real Manager imported the candidate, used the exact supported saved original
  APK, built/signed/installed its output, and put it on the original Maps slot
  without Clone. The existing old clone remained. The visible Home card count
  did not increase with this new package; cold Manager preserved the association.
- The original-card details exposed the personal output package and original
  `com.google.android.apps.maps` target. Rebuilding from that card and installing
  again updated the same package without a rename/clone flow. UID and original
  installation time persisted; update time changed and the saved dark-theme
  preference survived. This verifies a same-package candidate reinstall, not a
  migration from the previous Ungoogled Maps package or a future-version upgrade.
- The Manager APK's signature and standard 4-byte ZIP alignment passed. It was
  installed and exercised on a 4 KiB-page device. The optional 16 KiB native-library
  alignment check failed for `libandroidx.graphics.path.so`; 16 KiB-page device
  support is not claimed or certified by this test.
- The app stayed alive/foreground and displayed loaded dark map tiles with GMS
  temporarily disabled. There were no relevant package-scoped fatal/ANR markers;
  GMS state was restored. This does not add a GPS, routing or zero-telemetry claim.
- All 356,987 runtime method digests, including the accepted guarded Cronet
  fallback, matched the accepted app baseline. No runtime code was changed.

The full local Gradle build remained blocked before compilation because the
maintained package-registry plugin was unavailable in the offline cache. Local
validation compiled fresh patch classes/DEX and borrowed the unchanged runtime
extension from accepted v1.0.0; that narrower route is **not a fresh full Gradle
release build**. The actual Manager application builds above used this candidate.

The source is local and unreleased. No public branch push, stable metadata edit,
release dispatch or main/dev promotion was performed. The later `1.1.0-dev.local.2`
retains this identity and adds the [native recenter correction](NATIVE_RECENTER.md).
The maintainer reported that the recenter error no longer occurs on the primary
device; this feedback is not a fresh exhaustive verification of Manager tracking
or every identity checklist item. An approved maintained full build/publication
remains a separate release gate.
Raw APKs, device identities/logs/screens and exact private receipts stay outside
the public tree. Old sources were restored to their prior enabled states, the
candidate source was left disabled, temporary broad file access was restored,
and the dedicated phone was put back to sleep after testing.
