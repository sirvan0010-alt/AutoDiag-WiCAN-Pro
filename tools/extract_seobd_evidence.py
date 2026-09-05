#!/usr/bin/env python3
"""Reproducible static evidence extractor for SEOBD research.

Usage: python extract_seobd_evidence.py input.xapk output_dir

It extracts APKs, inventories native ELF files, records SHA-256 hashes,
collects strings/symbols when host binutils are available, and emits only
candidate evidence. It never labels a protocol or signal as verified.
"""
import hashlib, json, os, re, shutil, subprocess, sys, zipfile
from pathlib import Path


def sha256(path):
    h=hashlib.sha256()
    with open(path,'rb') as f:
        for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
    return h.hexdigest()


def run(cmd, out):
    try:
        with open(out,'w',encoding='utf-8',errors='ignore') as f:
            subprocess.run(cmd,stdout=f,stderr=subprocess.STDOUT,check=False)
    except FileNotFoundError:
        Path(out).write_text('tool unavailable: '+cmd[0]+'\n',encoding='utf-8')


def main():
    if len(sys.argv)!=3:
        raise SystemExit('usage: extract_seobd_evidence.py input.xapk output_dir')
    xapk=Path(sys.argv[1]).resolve(); out=Path(sys.argv[2]).resolve(); out.mkdir(parents=True,exist_ok=True)
    pkgs=out/'packages'; pkgs.mkdir(exist_ok=True)
    with zipfile.ZipFile(xapk) as z: z.extractall(pkgs)
    extracted=out/'extracted'; extracted.mkdir(exist_ok=True)
    for name in ('base.apk','split_0.apk','split_1.apk'):
        p=pkgs/name
        if not p.exists(): continue
        d=extracted/name.replace('.apk',''); d.mkdir(exist_ok=True)
        with zipfile.ZipFile(p) as z: z.extractall(d)
    files=[]
    for p in extracted.rglob('*'):
        if p.is_file(): files.append({'path':str(p.relative_to(extracted)),'size':p.stat().st_size,'sha256':sha256(p)})
    (out/'file_inventory.json').write_text(json.dumps(files,indent=2),encoding='utf-8')
    native=[p for p in extracted.rglob('*.so')]
    manifest={'input':xapk.name,'input_sha256':sha256(xapk),'native_files':[],'status':'STATIC_EVIDENCE_ONLY'}
    for so in native:
        safe=so.name
        manifest['native_files'].append({'path':str(so.relative_to(extracted)),'size':so.stat().st_size,'sha256':sha256(so)})
        run(['readelf','-h',str(so)],out/f'{safe}.elf_header.txt')
        run(['readelf','-S',str(so)],out/f'{safe}.elf_sections.txt')
        run(['readelf','-Ws',str(so)],out/f'{safe}.elf_symbols.txt')
        run(['nm','-C','--defined-only',str(so)],out/f'{safe}.nm.txt')
        run(['strings','-n','4',str(so)],out/f'{safe}.strings.txt')
    (out/'extraction_manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
    print(json.dumps(manifest,indent=2))

if __name__=='__main__': main()
