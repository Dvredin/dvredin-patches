#!/usr/bin/env python3
"""Small standard-library public-tree, provenance and local-link gate."""
from pathlib import Path, PurePosixPath
import ast
import hashlib
import json
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def safe_relative(value):
    p = PurePosixPath(value)
    return bool(value) and not p.is_absolute() and ".." not in p.parts


def validate_lock(lock, root):
    errors = []
    if lock.get("schema_version") != 1:
        errors.append("Unsupported provenance schema")
    components = lock.get("components", [])
    if not components:
        errors.append("No reviewed source components")
    seen = set()
    for component in components:
        name = component.get("id", "")
        if not name or name in seen:
            errors.append("Missing/duplicate component id")
        seen.add(name)
        if not re.fullmatch(r"[0-9a-f]{40}", component.get("base_commit", "")):
            errors.append(f"{name}: pin a full upstream commit, not a moving ref")
        if not component.get("repository", "").startswith("https://github.com/"):
            errors.append(f"{name}: explicit public upstream required")
        for value in component.get("license_files", []):
            if not safe_relative(value) or not (root / value).is_file():
                errors.append(f"{name}: missing/unsafe license path")
        if not component.get("license_files") or not component.get("supported_inputs"):
            errors.append(f"{name}: licenses and supported inputs required")
        for addition in component.get("local_additions", []):
            if not re.fullmatch(r"[0-9a-f]{40}", addition.get("commit", "")):
                errors.append(f"{name}: pin the local addition commit")
            value = addition.get("path", "")
            if not safe_relative(value):
                errors.append(f"{name}: unsafe addition path")
                continue
            source = root / value
            if not source.is_file():
                errors.append(f"{name}: missing addition source")
            elif hashlib.sha256(source.read_bytes()).hexdigest() != addition.get("sha256"):
                errors.append(f"{name}: addition source differs from reviewed lock")
    return errors


def forbidden_path(value):
    p = PurePosixPath(value)
    forbidden_dirs = {".secrets", "node_modules", ".gradle", ".venv", "__pycache__", "inputs", "artifacts"}
    if any(part in forbidden_dirs for part in p.parts):
        return True
    if p.name == "local.properties" or p.name == ".env" or p.name.startswith(".env."):
        return True
    return p.suffix.lower() in {".apk", ".xapk", ".apks", ".aab", ".idsig", ".keystore", ".jks", ".key", ".pem", ".p12", ".mpp", ".log"}


def local_links(text, file, root):
    errors = []
    targets = re.findall(r"!?\[[^\]]*\]\(([^\s)]+)", text)
    targets += re.findall(r"(?:href|src)=[\"']([^\"']+)", text)
    for target in targets:
        if target.startswith(("https://", "http://", "mailto:", "data:", "#")):
            continue
        clean = target.split("#", 1)[0].split("?", 1)[0]
        if not clean:
            continue
        resolved = (file.parent / clean).resolve()
        if not resolved.is_relative_to(root.resolve()) or not resolved.exists():
            errors.append(f"{file.relative_to(root)}: missing/unsafe link {target}")
    return errors


def main():
    errors = validate_lock(json.loads((ROOT / "upstreams.json").read_text()), ROOT)
    result = subprocess.run(["git", "ls-files", "-co", "--exclude-standard", "-z"], cwd=ROOT, check=True, capture_output=True)
    names = sorted(set(result.stdout.decode().split("\0")) - {""})
    private_markers = [r"/home/[^/]+/\.hermes(?:/|$)", r"gh[pousr]_[A-Za-z0-9]{30,}", r"github_pat_[A-Za-z0-9_]{40,}"]
    text_types = {".md", ".json", ".py", ".kts", ".kt", ".java", ".yml", ".yaml", ".toml", ".properties", ".svg"}
    for name in names:
        if forbidden_path(name):
            errors.append(f"Forbidden public artifact: {name}")
            continue  # Never open a forbidden credential/input file.
        file = ROOT / name
        if not file.is_file() or file.suffix not in text_types:
            continue
        text = file.read_text()
        if any(re.search(pattern, text) for pattern in private_markers):
            errors.append(f"Private marker in public file: {name}")
        if file.suffix == ".md":
            errors.extend(local_links(text, file, ROOT))
            if text.count("```") % 2:
                errors.append(f"Unbalanced code fence: {name}")
        if file.suffix == ".py":
            ast.parse(text, filename=name)
        elif file.suffix == ".json":
            json.loads(text)
    release = (ROOT / ".github/workflows/release.yml").read_text()
    if "  push:" in release or "  schedule:" in release:
        errors.append("Publication must be explicitly dispatched, not automatic")
    if "inputs.owner_verified" not in release:
        errors.append("Stable publication acceptance guard missing")
    readme = (ROOT / "README.md").read_text()
    for marker in ["<!-- PATCHES_START -->", "<!-- PATCHES_END -->"]:
        if readme.count(marker) != 1:
            errors.append("Maintained catalog generator markers missing/duplicated")
    for problem in errors:
        print("FAIL:", problem)
    if errors:
        return 1
    print(f"PASS: {len(names)} public files; pinned provenance, privacy baseline, local links and release guard")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
