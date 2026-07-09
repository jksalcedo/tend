# Translations (i18n) — Gherkin specs

These `.feature` files specify the work needed to make Tend translatable
and to integrate a Weblate instance for managing those translations. They
follow the same convention as `docs/specs/contact-sync/`: written for a
first contribution to this repo, **not wired to an executable
Cucumber/Gherkin runner** — treat them as acceptance criteria to implement
against and/or convert to instrumented tests later. Alongside each
`NN_name.feature` file is a **Manual Test Plan** (`NN_name.manual-tests.md`)
once that feature is implemented.

This effort is at an earlier stage than contact-sync was when its spec
folder was created — there has been no stakeholder interview yet, and
several real decisions (exact sync workflow, whether an in-app language
override is wanted) are still open. Rather than invent answers, this
README states plainly what's confirmed and what isn't; see "Open
questions" below before assuming any unstated behavior.

## Confirmed starting state (2026-07-09)

- A Weblate server has been stood up at **https://weblate.tend.farband.ca**,
  self-hosted on AWS. It is explicitly a **proof of concept** — not the
  permanent home for this project's translations. Expect the URL and
  possibly the whole hosting arrangement to change as this matures.
- Access is per-user: the team will issue individual credentials to each
  translator/contributor as they're added. No credentials — shared or
  individual — are stored in this repository. See "Credentials" below for
  where they _do_ live.
- **Tend currently has zero externalized strings.** `app/src/main/res/values/strings.xml`
  contains only the auto-generated `app_name` entry; every other piece of
  UI text (48+ call sites as of this writing) is a hardcoded string literal
  passed directly to `Text(...)` in Compose code. Nothing is translatable
  yet — string externalization is a genuine prerequisite, not a formality,
  and is spec'd as `01` below.
- Android's own resource resolution already provides locale fallback for
  free once strings _are_ externalized: any string missing from a
  locale-specific `values-xx/strings.xml` automatically resolves to the
  base `values/strings.xml` entry for that key. This requires no custom
  code — see the design-decisions table below — but only works correctly
  if the base `values/strings.xml` is complete, which is exactly what `01`
  establishes.

## Credentials

Nothing Weblate-related should ever be committed to this repo — not a
server URL baked into app code (there's no reason the Android app would
ever talk to Weblate at runtime; this is a repo/tooling-level concern, not
an in-app one), and not any token or password.

For anything that _does_ need local file storage (e.g. a personal Weblate
API token for future CLI/automation use), the standard convention applies:

- **`.env.example`** (repo root, committed) — documents the variable names
  expected, with placeholder/blank values. Copy it to `.env` locally and
  fill in your own values.
- **`.env`** (repo root, gitignored, never committed) — your personal,
  local secrets. Each contributor has their own; nobody else's values ever
  touch this repo.

Because the current server is a known-temporary POC, keep any credentials
granted to it narrowly scoped and easy to rotate — this matters more here
than usual, precisely because the server is expected to be replaced.

## Design decisions

| Question                                                | Decision                                                                                                                                                                                          |
| ------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Missing or incomplete translation for a given string    | Handled automatically by Android's resource resolution — falls back to the base (default-locale) `values/strings.xml` for that key. No custom fallback logic is needed or should be built.        |
| Where the base/source-of-truth language lives           | `app/src/main/res/values/strings.xml` (no locale qualifier) is the single source of truth Weblate translates _from_. Every string must exist here before it can be translated into anything else. |
| Server URL / credentials in the repo                    | Never. The current POC URL is documented here and in `.env.example` as a placeholder value only — see "Credentials" above.                                                                        |
| Coupling repo tooling to this specific Weblate instance | Deliberately avoided for now. No CI workflow or webhook is being built against `weblate.tend.farband.ca` specifically until a permanent host is chosen — see Non-Goals.                           |
| Initial language set                                    | English is the existing source language (already `01`'s base `values/strings.xml`) — nothing to translate there. A second language will be picked specifically to prove the pipeline works end to end, not as a commitment to a particular launch-language set. Which second language is still open — see "Open questions."                           |
| Scope of `01`'s string externalization                  | Every hardcoded string in the app as it exists today — not a per-screen subset. One complete pass, not an incremental rollout; anything added after `01` lands is covered by the "no new hardcoded strings" scenario already in the feature file.                                                |

## Non-Goals (for now)

- **No CI/automation wired to this specific Weblate server.** Because it's
  an explicitly temporary POC, building a GitHub Action or webhook
  integration against it now would just mean redoing that work when the
  permanent instance exists. Once the team has a stable host, that
  integration becomes its own spec item.
- **No in-app language override UI**, unless a future spec establishes one
  is wanted — this repo's scope for now is making strings translatable and
  getting a translation pipeline working, not necessarily giving users a
  way to pick a language independent of their device's system locale.
  Android already respects the system locale automatically once strings
  are externalized; anything beyond that is a separate, undecided feature.

## Open questions

Not yet decided — do not assume answers to these when implementing:

- Which specific second language will be used to prove the pipeline works?
  (English is confirmed as the source language — see the design-decisions
  table above — but the validation language beyond that isn't picked yet.)
- Does Weblate push translated strings back via an automatic PR/commit, or
  is syncing a manual export/import step? This depends on the permanent
  hosting decision and hasn't been discussed yet.
- Who besides the team lead can invite new Weblate users, and what's the
  process?

## Feature files (numbered in suggested implementation order)

1. **`01_string_externalization.feature`** — build this first; nothing
   else in this folder is possible without it. Extracts hardcoded UI
   strings into `res/values/strings.xml` and switches call sites to
   `stringResource(...)`. This is the actual prerequisite work, not a
   formality — see "Confirmed starting state" above.
2. **`02_weblate_server_access.feature`** — documents what's concretely
   known about reaching and authenticating against the current POC server.
   Deliberately thin: most of the real workflow (sync direction, which
   second language validates the pipeline) is still open — see "Open
   questions" above.
