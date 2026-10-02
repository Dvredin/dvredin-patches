import hashlib
import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("verify", ROOT / "scripts/verify_repository.py")
assert spec is not None and spec.loader is not None
verify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verify)


class RepositoryGates(unittest.TestCase):
    def fixture(self, root):
        (root / "LICENSE").write_text("fixture license")
        (root / "patch.kt").write_text("fixture source")
        return {"schema_version": 1, "components": [{"id": "app", "repository": "https://github.com/example/patches", "base_commit": "a" * 40, "license_files": ["LICENSE"], "supported_inputs": [{"package": "example.app", "version": "1"}], "local_additions": [{"commit": "b" * 40, "path": "patch.kt", "sha256": hashlib.sha256(b"fixture source").hexdigest()}]}]}

    def test_reviewed_lock(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.assertEqual(verify.validate_lock(self.fixture(root), root), [])

    def test_moving_ref_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lock = self.fixture(root)
            lock["components"][0]["base_commit"] = "main"
            self.assertTrue(verify.validate_lock(lock, root))

    def test_changed_addition_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lock = self.fixture(root)
            (root / "patch.kt").write_text("unreviewed change")
            self.assertTrue(verify.validate_lock(lock, root))

    def test_unsafe_path_rejected(self):
        for value in ["../key", "/outside", "app/../../outside"]:
            self.assertFalse(verify.safe_relative(value))

    def test_missing_license_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lock = self.fixture(root)
            (root / "LICENSE").unlink()
            self.assertTrue(verify.validate_lock(lock, root))

    def test_private_files_rejected(self):
        for value in [".env", "key.jks", "inputs/original.apk", ".secrets/alias", "dist/patched.xapk", "private.log"]:
            self.assertTrue(verify.forbidden_path(value), value)
        for value in ["upstreams.json", "LICENSE", "docs/assets/logo.svg", "patches/src/Patch.kt"]:
            self.assertFalse(verify.forbidden_path(value), value)

    def test_npm_publisher_rejected(self):
        config = {"plugins": ["@semantic-release/github", "@semantic-release/exec", "gradle-semantic-release-plugin"]}
        self.assertEqual(verify.validate_release_config(config), [])
        config["plugins"].append("@semantic-release/npm")
        self.assertTrue(verify.validate_release_config(config))

    def test_inherited_publisher_config_rejected(self):
        config = {"extends": "unknown-config", "plugins": ["@semantic-release/github", "@semantic-release/exec", "gradle-semantic-release-plugin"]}
        self.assertTrue(verify.validate_release_config(config))
        self.assertTrue(verify.validate_release_config({}))

    def test_local_link_detection(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "LICENSE").write_text("fixture")
            file = root / "README.md"
            self.assertEqual(verify.local_links("[license](LICENSE) [web](https://example.com)", file, root), [])
            self.assertTrue(verify.local_links("[missing](missing.md)", file, root))


if __name__ == "__main__":
    unittest.main()
