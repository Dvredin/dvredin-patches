<p align="center"><img src="docs/assets/logo.svg" width="112" alt="Dvredin Patches"></p>
<h1 align="center">Dvredin Patches</h1>
<p align="center">Personal patches · compatible with Morphe · upstream-friendly</p>
<p align="center">
  <a href="LICENSE"><img alt="License GPLv3" src="https://img.shields.io/badge/license-GPLv3-2563eb"></a>
  <a href="https://morphe.software"><img alt="For use with Morphe" src="https://img.shields.io/badge/for-Morphe-14b8a6"></a>
</p>

A personal collection of application patches maintained by [Dvredin](https://github.com/Dvredin).
This is a **modified fork of third-party patches**, not an official Morphe, Google,
or upstream-author release. Original developers retain their authorship.

## Applications

| Application | Original version | Current scope |
|---|---|---|
| Google Maps → Ungoogled Maps | `26.36.04.973607363` | bearinmind patches v1.3.0 plus the system-Cronet correction: 33 patches |

Yandex applications and other patch families are not included yet.
An application version is supported only after it has been identified and tested;
there is no promise that a newer APK will work.

<!-- PATCHES_START -->
The detailed patch catalog is generated from the actual build during release.
<!-- PATCHES_END -->

## What differs from upstream

The additional **Use system Cronet fallback** patch prefers Android's system
HttpEngine when Maps requests the Java Cronet fallback. A working Play services
engine is left alone. The original fallback remains available when the system
provider is unavailable or an application proxy is configured.

The implementation is also submitted as [upstream PR #16](https://github.com/bearinmindcat/morphe-patches/pull/16).
Earlier unrelated import, photo, and proxy fixes are not silently included.
Our source branding does not change the installed application's name, package,
permissions, or signing identity. Use the same Manager signing key for updates.

## Use with Morphe

After the first release is published, add this repository as a remote source:

[**Add Dvredin Patches to Morphe**](https://morphe.software/add-source?github=Dvredin/dvredin-patches)

1. Install an official compatible [Morphe Manager](https://github.com/MorpheApp/morphe-manager/releases).
2. Add the source and select the original application of the exact supported version.
3. Select patches from this source only; do not combine overlapping Maps bundles.
4. Patch and install using Manager. Do not uninstall or clear a working application
   to bypass a signing error. Export your Manager signing key through its normal backup UI.

This repository distributes patch code, not original or patched application APKs.
Maps still accesses Google's servers for its core map, search, and routing functions.
Google account-dependent features are not supported by this profile. This is not a
claim of zero telemetry or offline availability of every feature.

## Build and contribute

Use the maintained Morphe Gradle plugin and Semantic Release integration, rather
than a custom compiler or release service. See:

- [Contribution entry point](CONTRIBUTING.md)
- [Development and verification](docs/DEVELOPMENT.md)
- [Pinned upstream updates](docs/UPSTREAM_UPDATES.md)
- [Release and publication](docs/PUBLISHING.md)
- [Provenance and credits](docs/PROVENANCE.md)

The current source is inherited from the original public Git history. Exact
component revisions and the local addition are recorded in [upstreams.json](upstreams.json).

## Credits and license

Thank you to **[bearinmindcat](https://github.com/bearinmindcat)** for Ungoogled Maps
and the original [bearinmind patches](https://github.com/bearinmindcat/morphe-patches),
and to **[MorpheApp](https://github.com/MorpheApp)** and the original ReVanced
contributors for the patching infrastructure and template.

Distributed under [GNU GPL v3](LICENSE), with the additional naming terms retained
in [NOTICE](NOTICE). The project's primary name is **Dvredin Patches**; Morphe is
mentioned only to describe compatibility. See [provenance](docs/PROVENANCE.md).
