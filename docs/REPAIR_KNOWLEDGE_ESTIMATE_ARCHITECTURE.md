# Repair Knowledge, Parts & Cost Architecture

## Purpose

AutoDiag-WiCAN-Pro should not stop at displaying a DTC. After a fault is read and identified for the exact vehicle scope, the application should provide a **repair intelligence card** containing, where reliable data exists:

- DTC / manufacturer fault code and ECU
- plain-language meaning
- severity and drivability/safety relevance
- probable causes, clearly separated from confirmed findings
- diagnostic checks before replacing parts
- required parts and OEM/aftermarket part numbers
- estimated labor time
- indicative labor cost
- indicative parts cost
- estimated total repair range
- required tools / special tools
- mechanical and/or electrical repair procedure
- wiring/connector information when available
- calibration/programming/adaptation requirements when documented
- post-repair verification steps
- direct links to the best available source for the exact vehicle
- source, revision/date, license/access status and verification level

The goal is to make AutoDiag useful to a skilled mechanic/electrician working at home or in an independent workshop, while never presenting an estimate or repair procedure as certain when the source data is uncertain.

## Important distinction: diagnosis vs repair recommendation

A DTC does **not** automatically identify the failed component. AutoDiag must first show the diagnostic evidence and possible causes, then the repair paths.

Example:

```text
Pxxxx — [vehicle-specific description]
ECU: Engine ECU

Observed:
  DTC reported by vehicle
  Freeze-frame: ...
  Live data: ...

Likely causes:
  1. ...
  2. ...
  3. ...

Before replacing a part:
  [ ] Check connector
  [ ] Check supply/ground
  [ ] Check wiring
  [ ] Run manufacturer test

Repair candidates:
  Part A — evidence: verified
  Part B — evidence: possible
```

A code-to-part shortcut is allowed only when the source explicitly supports it or repeated validated evidence is available.

## Exact-vehicle matching

Repair information must be matched as narrowly as possible using:

- VIN
- manufacturer
- model
- model generation/platform
- model year
- production date/range
- engine/motor
- battery variant where relevant
- transmission
- drivetrain
- region/market
- ECU/module
- software/firmware version when relevant
- diagnostic protocol/addressing where relevant

A generic DTC description must never be silently presented as an exact manufacturer repair instruction.

## Repair Source hierarchy

Preferred order:

1. **Official OEM service manual / technical documentation**
2. Official OEM diagnostic/service portal
3. Licensed professional repair databases containing OEM information
4. Manufacturer technical bulletins / recalls / service campaigns
5. Reputable professional repair databases and verified repair cases
6. Verified community/reverse-engineering sources
7. General web pages/videos/forums only as supplementary evidence

The source record must retain provenance. Community information must never be promoted to OEM fact merely because it is popular.

## Tesla example

Tesla demonstrates the desired model particularly well. The public Model Y Service Manual contains vehicle-specific procedures, warnings, required tools, torque specifications, correction codes and procedure hierarchy. It also indicates when a procedure is mobile-service capable. For example, the manual explicitly states that procedures include special tools, torque figures and assembly details, and it distinguishes mobile-capable procedures. See the official Tesla documentation linked from the application rather than copying the manual into the repository.

Official entry points:

- Tesla Service: https://service.tesla.com/
- Tesla Model Y Service Manual: https://service.tesla.com/en-US/vehicle-models/ModelY

For a matched repair, AutoDiag should open the exact procedure where possible. For example, Tesla procedures expose correction codes and flat-rate time information, while individual procedures can contain equipment, torque values, removal/install steps and safety warnings.

## Other manufacturers

The same architecture must work for VAG, Škoda, Volkswagen, Audi, SEAT/CUPRA, BMW, Mercedes-Benz, Hyundai/Kia, Renault/Dacia, Nissan, Mitsubishi, Toyota, Ford, GM, Stellantis brands, Volvo/Polestar and other manufacturers.

The app should not assume that every OEM has the same public-access model. Some information will be freely accessible, some will require an account/subscription, and some will only be available through licensed professional data providers.

The UI therefore uses a **source resolver** rather than one hard-coded repair website.

## Source resolver

```text
Vehicle + VIN + DTC + ECU + component
                |
                v
        Repair Source Resolver
                |
       +--------+--------+
       |        |        |
      OEM    Licensed   Community
     portal    data      verified
       |        |        |
       +--------+--------+
                |
                v
      Matched repair records
                |
                v
       Repair Intelligence UI
```

