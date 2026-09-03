#!/usr/bin/env python3
"""Inventory a locally stored workshop database/archive without copying its contents.

The tool is intentionally metadata-first: it reports files, sizes, extensions and
optionally SHA-256 hashes. It never uploads, extracts, or publishes the source data.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
from collections import Counter
from pathlib import Path


def sha256_file(path: Path, chunk_size: int = 1024 * 1024) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(chunk_size)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def classify(path: Path) -> str:
    ext = path.suffix.lower()
    names = {
        ".db": "database", ".sqlite": "database", ".sqlite3": "database",
        ".mdb": "access_database", ".accdb": "access_database",
        ".mdf": "sql_server_database", ".ldf": "sql_server_log",
        ".iso": "disk_image", ".img": "disk_image", ".bin": "binary",
        ".zip": "archive", ".7z": "archive", ".rar": "archive",
        ".cab": "archive", ".gz": "archive", ".tar": "archive",
        ".pdf": "pdf", ".html": "html", ".htm": "html",
        ".xml": "xml", ".json": "json", ".csv": "csv",
        ".txt": "text", ".ini": "config", ".cfg": "config",
        ".dll": "library", ".exe": "executable", ".dat": "data",
    }
    return names.get(ext, "other")


def main() -> int:
    parser = argparse.ArgumentParser(description="Inventory a local workshop archive")
    parser.add_argument("root", help="Local archive directory or file")
    parser.add_argument("--output", default=None, help="JSONL inventory output")
    parser.add_argument("--csv", dest="csv_output", default=None, help="CSV inventory output")
    parser.add_argument("--hash", action="store_true", help="Calculate SHA-256 for every file")
    args = parser.parse_args()

    root = Path(args.root).expanduser().resolve()
    if not root.exists():
        print(f"ERROR: path does not exist: {root}")
        return 2

    files = [root] if root.is_file() else [p for p in root.rglob("*") if p.is_file()]
    files.sort(key=lambda p: str(p).lower())

    records = []
    type_counts = Counter()
    extension_counts = Counter()
    total_bytes = 0

    for path in files:
        stat = path.stat()
        category = classify(path)
        extension = path.suffix.lower() or "[none]"
        total_bytes += stat.st_size
        type_counts[category] += 1
        extension_counts[extension] += 1
        record = {
            "path": str(path),
            "relative_path": str(path.relative_to(root.parent if root.is_file() else root)),
            "size_bytes": stat.st_size,
            "extension": extension,
            "category": category,
        }
        if args.hash:
            record["sha256"] = sha256_file(path)
        records.append(record)

    summary = {
        "schema_version": 1,
        "root": str(root),
        "file_count": len(files),
        "total_bytes": total_bytes,
        "categories": dict(sorted(type_counts.items())),
        "extensions": dict(sorted(extension_counts.items())),
    }

    if args.output:
        output = Path(args.output).expanduser().resolve()
        output.parent.mkdir(parents=True, exist_ok=True)
        with output.open("w", encoding="utf-8") as handle:
            handle.write(json.dumps({"summary": summary}, ensure_ascii=False) + "\n")
            for record in records:
                handle.write(json.dumps(record, ensure_ascii=False) + "\n")

    if args.csv_output:
        output = Path(args.csv_output).expanduser().resolve()
        output.parent.mkdir(parents=True, exist_ok=True)
        fields = ["path", "relative_path", "size_bytes", "extension", "category", "sha256"]
        with output.open("w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields)
            writer.writeheader()
            writer.writerows(records)

    print(f"Workshop archive: {root}")
    print(f"Files: {len(files)}")
    print(f"Total bytes: {total_bytes:,}")
    print("Categories:")
    for key, value in sorted(type_counts.items()):
        print(f"  {key}: {value}")
    print("Top extensions:")
    for key, value in extension_counts.most_common(20):
        print(f"  {key}: {value}")
    if args.output:
        print(f"JSONL: {Path(args.output).resolve()}")
    if args.csv_output:
        print(f"CSV: {Path(args.csv_output).resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
