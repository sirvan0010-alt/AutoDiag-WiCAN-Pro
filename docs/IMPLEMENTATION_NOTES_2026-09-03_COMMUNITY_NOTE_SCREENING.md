# Implementační poznámka: kontrola volného textu před publikací na GitHub

Datum: 2026-09-03
Navazuje na: `docs/GITHUB_COMMUNITY_CONTRIBUTION_ARCHITECTURE.md`, sekci
„Security boundaries“ — konkrétně požadavek *„Never upload VINs, owner
identity, addresses, phone numbers, credentials or raw private logs“*.

## Zjištěná mezera

`GitHubContributionPublisher.buildIssue()` bral pole `note` (volný text, který
si uživatel sám napíše k opravě) doslovně a bez kontroly. Protože issue vzniká
pod GitHub identitou přispěvatele a zůstává veřejně a natrvalo viditelné,
jakákoli náhodou vložená VIN, telefon, e-mail apod. by unikla přesně tím
kanálem, který dokumentace explicitně zakazuje.

## Řešení

- `CommunityNoteScreening.kt` — heuristická (ne dokonalá) kontrola na VIN-like
  řetězce, e-maily, telefonní čísla a dlouhé číselné sekvence. **Netiše
  neredukuje/nemaskuje text** — to by dalo falešný pocit bezpečí. Místo toho
  vrací nálezy, které má UI vrstva ukázat uživateli k potvrzení/úpravě.
- `GitHubContributionPublisher.preparePublish(contribution,
  userConfirmedNoteIsSafe)` — nový, doporučený vstupní bod. Vrací
  `ContributionPublishResult.NeedsReview` místo payloadu, dokud uživatel
  podezřelý text nepotvrdí nebo needituje.
- Původní `buildIssue()` zůstává beze změny kvůli zpětné kompatibilitě se
  stávajícími testy a případnými voláními, ale je zdokumentováno, že
  neprovádí kontrolu a UI vrstva má používat `preparePublish`.

## Co zůstává otevřené

- Skutečné UI pro „Přispět přes GitHub“ (tlačítko, GitHub Device Flow
  obrazovka, zobrazení `NeedsReview` nálezů uživateli) zatím není
  naimplementované — tahle revize řeší core vrstvu, na které má UI stát.
- Kontrola je heuristická; nezachytí např. jméno a příjmení v běžném textu.
  UI text by měl uživatele srozumitelně upozornit, že poznámka bude veřejná a
  natrvalo spojená s jeho GitHub účtem, bez ohledu na to, co screening najde.