A source can be represented as:

```json
{
  "source_id": "oem:tesla:model-y:service-manual",
  "provider": "Tesla",
  "type": "oem_service_manual",
  "vehicle_scope": {
    "make": "Tesla",
    "model": "Model Y",
    "year_from": 2020,
    "year_to": 2024
  },
  "access": "public",
  "url": "https://service.tesla.com/en-US/vehicle-models/ModelY",
  "verification": "source_verified"
}
```

## Parts data

Every proposed part should carry:

- component name
- OEM part number
- manufacturer part number where available
- supersession history where available
- quantity
- mandatory/optional status
- new/reuse/discard status
- source
- price currency
- price region
- price date
- price type (OEM list, dealer, aftermarket, marketplace, user-entered)
- availability/link where available
- confidence

Do not claim that one part number fits every vehicle bearing the same DTC.

### Parts display

```text
Potenciální díly

1. Snímač EGR
   OEM: XXXXXXXX
   Množství: 1
   Cena dílu: 2 490–3 790 Kč
   Zdroj ceny: [zdroj]
   Vhodnost: přesně pro VIN / varianta vozidla

2. Těsnění EGR
   OEM: XXXXXXXX
   Množství: 1
   Cena: 180–320 Kč
   Povinná výměna: ANO, dle postupu OEM
```

Prices are **indicative**, not guaranteed. Actual prices vary by country, supplier, availability and date.

## Labor and repair time

AutoDiag should support several time classes:

- OEM flat-rate/warranty time
- professional published standard labor time
- estimated independent-shop time
- user-configured personal time estimate

Never mix these into one unlabeled number.

Example:

```text
Práce
OEM / published time: 1.8 h
Independent estimate: 1.5–2.5 h
User labor rate: 850 Kč/h
Indicative labor cost: 1 275–2 125 Kč
```

The application should allow the user to set:

- currency
- country/region
- workshop hourly rate
- independent vs dealer rate
- VAT inclusion
- parts markup
- preferred parts source

## Total estimate

The estimate engine should calculate a transparent range:

```text
Díly:        2 670–4 110 Kč
Práce:       1 275–2 125 Kč
Diagnostika:    0–850 Kč
--------------------------------
Odhad celkem: 3 945–7 085 Kč
```

Additional costs such as programming, calibration, consumables, towing or specialist equipment must be separate line items when known.

The UI must show **why a range exists** rather than pretending to know an exact repair price.

## Repair procedure record

```text
RepairProcedure
  id
  title
  vehicle_scope
  DTC_scope[]
  ECU_scope[]
  component_scope[]
  procedure_type
      diagnostic
      mechanical
      electrical
      HV_EV
      calibration
      programming
  source
  source_url
  access_type
      public
      login_required
      subscription_required
  revision/date
  estimated_time
  correction_code
  required_tools[]
  required_parts[]
  safety_level
  prerequisites[]
  steps[]
  torque_specs[]
  wiring_refs[]
  post_repair_checks[]
  verification
```

## Electrical and EV repairs

The same repair card must support electrical faults, not only mechanical parts.

Examples:

- power/ground checks
- fuse and relay identification
- connector/pin information
- wiring continuity/short-to-ground/short-to-power tests
- sensor reference voltage
- CAN/CAN-FD network diagnosis
- LIN/K-Line where applicable
- module replacement
- calibration/adaptation
- HV isolation procedures
- HV battery service procedures
- inverter/drive-unit procedures
- charging-system procedures
- thermal-system procedures

High-voltage procedures must display OEM safety prerequisites and required certification/PPE where the source specifies them. The app must not turn an HV procedure into a casual home-repair instruction when the source explicitly requires qualified technicians.

## DTC UI

When a DTC is opened:

```text
┌──────────────────────────────────────┐
│ Pxxxx  ⚠ Engine / ECU               │
│ [Vehicle-specific description]       │
├──────────────────────────────────────┤
│ Severity: Medium                     │
│ Status: Stored / Active              │
│ Evidence: Vehicle reported           │
├──────────────────────────────────────┤
│ Co může být příčinou                  │
│ • ...                                 │
│ • ...                                 │
├──────────────────────────────────────┤
│ Co zkontrolovat jako první            │
│ 1. ...                                │
│ 2. ...                                │
├──────────────────────────────────────┤
│ Odhad opravy                          │
│ Díly:  2 490–3 790 Kč                 │
│ Práce: 1.5–2.5 h                       │
│ Celkem: 3 765–5 915 Kč                │
├──────────────────────────────────────┤
│ Potřebné díly                         │
│ • OEM XXXXXXXX                        │
│ • těsnění XXXXXXXX                    │
├──────────────────────────────────────┤
│ Oprava                                │
│ [OEM postup] [Elektrické schéma]      │
│ [Diagnostika] [Video / další zdroje] │
└──────────────────────────────────────┘
```

