#!/usr/bin/env python3
"""Inventory a private Tesla source archive without publishing its contents.

This tool intentionally does not crawl or scrape Tesla Service. It inventories
files the user has already obtained and is entitled to retain, producing
checksums and metadata for reproducibility.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
from datetime import datetime, timezone
from pathlib import Path

SKIP_NAMES = {"manifest.jsonl", "references.csv", "checksums.sha256"}


def sha256_file(path: Path, chunk_size: int = 1024 * 1024) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(chunk_size)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def inventory(root: Path):
    root = root.resolve()
    now = datetime.now(timezone.utc).isoformat()
    rows = []
    for path in sorted(root.rglob("*")):
        if not path.is_file() or path.name in SKIP_NAMES:
            continue
        rel = path.relative_to(root).as_posix()
        stat = path.stat()
        rows.append(
            {
                "path": rel,
                "size": stat.st_size,
                "sha256": sha256_file(path),
                "modifiedUtc": datetime.fromtimestamp(stat.st_mtime, timezone.utc).isoformat(),
                "inventoriedUtc": now,
            }
        )
    return rows


def write_jsonl(path: Path, rows) -> None:
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False, sort_keys=True) + "\n")


def write_csv(path: Path, rows) -> None:
    fields = ["path", "size", "sha256", "modifiedUtc", "inventoriedUtc"]
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def write_checksums(path: Path, rows) -> None:
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        for row in rows:
            handle.write(f"{row['sha256']}  {row['path']}\n")


def main() -> int:
    parser = argparse.ArgumentParser(description="Inventory a private Tesla archive")
    sub = parser.add_subparsers(dest="command", required=True)

    inv = sub.add_parser("inventory", help="hash and inventory files already present locally")
    inv.add_argument("--root", required=True, type=Path)
    inv.add_argument("--output", required=True, type=Path, help="JSONL manifest")
    inv.add_argument("--csv", required=True, type=Path, dest="csv_output")
    inv.add_argument("--checksums", type=Path)

    args = parser.parse_args()
    if args.command == "inventory":
        if not args.root.is_dir():
            parser.error(f"archive root does not exist or is not a directory: {args.root}")
        rows = inventory(args.root)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.csv_output.parent.mkdir(parents=True, exist_ok=True)
        write_jsonl(args.output, rows)
        write_csv(args.csv_output, rows)
        if args.checksums:
            args.checksums.parent.mkdir(parents=True, exist_ok=True)
            write_checksums(args.checksums, rows)
        print(f"Inventoried {len(rows)} files from {args.root}")
        return 0
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
