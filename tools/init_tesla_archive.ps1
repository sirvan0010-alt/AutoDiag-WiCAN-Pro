param(
    [string]$Root = "$env:USERPROFILE\AutoDiag-Private\Tesla-Archive"
)

$ErrorActionPreference = "Stop"

$folders = @(
    "Model-S", "Model-3", "Model-X", "Model-Y", "Cybertruck", "Roadster",
    "by-year", "wiring", "parts", "diagnostics", "service-bulletins", "diy",
    "screenshots", "exports"
)

New-Item -ItemType Directory -Force -Path $Root | Out-Null
foreach ($folder in $folders) {
    New-Item -ItemType Directory -Force -Path (Join-Path $Root $folder) | Out-Null
}

@"
AutoDiag private Tesla archive

This directory is intentionally outside the public Git repository.
Store only material you are legally entitled to retain under the applicable source terms.
Do not upload the contents to the public AutoDiag repository.

Use tools/tesla_archive.py inventory to create a checksum manifest.
"@ | Set-Content -Encoding UTF8 (Join-Path $Root "README.txt")

Write-Host "Tesla private archive initialized: $Root"
Write-Host "Next: place your legally obtained source files into the folders, then run:"
Write-Host "python tools/tesla_archive.py inventory --root `"$Root`" --output `"$Root\manifest.jsonl`" --csv `"$Root\references.csv`" --checksums `"$Root\checksums.sha256`""
