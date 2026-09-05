#!/usr/bin/env python3
"""Reproducible, non-proprietary static inventory extractor for Android XAPK files.

Outputs artifact/APK hashes, native ELF metadata, project debug source paths,
QML cache units and native symbols. It deliberately does not copy vendor
source, binaries or UI assets into AutoDiag.
"""
from __future__ import annotations
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, zipfile


def sha256(path: str) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def run(*cmd: str) -> str:
    p = subprocess.run(cmd, text=True, capture_output=True, errors="replace")
    return p.stdout


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("xapk")
    ap.add_argument("-o", "--output", required=True)
    args = ap.parse_args()
    os.makedirs(args.output, exist_ok=True)

    with tempfile.TemporaryDirectory() as td:
        with zipfile.ZipFile(args.xapk) as z:
            z.extractall(td)
            members = z.namelist()

        apks = []
        native = []
        source_paths = set()
        qml_units = set()
        symbols = set()

        for name in sorted(x for x in members if x.lower().endswith(".apk")):
            apk_path = os.path.join(td, name)
            apks.append({"file": name, "size": os.path.getsize(apk_path), "sha256": sha256(apk_path)})
            with zipfile.ZipFile(apk_path) as apk:
                for member in apk.namelist():
                    if not (member.startswith("lib/") and member.endswith(".so")) or "S3XYButtons" not in member:
                        continue
                    native_dir = os.path.join(td, "native")
                    os.makedirs(native_dir, exist_ok=True)
                    so_path = os.path.join(native_dir, os.path.basename(member))
                    if not os.path.exists(so_path):
                        with apk.open(member) as src, open(so_path, "wb") as dst:
                            shutil.copyfileobj(src, dst)
                    native.append({"path": member, "size": os.path.getsize(so_path), "sha256": sha256(so_path)})
                    decoded = run("readelf", "--debug-dump=decodedline", so_path)
                    for line in decoded.splitlines():
                        m = re.match(r"(/Users/[^:]+/S3XYButtons/[^:]+\\.(?:cpp|cc|h|hpp)):", line)
                        if m and "/Libs/" not in m.group(1):
                            source_paths.add(m.group(1))
                        q = re.search(r"S3XYButtons_qml_(.+)_qml\\.cpp", line)
                        if q:
                            qml_units.add(q.group(1))
                    for line in run("readelf", "-Ws", so_path).splitlines():
                        parts = line.split()
                        if len(parts) > 7 and parts[3] == "FUNC" and parts[6] != "UND":
                            symbols.add(parts[7])

        result = {
            "artifact": os.path.basename(args.xapk),
            "sha256": sha256(args.xapk),
            "size": os.path.getsize(args.xapk),
            "apks": apks,
            "native": native,
            "counts": {"native_symbols": len(symbols), "project_source_paths": len(source_paths), "qml_cache_units": len(qml_units)},
            "project_source_paths": sorted(source_paths),
            "qml_cache_units": sorted(qml_units),
            "native_symbols": sorted(symbols),
        }
        with open(os.path.join(args.output, "inventory.json"), "w", encoding="utf-8") as f:
            json.dump(result, f, indent=2, ensure_ascii=False)


if __name__ == "__main__":
    main()
