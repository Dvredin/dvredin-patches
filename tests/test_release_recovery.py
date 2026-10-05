"""Failure-injection coverage for the bounded existing-tag recovery adapter."""
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import zipfile

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('recovery', ROOT / 'scripts/recover_release.py')
assert spec is not None and spec.loader is not None
recovery = importlib.util.module_from_spec(spec)
spec.loader.exec_module(recovery)


class ReleaseRecoveryTests(unittest.TestCase):
    repo = 'example/patches'
    tag = 'v1.2.0'
    sha = 'a' * 40
    digest = 'sha256:' + 'b' * 64

    def release(self, draft=True, assets=True):
        return {'id': 7, 'tag_name': self.tag, 'draft': draft, 'prerelease': False,
                'assets': [{'name': 'patches-1.2.0.mpp', 'digest': self.digest, 'state': 'uploaded'}] if assets else []}

    def test_exact_stable_identity(self):
        self.assertEqual(recovery.validate_identity(self.repo, self.tag, self.sha), '1.2.0')
        for tag in ['--tag', 'v1.2.0;false', 'v01.2.0', 'v1.2.0-dev.1']:
            with self.assertRaises(RuntimeError):
                recovery.validate_identity(self.repo, tag, self.sha)
        with self.assertRaises(RuntimeError):
            recovery.validate_identity(self.repo, self.tag, 'a' * 7)

    def test_remote_tag_change_is_rejected(self):
        with patch.object(recovery, 'api', return_value={'object': {'type': 'commit', 'sha': 'b' * 40}}), patch.object(recovery, 'command') as cmd:
            with self.assertRaises(RuntimeError):
                recovery.verify_tag(self.repo, self.tag, self.sha)
            cmd.assert_not_called()

    def test_auth_failure_is_not_absent_release(self):
        with patch.object(recovery, 'command', return_value=subprocess.CompletedProcess([], 1, '', 'HTTP 403')):
            with self.assertRaises(RuntimeError):
                recovery.release_for(self.repo, self.tag)
        with patch.object(recovery, 'command', return_value=subprocess.CompletedProcess([], 1, '', 'HTTP 404')):
            self.assertIsNone(recovery.release_for(self.repo, self.tag))

    def test_asset_conflicts_fail_closed(self):
        release = self.release()
        self.assertTrue(recovery.ensure_asset_compatible(release, self.tag, 'patches-1.2.0.mpp', self.digest))
        with self.assertRaises(RuntimeError):
            recovery.ensure_asset_compatible(release, self.tag, 'patches-1.2.0.mpp', 'sha256:wrong')
        release['assets'].append(dict(release['assets'][0]))
        with self.assertRaises(RuntimeError):
            recovery.ensure_asset_compatible(release, self.tag, 'patches-1.2.0.mpp', self.digest)

    def test_stage_existing_correct_asset_has_no_writes(self):
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', return_value=self.release()), patch.object(recovery, 'command') as cmd:
            recovery.stage(self.repo, self.tag, self.sha, Path('patches-1.2.0.mpp'), self.digest, Path('notes'))
            cmd.assert_not_called()

    def test_stage_missing_release_creates_draft_then_uploads_without_clobber(self):
        reads = [None, self.release(assets=False), self.release()]
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', side_effect=reads), patch.object(recovery, 'command') as cmd:
            recovery.stage(self.repo, self.tag, self.sha, Path('patches-1.2.0.mpp'), self.digest, Path('notes'))
            self.assertEqual(cmd.call_count, 2)
            create, upload = [call.args for call in cmd.call_args_list]
            self.assertIn('--draft', create)
            self.assertIn('--verify-tag', create)
            self.assertNotIn('--clobber', upload)
            self.assertNotIn('--force', create + upload)

    def test_publish_is_idempotent_and_checks_latest(self):
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', return_value=self.release(draft=False)), patch.object(recovery, 'api', return_value={'id': 7}), patch.object(recovery, 'command') as cmd:
            recovery.publish(self.repo, self.tag, self.sha, Path('patches-1.2.0.mpp'), self.digest)
            cmd.assert_not_called()
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', return_value=self.release(draft=False)), patch.object(recovery, 'api', return_value={'id': 8}):
            with self.assertRaises(RuntimeError):
                recovery.publish(self.repo, self.tag, self.sha, Path('patches-1.2.0.mpp'), self.digest)

    def test_no_semantic_note_for_unpublished_draft(self):
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', return_value=self.release()), patch.object(recovery, 'command') as cmd:
            with self.assertRaises(RuntimeError):
                recovery.restore_note(self.repo, self.tag, self.sha)
            cmd.assert_not_called()

    def test_conflicting_semantic_note_is_not_overwritten(self):
        def command(*args, **kwargs):
            if args[:2] == ('git', 'ls-remote'):
                return subprocess.CompletedProcess(args, 0, 'b' * 40 + '\trefs/notes/semantic-release-v1.2.0', '')
            if args[:2] == ('git', 'fetch'):
                return subprocess.CompletedProcess(args, 0, '', '')
            if args[:2] == ('git', 'notes'):
                return subprocess.CompletedProcess(args, 0, json.dumps({'channels': ['dev']}), '')
            self.fail('Unexpected write: ' + repr(args))
        with patch.object(recovery, 'verify_tag'), patch.object(recovery, 'release_for', return_value=self.release(draft=False)), patch.object(recovery, 'command', side_effect=command):
            with self.assertRaises(RuntimeError):
                recovery.restore_note(self.repo, self.tag, self.sha)

    def test_generated_metadata_is_authoritative(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            bundle = {'version': '1.2.0', 'description': 'Generated notes', 'download_url': 'https://github.com/example/patches/releases/download/v1.2.0/patches-1.2.0.mpp'}
            (root / 'patches-bundle.json').write_text(json.dumps(bundle))
            (root / 'patches-list.json').write_text(json.dumps({'version': '1.2.0', 'patches': [{}]}))
            (root / 'upstreams.json').write_text(json.dumps({'components': [{'expected_public_patches': 1}]}))
            (root / 'gradle.properties').write_text('version=1.2.0\n')
            recovery.validate_metadata(root, self.repo, self.tag)
            bundle['download_url'] = 'https://github.com/other/repository/file'
            (root / 'patches-bundle.json').write_text(json.dumps(bundle))
            with self.assertRaises(RuntimeError):
                recovery.validate_metadata(root, self.repo, self.tag)

    def test_bundle_rejects_wrong_version_and_vendor_payload(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for name in ('LICENSE', 'NOTICE'):
                (root / name).write_text(name)
            path = root / 'patches/build/libs/patches-1.2.0.mpp'
            path.parent.mkdir(parents=True)
            def make(version, extra=False):
                with zipfile.ZipFile(path, 'w') as archive:
                    for name in ('LICENSE', 'NOTICE'):
                        archive.writestr(name, name)
                    archive.writestr('META-INF/MANIFEST.MF', f'Version: {version}\r\n')
                    archive.writestr('classes.dex', b'test fixture only')
                    archive.writestr('extensions/extension.mpe', b'test fixture only')
                    if extra:
                        archive.writestr('vendor.apk', b'test fixture only')
            make('1.2.0')
            recovery.verify_bundle(root, '1.2.0')
            make('1.1.0')
            with self.assertRaises(RuntimeError):
                recovery.verify_bundle(root, '1.2.0')
            make('1.2.0', extra=True)
            with self.assertRaises(RuntimeError):
                recovery.verify_bundle(root, '1.2.0')


if __name__ == '__main__':
    unittest.main()
