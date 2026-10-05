import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = "extensions/extension/src/main/java/org/ungoogled/ui/"
BASE = "9959031"

class SettingsRollback(unittest.TestCase):
    def baseline(self, path):
        return subprocess.check_output(["git", "show", BASE + ":" + path], cwd=ROOT, text=True)

    def test_activity_navigation_is_exact_pre_experiment_source(self):
        for path in [UI + "CustomizationActivity.java", UI + "SavedPlaces.java", UI + "YouActivity.java",
                     "patches/src/main/kotlin/org/ungoogled/patches/maps/ui/customization/CustomizationScreenPatch.kt"]:
            with self.subTest(path=path):
                current = (ROOT / path).read_text()
                if path == UI + "CustomizationActivity.java":
                    # The new switch is the only authorized addition; all navigation remains exact.
                    addition = '''        if (Shapes.locationSmoothingPatched()) {
            Switch smooth = new Switch(this);
            smooth.setChecked(LocationSmoothing.enabled(this));
            body.addView(toggleRow("Smooth location updates",
                    "Android API: holds the last precise position for up to 3 seconds when accuracy drops", smooth));
            smooth.setOnCheckedChangeListener((CompoundButton b, boolean on) ->
                    LocationSmoothing.setEnabled(this, on));
        }

'''
                    self.assertEqual(current.count(addition), 1)
                    current = current.replace(addition, "")
                    # The everyday-use source contains no diagnostics or export bridge.
                    self.assertNotIn("SatelliteDiagnostics", current)
                    self.assertNotIn("SatelliteDebugArchive", current)
                self.assertEqual(self.baseline(path), current)

    def test_original_settings_return_is_restored_and_gear_helper_has_no_motion(self):
        old = self.baseline(UI + "Shapes.java")
        current = (ROOT / (UI + "Shapes.java")).read_text()
        start = "    /** The OneGoogle account-menu shower"
        end = "    private static float radiusPx"
        self.assertEqual(old[old.index(start):old.index(end)], current[current.index(start):current.index(end)])
        gear = (ROOT / (UI + "SettingsMenu.java")).read_text()
        self.assertIn("drawGear", gear)
        for forbidden in ["ActivityOptions", "Dialog", "heldOwner", "heldDialog", "overridePendingTransition",
                          "overrideActivityTransition", "beginSettings", "setWindowAnimations", "startActivity"]:
            self.assertNotIn(forbidden, gear)

    def test_profile_is_cut_at_full_row_boundaries_in_both_builders(self):
        trim = (ROOT / "patches/src/main/kotlin/org/ungoogled/patches/maps/account/trim/TrimAccountMenuPatch.kt").read_text()
        self.assertIn("CustomActionsListFingerprint", trim)
        self.assertIn("val start = m.first().index + 1", trim)
        legacy = trim[trim.index("LegacyYourProfileFingerprint.let"):]
        self.assertIn("val start = m[1].index + 1", legacy)
        self.assertIn("removeWholeRows", legacy)
        fp = (ROOT / "patches/src/main/kotlin/org/ungoogled/patches/maps/account/trim/Fingerprints.kt").read_text()
        self.assertIn("literal(2132026353)", fp)
        self.assertIn("ProfileActionProviderFingerprint.instructionMatches", trim)
        self.assertIn("ProfileActionRendererFingerprint.let", trim)
        self.assertIn("iget v12, v8, Lcmia;->d:I", trim)
        self.assertIn("sget-object v14, Lcpdc;->e:Lbyfp;", trim)
        self.assertIn('ExternalLabel("next_action", instructions[loop])', trim)