## Source links vs copied content

The repository must not copy large portions of copyrighted OEM manuals into the application unless licensing explicitly permits it.

Preferred implementation:

- store source metadata
- store direct links
- store short factual metadata needed for matching
- store licensed data only under the applicable license
- store internally authored summaries where legally permitted
- preserve source/version/provenance
- show the original source for the authoritative procedure

This makes the application extensible to all manufacturers without turning the GitHub repository into an unauthorized mirror of proprietary repair manuals.

## Licensed professional data integration

A serious all-brand implementation should support licensed providers rather than relying only on public web links. Examples of the type of data required are:

- OEM repair procedures
- DTC diagnostic flows
- wiring diagrams
- parts and labor
- service information
- technical service bulletins
- vehicle identification/coverage

Current market examples include ALLDATA and Mitchell 1 ProDemand. ALLDATA advertises OEM repair information, DTCs, wiring diagrams, parts and labor and OEM labor times; Mitchell 1 ProDemand advertises OEM repair information, estimating, OEM parts pricing, labor times and interactive wiring/diagnostic information. These should be treated as potential commercial integrations, not as data that AutoDiag may copy without a license.

## Offline-first behavior

If the user is offline:

- show locally installed DTC/repair knowledge
- show cached source metadata
- show cached estimates with their price date
- clearly mark stale prices
- allow the user to open previously cached/public procedures
- do not pretend that a current price or online source was checked

When online:

- resolve the best source for the exact vehicle
- refresh prices if a supported provider is available
- refresh source revision information
- preserve the previous result if the update fails

## Verification states

Repair information uses explicit states:

- `SOURCE_VERIFIED` — source is authentic/identified
- `VEHICLE_SCOPE_VERIFIED` — source matches the vehicle scope
- `PROCEDURE_VERIFIED` — procedure has been validated for that scope
- `ESTIMATED` — AutoDiag calculated a value such as total price
- `COMMUNITY_REPORTED` — community evidence only
- `UNVERIFIED` — insufficient validation
- `STALE` — source/price needs refresh

The UI must distinguish these states visually and in text.

## Implementation priority

### P0

1. Extend the existing DTC knowledge base with `repairProcedureRefs`, `partsRefs`, `laborTimeRefs`, `sourceRefs`.
2. Add a vehicle-scoped repair source model.
3. Add estimate model for parts + labor + diagnostics.
4. Add price provenance and date.
5. Add direct source links from DTC detail.

### P1

1. OEM source resolver.
2. VIN/vehicle exact-match filtering.
3. Repair procedure viewer/link-out.
4. Parts list and OEM part-number model.
5. Labor-time model.
6. User-configurable hourly labor rate and currency.
7. Cached/offline repair records.
8. Mechanical + electrical + EV/HV procedure categories.

### P2

1. Licensed provider adapters.
2. Parts price aggregation.
3. Local supplier search.
4. Automatic estimate generation.
5. Wiring-diagram references.
6. TSB/recall/service-campaign matching.
7. Repair history and actual-vs-estimated cost learning.

## Safety rule

The app may provide information that helps a skilled person diagnose and repair a vehicle, but it must not convert uncertain evidence into a definitive diagnosis or encourage unsafe work. For brakes, steering, airbags, HV systems and other safety-critical systems, the UI must prominently show source warnings, prerequisites and qualification requirements when the source specifies them.

## Current repository status

The existing repository already has an offline-first DTC/knowledge-base architecture with manufacturer packs, source provenance and explicit `REPORTED`, `ESTIMATED`, `VEHICLE_DID_NOT_PROVIDE`, `UNVERIFIED` and `VERIFIED` states. This document extends that architecture with the missing **repair procedure + parts + labor + price + source resolver** layer. The existing rule against guessed manufacturer DIDs, CAN IDs, thresholds and repair instructions remains in force.
