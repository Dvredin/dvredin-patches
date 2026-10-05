"""Keep the promoted runtime equal to the maintainer-accepted source snapshot."""
import hashlib
import json
import unittest
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
class AcceptedRuntime(unittest.TestCase):
    def test_exact_accepted_runtime(self):
        expected = json.loads((ROOT / "tests/accepted-runtime-sha256.json").read_text())
        actual = {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest()
                  for root in ("patches/src/main/kotlin", "extensions/extension/src/main/java", "extensions/extension/stub/src/main/java")
                  for p in (ROOT / root).rglob("*") if p.is_file()}
        self.assertEqual(expected, actual)
    def test_release_selection_excludes_black_and_diagnostics(self):
        build = (ROOT / "patches/build.gradle.kts").read_text()
        self.assertIn('kotlin.exclude("org/ungoogled/patches/maps/ui/black/BlackThemePatch.kt")', build)
        names = [p.name for root in ("patches/src/main/kotlin", "extensions/extension/src/main/java")
                 for p in (ROOT / root).rglob("*") if p.is_file()]
        for name in names:
            self.assertFalse(any(token in name for token in ("SatelliteDiagnostic", "SatelliteDebug", "MapEngineCounter", "MapEngineDiagnostic", "PropertyObserver", "PropertyDiagnostic", "NativeRenderDiagnostic", "RestoreSatelliteStyle")), name)
