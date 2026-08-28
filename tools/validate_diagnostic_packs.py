"""Validate offline diagnostic database packs before they can be published."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "diagnostics" / "manifest.json"


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as fh:
        value = json.load(fh)
    if not isinstance(value, dict):
        raise ValueError(f"{path}: root must be an object")
    return value


def main() -> None:
    manifest = load_json(MANIFEST)
    if manifest.get("schema_version") != 1:
        raise ValueError("Unsupported diagnostic manifest schema")

    packs = manifest.get("packs", [])
    if not isinstance(packs, list):
        raise ValueError("manifest.packs must be a list")

    seen: set[str] = set()
    for pack in packs:
        for key in ("id", "name_cs", "version", "path", "verification"):
            if not pack.get(key):
                raise ValueError(f"Pack is missing required field: {key}")
        pack_id = pack["id"]
        if pack_id in seen:
            raise ValueError(f"Duplicate pack id: {pack_id}")
        seen.add(pack_id)

        path = ROOT / "diagnostics" / pack["path"]
        if not path.is_file():
            raise ValueError(f"Missing pack file: {path}")
        data = load_json(path)
        if data.get("schema_version") != 1:
            raise ValueError(f"{path}: unsupported schema")
        if data.get("pack_id") != pack_id:
            raise ValueError(f"{path}: pack_id does not match manifest")
        if not isinstance(data.get("entries", []), list):
            raise ValueError(f"{path}: entries must be a list")

        for entry in data.get("entries", []):
            if not isinstance(entry, dict):
                raise ValueError(f"{path}: every entry must be an object")
            if not entry.get("code") or not entry.get("kind"):
                raise ValueError(f"{path}: entry requires code and kind")
            if "verification" not in entry:
                raise ValueError(f"{path}: {entry['code']} has no verification state")
            if "sources" not in entry:
                raise ValueError(f"{path}: {entry['code']} has no source list")

    print(f"Validated {len(packs)} diagnostic packs successfully.")


if __name__ == "__main__":
    main()
