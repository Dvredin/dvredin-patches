#!/usr/bin/env python3
"""Resume an explicitly approved, already tagged release using git and GitHub CLI.

This is not a release/version engine: version, notes, metadata and source all
come from the immutable tag produced by the ordinary Semantic Release workflow.
Existing assets are never clobbered, and tags/history are never rewritten.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import zipfile


def require(ok, message):
    if not ok:
        raise RuntimeError(message)


def command(*args, check=True):
    result = subprocess.run(args, capture_output=True, text=True, timeout=120)
    if check and result.returncode:
        raise RuntimeError(f"{args[0]} failed ({result.returncode}): {result.stderr.strip()}")
    return result


def api(path):
    return json.loads(command('gh', 'api', path).stdout)


def release_for(repo, tag):
    result = command('gh', 'api', f'repos/{repo}/releases/tags/{tag}', check=False)
    if result.returncode == 0:
        return json.loads(result.stdout)
    require('HTTP 404' in result.stderr, 'Release lookup failed; not treating it as absent')
    # GitHub's tag endpoint only finds published releases. Drafts must be read
    # through the authenticated releases collection before deciding to create.
    pages = json.loads(command('gh', 'api', '--paginate', '--slurp',
                               f'repos/{repo}/releases?per_page=100').stdout)
    matches = [release for page in pages for release in page if release['tag_name'] == tag]
    require(len(matches) <= 1, 'Multiple releases for target tag; refusing ambiguity')
    return matches[0] if matches else None


def validate_identity(repo, tag, sha):
    require(re.fullmatch(r'[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+', repo), 'Invalid repository')
    require(re.fullmatch(r'v(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)', tag), 'Stable tag required')
    require(re.fullmatch(r'[0-9a-f]{40}', sha), 'Full expected commit required')
    return tag[1:]


def verify_tag(repo, tag, sha):
    obj = api(f'repos/{repo}/git/ref/tags/{tag}')['object']
    for _ in range(5):
        if obj['type'] != 'tag':
            break
        obj = api(f'repos/{repo}/git/tags/{obj["sha"]}')['object']
    require(obj['type'] == 'commit' and obj['sha'] == sha, 'Remote tag differs from approved commit')
    require(command('git', 'rev-parse', 'HEAD').stdout.strip() == sha, 'Checkout differs from approved commit')


def validate_metadata(root, repo, tag):
    version = tag[1:]
    bundle = json.loads((root / 'patches-bundle.json').read_text())
    catalog = json.loads((root / 'patches-list.json').read_text())
    expected_url = f'https://github.com/{repo}/releases/download/{tag}/patches-{version}.mpp'
    require(bundle['version'] == catalog['version'] == version, 'Generated version mismatch')
    require(bundle['download_url'] == expected_url, 'Generated download target mismatch')
    require(bundle.get('description', '').strip(), 'Missing generated release notes')
    require(re.search(r'^version\s*=\s*' + re.escape(version) + r'\s*$',
                      (root / 'gradle.properties').read_text(), re.M), 'Gradle version mismatch')
    lock = json.loads((root / 'upstreams.json').read_text())
    expected = sum(c['expected_public_patches'] for c in lock['components'])
    require(len(catalog['patches']) == expected, 'Generated patch count mismatch')
    return bundle


def verify_bundle(root, version):
    artifact = root / 'patches/build/libs' / f'patches-{version}.mpp'
    require(artifact.is_file(), 'Official Gradle MPP missing')
    with zipfile.ZipFile(artifact) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), 'Duplicate archive entries')
        require(archive.testzip() is None, 'Corrupt archive')
        manifest = archive.read('META-INF/MANIFEST.MF').decode().replace('\r\n ', '').replace('\r\n', '\n')
        require(re.search(r'^Version: ' + re.escape(version) + r'$', manifest, re.M), 'MPP version mismatch')
        for name in ('LICENSE', 'NOTICE'):
            require(archive.read(name) == (root / name).read_bytes(), 'Missing/mismatched notice')
        require('classes.dex' in names and 'extensions/extension.mpe' in names, 'Incomplete Android bundle')
        forbidden = ('.apk', '.xapk', '.keystore', '.jks', '.key', '.pem')
        require(not any(n.lower().endswith(forbidden) for n in names), 'Unexpected private/vendor archive asset')
    return artifact, 'sha256:' + hashlib.sha256(artifact.read_bytes()).hexdigest()


def ensure_asset_compatible(release, tag, name, digest):
    require(release['tag_name'] == tag and not release['prerelease'], 'Release identity/channel mismatch')
    assets = release.get('assets', [])
    require(all(a['name'] == name for a in assets), 'Unexpected existing release assets')
    require(len(assets) <= 1, 'Duplicate release assets')
    if assets:
        require(assets[0].get('digest') == digest and assets[0].get('state') == 'uploaded',
                'Existing asset differs; refusing overwrite')
    return bool(assets)


def stage(repo, tag, sha, artifact, digest, notes):
    verify_tag(repo, tag, sha)
    release = release_for(repo, tag)
    if release is None:
        command('gh', 'release', 'create', tag, '--repo', repo, '--verify-tag',
                '--draft', '--title', tag, '--notes-file', str(notes), '--latest=false')
        release = release_for(repo, tag)
        require(release is not None and release['draft'], 'Draft creation readback failed')
    assert release is not None
    exists = ensure_asset_compatible(release, tag, artifact.name, digest)
    require(release['draft'] or exists, 'Published release without expected asset; refusing automatic repair')
    if not exists:
        command('gh', 'release', 'upload', tag, str(artifact), '--repo', repo)
    release = release_for(repo, tag)
    require(ensure_asset_compatible(release, tag, artifact.name, digest), 'Asset readback failed')
    return release


def publish(repo, tag, sha, artifact, digest):
    verify_tag(repo, tag, sha)
    release = release_for(repo, tag)
    require(release is not None, 'Staged release missing')
    assert release is not None
    require(ensure_asset_compatible(release, tag, artifact.name, digest), 'Staged asset missing')
    if release['draft']:
        command('gh', 'release', 'edit', tag, '--repo', repo,
                '--draft=false', '--prerelease=false', '--latest')
    release = release_for(repo, tag)
    require(release is not None, 'Published release disappeared')
    assert release is not None
    require(not release['draft'] and ensure_asset_compatible(release, tag, artifact.name, digest),
            'Published release readback failed')
    require(api(f'repos/{repo}/releases/latest')['id'] == release['id'], 'Latest release readback failed')
    return release


def restore_note(repo, tag, sha):
    verify_tag(repo, tag, sha)
    release = release_for(repo, tag)
    require(release and not release['draft'] and not release['prerelease'] and release['assets'],
            'Cannot mark unpublished release as complete')
    ref = 'refs/notes/semantic-release-' + tag
    remote = command('git', 'ls-remote', 'origin', ref).stdout.strip()
    if remote:
        command('git', 'fetch', '--no-tags', 'origin', f'{ref}:{ref}')
    existing = command('git', 'notes', '--ref', ref, 'show', sha, check=False)
    expected = {'channels': [None]}
    if existing.returncode == 0:
        require(json.loads(existing.stdout) == expected, 'Different semantic note; refusing overwrite')
    else:
        command('git', '-c', 'user.name=github-actions[bot]',
                '-c', 'user.email=41898282+github-actions[bot]@users.noreply.github.com',
                'notes', '--ref', ref, 'add', '-m', json.dumps(expected, separators=(',', ':')), sha)
    command('git', 'push', 'origin', f'{ref}:{ref}')
    local = command('git', 'rev-parse', ref).stdout.strip()
    require(command('git', 'ls-remote', 'origin', ref).stdout.split()[0] == local,
            'Semantic note remote readback failed')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('phase', choices=('validate', 'stage', 'publish', 'note'))
    parser.add_argument('--repo', required=True)
    parser.add_argument('--tag', required=True)
    parser.add_argument('--sha', required=True)
    args = parser.parse_args()
    version = validate_identity(args.repo, args.tag, args.sha)
    root = Path.cwd()
    verify_tag(args.repo, args.tag, args.sha)
    bundle = validate_metadata(root, args.repo, args.tag)
    notes = root / 'build/recovery-notes.md'
    notes.parent.mkdir(exist_ok=True)
    notes.write_text(bundle['description'] + '\n')
    if args.phase == 'validate':
        print('PASS: existing tag, exact source and generated metadata')
        return
    artifact, digest = verify_bundle(root, version)
    if args.phase == 'stage':
        stage(args.repo, args.tag, args.sha, artifact, digest, notes)
    elif args.phase == 'publish':
        publish(args.repo, args.tag, args.sha, artifact, digest)
    else:
        restore_note(args.repo, args.tag, args.sha)
    print(json.dumps({'phase': args.phase, 'tag': args.tag, 'source': args.sha,
                      'asset': artifact.name, 'digest': digest}))


if __name__ == '__main__':
    main()
