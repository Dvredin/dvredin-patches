import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class CatalogPresentation(unittest.TestCase):
    def test_generated_catalog_uses_current_output_name_and_is_idempotent(self):
        with tempfile.TemporaryDirectory() as folder:
            readme = Path(folder) / "README.md"
            readme.write_text((ROOT / "README.md").read_text())
            command = [sys.executable, str(ROOT / ".github/scripts/generate_patches_readme.py"),
                       "Dvredin/dvredin-patches", "main", str(ROOT / "patches-list.json"), str(readme)]
            subprocess.run(command, cwd=ROOT, check=True, capture_output=True)
            first = readme.read_text()
            summary = next(line for line in first.splitlines() if line.startswith("<summary>"))
            self.assertIn(" Google Maps", summary)
            self.assertIn('align="top"> Maps', summary)
            self.assertNotIn("Ungoogled Maps", summary)
            self.assertIn("33 patches", summary)
            self.assertIn("| Maps identity |", first)
            subprocess.run(command, cwd=ROOT, check=True, capture_output=True)
            self.assertEqual(first, readme.read_text())
