"""Source-contract regressions; actual output/Manager acceptance is separate."""
from pathlib import Path
import json
import re
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = "patches/src/main/kotlin/org/ungoogled/patches/maps/resources/"


class MapsIdentityContract(unittest.TestCase):
    def text(self, name):
        return (ROOT / RESOURCES / name).read_text()

    def upstream(self, name):
        lock = json.loads((ROOT / "upstreams.json").read_text())
        component = next(c for c in lock["components"] if c["id"] == "maps")
        result = subprocess.run(["git", "show", component["base_commit"] + ":" + RESOURCES + name], cwd=ROOT, capture_output=True, text=True, check=True)
        return result.stdout

    def test_visible_name_is_maps(self):
        source = self.text("ChangeAppNamePatch.kt")
        self.assertRegex(source, r'key = "appName",\s+default = "Maps",')

    def test_source_owned_package_option(self):
        source = self.text("ChangePackageNamePatch.kt")
        self.assertRegex(source, r'key = "mapsPackageName",\s+default = "io.github.dvredin.maps",')
        self.assertNotIn('key = "packageName"', source)
        self.assertIn('name = "Maps identity"', source)
        self.assertIn('required = true', source)

    def test_manifest_transformation_is_unchanged(self):
        name = "ChangePackageNamePatch.kt"
        self.assertEqual(self.text(name).split("    execute {", 1)[1], self.upstream(name).split("    execute {", 1)[1])

    def test_locale_safe_label_transformation_is_unchanged(self):
        name = "ChangeAppNamePatch.kt"
        self.assertEqual(self.text(name).split("    execute {", 1)[1], self.upstream(name).split("    execute {", 1)[1])

    def test_only_original_input_declared(self):
        source = (ROOT / "patches/src/main/kotlin/org/ungoogled/patches/shared/Constants.kt").read_text()
        self.assertEqual(re.findall(r'packageName = "([^"]+)"', source), ["com.google.android.apps.maps"])
        self.assertNotIn("io.github.dvredin.maps", source)


if __name__ == "__main__":
    unittest.main()
