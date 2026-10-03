# Combined Maps safety candidate

Status: local test source `1.1.0-dev.local.4`, awaiting maintainer testing.
Published stable remains `v1.0.0`; no stable release or public push was performed.
Future changes are explicitly deferred to the next version. Selected nearby
search and light-theme persistence are tracked in the [backlog](BACKLOG.md),
not included in this candidate.

This candidate adds the maintainer's earlier contributions #12, #13 and #14 to
the accepted personal Maps identity, [native recenter](NATIVE_RECENTER.md) and
[native heading](NATIVE_HEADING.md) changes. The existing system-Cronet correction
#16 is retained, not applied a second time. Upstream #15 is a feature-request
issue, not a patch included here. These contributions remain separate from
upstream approval; importing them does not claim they have been merged upstream.

## Imported contributions

| Contribution | Reviewed upstream commit | Local adoption commit | Behavior |
|---|---|---|---|
| [#12](https://github.com/bearinmindcat/morphe-patches/pull/12) | `c453f984d8abda841df656151d9bac9d92266e17` | `d7e46786b1b4d0d1d6bc567c2e327736a8e9ca43` | Reject invalid/missing coordinates, roll back failed imports, preserve unreadable saved/recent stores |
| [#13](https://github.com/bearinmindcat/morphe-patches/pull/13) | `c6fbb869aa4a4d8a09cdbb27a3fdb5baf9827542` | `95fbc7394f6ab503e756f9165971acb6204249b3` | Remove external photo URL fields from newly imported places/history/Home/Work/labels |
| [#14](https://github.com/bearinmindcat/morphe-patches/pull/14) | `fe827ce7763ea74f06a6ff714fd1e87049c22ea8` | `8fb745a89530885abdc9c06171f620916bfa581b` | Reject invalid enabled proxies and stop Cronet engine creation when required proxy setup fails |
| [#16](https://github.com/bearinmindcat/morphe-patches/pull/16) | `666135f8fccb6404bffba8a74d58c41d50309ebd` | Already retained | Prefer system Cronet at the exact Java-fallback site, preserving proxy/original fallback |

The existing public patches are modified internally; the source still contains
**33 patches / 1 application**, not three extra switches. Supported input remains
original Google Maps `com.google.android.apps.maps` / `26.36.04.973607363`.
Default output remains **Maps**, package `io.github.dvredin.maps`, with the same
Manager main-card tracking and signing-key continuity.

## Intentional differences and limits

- Imported records no longer restore remote photo URLs, including Google image
  URLs; they show a placeholder. Names, coordinates, lists and notes remain.
  Existing locally saved photos are not scrubbed or migrated.
- A damaged saved/recent file is preserved instead of overwritten automatically.
  This does not add a recovery UI or crash-atomic transactions across both files.
- With a required proxy that Cronet cannot apply, its engine creation now fails
  rather than silently continuing directly. Map loading may be blocked until
  the configuration is corrected or the proxy disabled. This is protection of
  this Cronet build path, not a whole-app/system firewall.
- The deferred startup investigation is not resumed and no startup fix is added.

## Fresh build and verification

The Java runtime extension was **rebuilt from all current production sources**,
not borrowed from the old heading-only candidate. Local maintained javac/D8
compiled 20 Java sources and 57 extension classes plus all 54 Kotlin patch sources.
Compile-only Cronet stubs and JUnit/Robolectric tests are excluded from the runtime.
The official local Gradle build still cannot resolve Morphe plugin `1.3.4`;
this is a local test compilation, not a successful fresh official Gradle/CI release.

Verified gates:

- Combined extension behavior tests: **20 passed** (10 storage/import, 3 photo
  privacy including a loopback HTTP request counter, 7 proxy). Python source/
  repository contracts: **23 passed**. Both maintained CI workflows now run the
  isolated extension behavior project before build/release.
- Morphe Desktop applied **33 patches, zero failures** to the exact original input.
  The clean QA APK passed signature and 16-KiB ZIP-alignment verification and
  updated the retained QA package without uninstalling or clearing data.
- Real Manager `1.33.0` imported the exact local source as `1.1.0-dev.local.4`,
  **33 patches / 1 app**, alongside existing sources. It remained disabled as
  before import. Manager cold Home survived without a relevant fatal exception;
  its temporary all-files app-op was restored to the original default.
- Independent DEX comparison found all closed Maps methods identical to the
  accepted heading candidate, including native heading/recenter and Cronet.
  Ten selected new PR implementation methods in the generated APK exactly match
  the freshly built extension. No private diagnostics/test classes were present.
  The manual Java route omits AGP's generated empty R shell; it was independently
  proved empty and unreferenced, not treated as an application behavior change.
- Clean Pixel 8 location/recenter passed cold, Home/resume, public mock movement
  and screen lock/unlock; the foreground process survived without a fatal.
- On the real candidate UI, a blank proxy could not be enabled and the switch
  returned to disabled. A valid public-landmark JSON import succeeded; an import
  without coordinates displayed a rejection and preserved the first place.
  Its UI-exported backup retained that place and excluded the imported photo URL.

Successful real Cronet proxy networking, an all-stack traffic audit, physical
turning on the primary phone for this combined candidate and the full Manager
patch/install flow are not newly claimed. Earlier heading acceptance is preserved,
but the maintainer must test this exact combined candidate before publication.

## Owner test source

File: `Dvredin_Patches_Maps_All_Fixes_1.1.0-dev.local.4.mpp`.
Size: 606904 bytes. SHA-256:
`dcb80ae51c727ec97105e4ecc3af621a1e9f8684ad087739e04d316a42630d87`.

1. Export a backup of saved places using Maps' existing UI.
2. Import the new `.mpp` in Manager and select patches from this Maps source only.
3. Patch original `26.36.04.973607363`, keeping Maps identity and the existing
   Manager signing key. Update the current Maps installation without deleting it.
4. Keep Location source on Android; check compass, recenter and Home/resume,
   then ordinary saved places and import/export. If using a proxy, check its
   intended blocked/working behavior separately.

Owner feedback on `.local.3` is not blanket acceptance of these new persistence/
proxy changes. Publication awaits the maintainer's test of `.local.4`; there is
no background release dispatch or automatic stable promotion.
