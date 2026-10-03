"""Source safety contracts; bytecode and device regression evidence is separate."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
LOCATION = ROOT / "patches/src/main/kotlin/org/ungoogled/patches/maps/location"


class NativeLocationSettingsContract(unittest.TestCase):
    def setUp(self):
        self.source = (LOCATION / "NativeLocationSettingsPatch.kt").read_text()

    def test_is_part_of_existing_provider_patch(self):
        toggle = (LOCATION / "LocationProviderTogglePatch.kt").read_text()
        self.assertIn("activityContextHookPatch, nativeLocationSettingsPatch", toggle)
        self.assertIn("internal val nativeLocationSettingsPatch = bytecodePatch {", self.source)
        self.assertNotIn('name = "Native location', self.source)

    def test_stock_path_is_guarded_and_retained(self):
        self.assertIn("$SHAPES->playLocation()Z", self.source)
        self.assertIn("if-nez v0, :play_settings", self.source)
        self.assertIn('ExternalLabel("play_settings", settings.implementation!!.instructions.first())', self.source)
        self.assertNotIn("removeInstructions", self.source)

    def test_native_path_reuses_callback_and_gps_dialog(self):
        self.assertIn("const/4 v2, 0x1", self.source)
        self.assertIn("Lamcl;->c(ZZZLamck;)V", self.source)
        self.assertIn("{v6, p0, p4, v1}", self.source)
        self.assertIn("$CALLBACK-><init>(Ljava/lang/Object;Ljava/lang/Object;I)V", self.source)
        self.assertNotIn("amdk.OPTIMIZED", self.source)
        self.assertNotIn("SettingsClient", self.source.split("settings.addInstructionsWithLabels", 1)[1])

    def test_original_and_constructor_shapes_are_gated(self):
        self.assertIn("settingsShape(settings) != ORIGINAL_SETTINGS", self.source)
        self.assertIn("a6a921c72446548df2a09ce4da7b18f390d20b2b163e1f6b3ae757ba40264faa", self.source)
        self.assertIn("parameterTypes == constructorParameters", self.source)
        self.assertIn("fields.containsAll", self.source)
        self.assertIn("implementation.registerCount", self.source)

    def test_does_not_ship_diagnostics_or_reset_app(self):
        for token in ["UGRecenterTrace", "TraceInfo", "private-trace", "android/util/Log", "force-stop", "killProcess", "grantRuntimePermission"]:
            self.assertNotIn(token, self.source)


if __name__ == "__main__":
    unittest.main()
