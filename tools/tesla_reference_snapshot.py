#!/usr/bin/env python3
"""Save explicitly supplied Tesla reference URLs into a private local archive.

This tool deliberately does NOT crawl Tesla Service, follow site links, or
perform bulk/systematic retrieval. Use it only for individual URLs you are
legally entitled to retain under the applicable source terms.

The downloaded files stay on the local machine. A JSON manifest records URL,
local path, timestamp, byte count and SHA-256 for reproducibility.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import urllib.request
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import urlparse


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def safe_name(url: str, index: int) -> str:
    parsed = urlparse(url)
    path = parsed.path.strip("/")
    base = Path(path).name if path else "index"
    base = re.sub(r"[^A-Za-z0-9._-]+", "_", base)[:120] or "index"
    return f"{index:04d}_{base}.html"


def main() -> int:
    parser = argparse.ArgumentParser(description="Snapshot explicitly supplied Tesla reference URLs")
    parser.add_argument("--url", action="append", default=[], help="single URL; may be repeated")
    parser.add_argument("--urls-file", type=Path, help="UTF-8 text file with one URL per line")
    parser.add_argument("--root", type=Path, required=True, help="private archive directory")
    parser.add_argument("--user-agent", default="AutoDiag-WiCAN-Pro private reference snapshot/1.0")
    args = parser.parse_args()

    urls = list(args.url)
    if args.urls_file:
        urls.extend(
            line.strip()
            for line in args.urls_file.read_text(encoding="utf-8").splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        )

    if not urls:
        parser.error("provide at least one --url or --urls-file")

    if any(not u.startswith(("https://", "http://")) for u in urls):
        parser.error("only http(s) URLs are accepted")

    args.root.mkdir(parents=True, exist_ok=True)
    records = []

    for index, url in enumerate(urls, start=1):
        request = urllib.request.Request(url, headers={"User-Agent": args.user_agent})
        with urllib.request.urlopen(request, timeout=30) as response:
            data = response.read()
            content_type = response.headers.get("Content-Type", "")
            final_url = response.geturl()

        filename = safe_name(final_url, index)
        destination = args.root / filename
        destination.write_bytes(data)
        records.append(
            {
                "url": url,
                "finalUrl": final_url,
                "localPath": destination.name,
                "contentType": content_type,
                "bytes": len(data),
                "sha256": sha256(data),
                "savedUtc": datetime.now(timezone.utc).isoformat(),
            }
        )
        print(f"Saved {len(data):,} bytes: {url}")

    manifest = args.root / "snapshot-manifest.json"
    manifest.write_text(
        json.dumps(
            {"schema_version": 1, "records": records},
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(f"Snapshot complete: {len(records)} explicit URLs")
    print(f"Manifest: {manifest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
