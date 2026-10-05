# Native min-mode compatibility ART regression

QA-only instrumentation for the exact supported Maps build. Compile this directory
as a separate test APK; never merge these sources or manifest into the extension.
The fixture hook classes deliberately collide with GrapheneOS-only class names to
reproduce its null-prefix stack on the **stock dedicated Pixel8**, not on GrapheneOS.

Run against the existing separate QA package with its matching test signer:

1. Install the previously signed local.15 QA APK without clearing its data.
2. Install this instrumentation APK and invoke
   `am instrument -w -e patched false io.github.dvredin.maps.minmodeartqa/qa.MinModeArtRunner`.
3. Upgrade to the exact candidate signed QA APK and invoke the same component with
   `-e patched true`. The marker from step2 must survive the update.
4. Parse the JSON result: require `pass:true`, `cases:8`, correct `fixed` value and
   all assertions. The shell exit or instrumentation finish code alone is not a verdict.
5. Remove only this isolated runner, restore the pre-test APK/settings and sleep
   the dedicated device after completing ordinary search UI checks.

The runner reflects the actual `abmz.a` method, with a Context wrapper injecting
known/unknown failures and controlled Resources returning true/false/missing.
It does not reimplement the production probe or call only the extension helper.
Baseline confirms the known NPE escapes; candidate must return false. Unrelated
NPE and SecurityException must keep their identity and propagate.

The exact build/signing recipe and artifacts remain in private project evidence;
see [fix/acceptance documentation](../../../docs/SEARCH_CRASH_FIX.md).
