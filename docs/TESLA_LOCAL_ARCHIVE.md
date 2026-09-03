# Tesla local archive — private/reference workflow

## Purpose

AutoDiag keeps Tesla Service references as links and can work with a **private local archive** of material that the user is legally entitled to retain. The public repository must not become a mirror of Tesla's manuals, EPC content, paid diagnostic content, images, or other copyrighted/proprietary material.

The official Tesla Service portal currently exposes Service Manuals, DIY Guides, Parts Catalog, Wiring Diagrams and diagnostic/service information. See: https://service.tesla.com/

Tesla's published service terms state that its service information is protected intellectual property and restrict reproduction, copying, redistribution and systematic retrieval. Therefore this project deliberately separates **reference URLs** from **private local source files**.

## What AutoDiag should store publicly

- canonical source URL;
- source title;
- vehicle family/model/generation;
- region/language;
- source type (manual, wiring, parts, diagnostic, bulletin, etc.);
- date observed;
- version/revision if exposed by the source;
- local archive identifier, when the user has a private copy;
- SHA-256 checksum of a private file when the user chooses to record it;
- provenance/license/usage note;
- an original AutoDiag interpretation or structured fact derived from the source, when legally appropriate.

Do **not** commit the private source itself to this repository.

## Recommended private directory

Keep this outside the Git repository, for example:

```text
D:\AutoDiag-Private\Tesla-Archive\
  references.csv
  manifest.jsonl
  checksums.sha256
  README.txt
  Model-S\
  Model-3\
  Model-X\
  Model-Y\
  Cybertruck\
  Roadster\
  by-year\
  wiring\
  parts\
  diagnostics\
  service-bulletins\
  diy\
  screenshots\
  exports\
```

Use the exact model/year/region hierarchy that makes sense for the files actually obtained.

## Automatic local ingestion

The repository contains `tools/tesla_archive.py`. It is intentionally an **ingestion and inventory tool**, not a Tesla web scraper. It can:

1. scan a private archive recursively;
2. identify files and sizes;
3. calculate SHA-256 checksums;
4. record modification time;
5. create a machine-readable JSONL manifest;
6. create a human-readable CSV inventory;
7. detect duplicate content by checksum;
8. preserve source metadata supplied by the user.

Example:

```powershell
python tools/tesla_archive.py inventory `
  --root D:\AutoDiag-Private\Tesla-Archive `
  --output D:\AutoDiag-Private\Tesla-Archive\manifest.jsonl `
  --csv D:\AutoDiag-Private\Tesla-Archive\references.csv
```

The resulting manifest is useful for proving which local file was used as a source without publishing that file.

## URL/reference registry

Maintain a separate public registry containing URLs only. Example record:

```json
{
  "sourceId": "tesla-service-model-3-2017-2023",
  "publisher": "Tesla",
  "title": "Model 3 Service Manual",
  "url": "https://service.tesla.com/docs/Model3/ServiceManual/en-us/",
  "type": "SERVICE_MANUAL",
  "model": "Model 3",
  "region": "en-US",
  "access": "PUBLIC_REFERENCE",
  "localCopy": true,
  "redistribution": "DO_NOT_DISTRIBUTE",
  "notes": "Private local archive may contain a separately obtained copy; public repo stores reference metadata only."
}
```

`localCopy=true` means only that the owner has a local file; it does not authorize redistribution.

## What should happen after archiving

The private archive is the source material. AutoDiag's public knowledge layer should contain only independently authored structured knowledge, for example:

```text
source
  -> vehicle/model scope
  -> subsystem
  -> DTC / symptom / condition
  -> diagnostic observation
  -> repair concept
  -> part category / OEM reference where legally publishable
  -> labor/time/cost observation
  -> safety notes
  -> provenance
  -> verification status
```

Do not copy whole Tesla procedures into the public knowledge base. Instead, create an original summary or structured fact and retain a reference to the source.

## Important distinction

A local backup is not automatically a redistribution right. Tesla's service website terms expressly address copying, systematic retrieval and redistribution, and Tesla's EPC terms also restrict saving/downloading/copying its content. Check the terms that apply to the particular source, subscription and jurisdiction before making a bulk local copy.

For paid/subscription material, the safest project design is:

```text
Tesla / licensed source
        |
        +--> user's permitted private archive
        |
        +--> public AutoDiag reference URL + metadata
        |
        +--> original AutoDiag facts/knowledge (not copied text)
```

## Why this design is valuable

It gives the project long-term resilience without turning GitHub into a copyrighted document mirror. A local archive can be backed up with normal disk/cloud backup, while the public project remains small, searchable, reproducible and license-aware.
