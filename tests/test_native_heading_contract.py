"""Heading source contracts; runtime/Dex/device receipts are separate gates."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
LOCATION = ROOT / "patches/src/main/kotlin/org/ungoogled/patches/maps/location"


class NativeHeadingContract(unittest.TestCase):
    def setUp(self):
        self.source = (LOCATION / "NativeHeadingPatch.kt").read_text()

    def test_dependency_not_new_user_option(self):
        self.assertIn("nativeLocationSettingsPatch, nativeHeadingPatch", (LOCATION / "LocationProviderTogglePatch.kt").read_text())
        self.assertIn("internal val nativeHeadingPatch = bytecodePatch {", self.source)
        self.assertNotIn('name = "', self.source)

    def test_android_uses_existing_compass_and_play_tail_remains(self):
        self.assertIn("$SHAPES->playLocation()Z", self.source)
        self.assertIn("if-nez v0, :play_heading", self.source)
        self.assertIn("const/4 v0, 0x0\n            return v0", self.source)
        self.assertIn('ExternalLabel("play_heading", selector.implementation!!.instructions.first())', self.source)
        self.assertNotIn("removeInstructions", self.source)
        self.assertNotIn("setPlayLocationEnabled", self.source)

    def test_exact_method_and_layout_fail_closed(self):
        self.assertIn('it.name == "o" && it.returnType == "Z" && it.parameterTypes.isEmpty()', self.source)
        self.assertIn("headingShape(selector) != ORIGINAL_HEADING", self.source)
        self.assertIn("d000355961ccd3d23d5c1c1969f07462f52068307d6d1fde95e0c78106ddd12e", self.source)
        self.assertIn("implementation.registerCount", self.source)
        self.assertIn("instruction.codeOffset", self.source)

    def test_no_sensor_engine_permissions_or_diagnostics_added(self):
        for token in ["UGHeadingTrace", "TraceHeading", "private-heading", "android/util/Log", "registerListener(", "getRotationMatrix(", "grantRuntimePermission", "force-stop"]:
            self.assertNotIn(token, self.source)
        self.assertNotIn("SensorEvent", self.source)


if __name__ == "__main__":
    unittest.main()
