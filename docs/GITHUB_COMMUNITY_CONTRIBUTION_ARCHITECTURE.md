# GitHub Community Contribution Architecture

## Goal

AutoDiag should let a normal user contribute a repair experience without becoming a Git expert.
The app already knows the DTC, ECU, memory and vehicle scope. The user should only confirm what
was repaired and optionally select coarse cost/time information.

## Publishing model

1. AutoDiag creates a sanitized contribution.
2. VIN and direct identifiers are excluded before publication.
3. User explicitly chooses **Contribute via GitHub**.
4. Android authenticates with GitHub Device Flow using the configured GitHub App.
5. The GitHub App installation is limited to the selected community repository and `Issues: write`.
6. AutoDiag creates a structured Issue payload.
7. GitHub Actions can validate, normalize and aggregate accepted contributions into versioned
   knowledge snapshots consumed by the app.

Issues are an input channel, not the permanent knowledge database.

## Security boundaries

- Never put GitHub tokens in core models, logs, issues or crash reports.
- Do not request broad `public_repo` access when a GitHub App installation can use a narrower
  repository permission.
- Never upload VINs, owner identity, addresses, phone numbers, credentials or raw private logs.
- Receipt photos are not uploaded automatically; they require a separate, explicit privacy flow.
- Contributions remain `UNVERIFIED` until independent evidence supports a stronger status.
- Conflicting repair categories are shown as separate cases, never averaged into a fake consensus.

## Offline behavior

If GitHub is unavailable, the sanitized contribution remains locally queued. Publishing must be
retryable and idempotent so a temporary network failure does not create duplicate knowledge entries.

## Why this is useful

The same mechanism can collect repair outcomes across Škoda/VAG, Tesla, BMW, Hyundai/Kia,
Mercedes, Renault/Dacia, Nissan and other vehicle families while keeping the physical WiCAN
interface reusable.

## Legal/content boundary

Community contributions must be original user observations. The project must not mirror or
redistribute proprietary OEM manuals, subscription repair databases, cloned diagnostic software,
firmware binaries, security credentials or copyrighted CD databases merely because a user owns a
copy. The app can store structured facts, original observations, provenance and links to lawful
sources, subject to the applicable license and terms.
