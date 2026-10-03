# Extension regression tests

Run from the repository root with Java 21 or newer:

```sh
./gradlew -p tests test
```

This isolated Gradle project compiles the actual extension Java sources and the
existing Cronet compile-time stubs, then runs JUnit 4 tests with Robolectric on
Android API 35. It needs neither an APK nor GitHub Packages credentials. Test
dependencies are not added to the production patch bundle.

The tests exercise extension behavior, not a patched Google Maps APK. In
particular, the Cronet stubs cannot establish successful networking with the
real Cronet version bundled into Maps; device testing remains separate.

The adopted PR #12/#13/#14 regressions run together: 10 saved-store/import tests,
3 imported-photo tests (including a loopback HTTP request counter), and 7 proxy
settings/hook tests. This is separate from the Python source/repository contracts.
The Verify and Release workflows run this isolated behavior project before the
production build. See [combined candidate evidence](../docs/PR_SAFETY_CANDIDATE.md).
