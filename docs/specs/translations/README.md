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
| Scope of `01`'s string externalization                  | Every hardcoded string on `main` as it exists today (48 `Text(...)` call sites as of 2026-07-09) — not a per-screen subset. One complete pass over that surface, not an incremental rollout. **Explicit exception:** `feature/contact-sync` (PR #15) is still unmerged and adds ~24 more hardcoded strings; those are a deliberate, separate follow-up pass once that PR lands — `01` does not wait for it. Anything added after `01` lands is covered by the "no new hardcoded strings" scenario already in the feature file.                                                |
| App behavior if the Weblate server is down, moved, or relinked | Unaffected. The app never talks to Weblate at runtime — see "Resilience to Weblate outages/migration" below.                                                                                                                                                                                       |

## Resilience to Weblate outages/migration

The app has **no runtime dependency on Weblate at all**. Weblate only ever
interacts with this git repository (translators edit strings on the
server; translated values get synced into `res/values-xx/strings.xml`
files in the repo). The compiled app just ships whatever's in the repo at
build time — it never makes a network call to Weblate, checks it's
reachable, or otherwise knows it exists.

That means, concretely:

- **If the Weblate server goes down**, nothing about the installed app
  changes. Every translation already synced into the repo keeps working
  exactly as before. The only impact is that translators can't submit new
  work until the server's back.
- **If the server moves or needs to be relinked** (expected, since this is
  a POC — see "Confirmed starting state"), it's a repo-doc update (the URL
  in this README and `.env.example`) plus re-registering the project on
  the new instance. No app code, build script, or CI configuration changes
  — none of them reference the server, by design (see the Non-Goals entry
  on avoiding coupling).
- **For any string without a synced translation for a given locale** —
  whether because it's genuinely not translated yet, or because the server
  was unreachable when a sync would otherwise have happened — Android's
  own resource fallback shows the base English string for that key.
  Same mechanism as the "missing or incomplete translation" row above;
  a server outage is just one more reason a translation might be missing,
  not a special case needing its own handling.

**Is setup effort wasted if the server moves?** Mostly no. String
externalization (`01`) — the actual bulk of the work — has nothing to do
with Weblate specifically; it's the same prerequisite for any Android
translation tool, and carries over completely regardless of what happens
to this server. The Weblate-specific piece (`02`, the URL, the project/
component configuration on this one instance) is cheap to redo precisely
because nothing beyond documentation depends on it — no CI/automation was
built against this instance (a deliberate Non-Goal, for exactly this
reason).

**The one real risk** isn't engineering effort, it's translated *content*:
any translations completed on the current server but not yet exported/
synced into this repo exist only in that server's database. If the POC
server is decommissioned before that sync happens, that translation work
is lost, even though nothing about the repo or app breaks. Whoever manages
the migration should confirm all completed translations are synced into
the repo before the old server goes away.

## Operational checklists

These aren't specced as `.feature` scenarios — they're one-time or
occasional operational procedures for whoever manages the server, not
app behavior to build or test. Update these as real decisions land (the
sync mechanism, launch languages, etc. — see "Open questions").

### Takedown (decommissioning a server, POC or otherwise)

- [ ] Confirm every completed translation on the server has been
      exported/synced into `res/values-xx/strings.xml` in the repo — this
      is the one real risk (see above); nothing else here matters if this
      is missed.
- [ ] Check Weblate's own component/translation stats for any in-progress
      work that hasn't been synced yet, not just "completed" translations.
- [ ] Revoke and rotate any credentials issued for this server — personal
      logins, API tokens, and especially any deploy key/bot account if one
      was ever provisioned (should be none yet — see Non-Goals — but
      confirm).
- [ ] Remove or update the server URL wherever it's documented (this
      README, `.env.example`) so nothing points at a dead server.
- [ ] Confirm no CI workflow or webhook was ever pointed at this instance
      (should be none — see Non-Goals — but worth a final check before
      the server disappears and makes that harder to verify).
- [ ] Decide whether to archive the server's own database/export
      (translation memory, contributor history) beyond what's already
      captured in the repo, if that history has value worth keeping.

### Launch (promoting a server from POC/staging to actually driving real translations)

- [ ] `01` (string externalization) is merged — nothing is translatable
      before this exists.
- [ ] The base English `res/values/strings.xml` is complete and stable —
      a moving source text mid-translation wastes translator effort and
      forces re-sync.
- [ ] Target language(s) for this launch are decided (see "Open
      questions" — the validation language, plus whatever the actual
      launch set turns out to be).
- [ ] The sync mechanism from Weblate back into the repo is decided *and
      tested end-to-end* at least once (see "Open questions" — currently
      unknown whether this is automatic or manual).
- [ ] The process for inviting/managing translator access is defined (see
      "Open questions" — currently only the team lead's process is known).
- [ ] Live-verify on a real device set to the target locale: a translated
      string renders correctly, and a string with no translation yet
      falls back to English without a crash or blank UI (see "Resilience"
      above — confirms the fallback is actually relied on correctly, not
      just documented).
- [ ] A contribution guideline exists (e.g. in `CONTRIBUTING.md`) telling
      contributors new UI strings must go in `strings.xml`, not as
      literals — otherwise `01`'s externalization work erodes over time.

### Move (migrating from one Weblate host to another)

Effectively Takedown (old server) and Launch (new server) combined, in a
specific order — do not decommission the old server until the new one is
proven working end-to-end.

- [ ] Complete every item in "Takedown" above **for the old server**,
      except actually deleting/decommissioning it yet.
- [ ] Stand up the new server and re-create the project/component
      configuration on it — this is manual re-setup, since nothing here
      is automated against a specific instance by design.
- [ ] Issue new credentials to translators/contributors on the new
      instance.
- [ ] Update the documented URL in this README and `.env.example` to the
      new server — the single reference point this was designed around
      (see the design-decisions table).
- [ ] Verify the new server can pull the current repo's source strings,
      and decide whether translation memory/history from the old server
      is worth migrating or is acceptable to lose.
- [ ] Complete "Launch" above for the new server, including the live
      device-fallback check, before treating the new server as the real
      one.
- [ ] Only once the new server is confirmed working: revoke old
      credentials and decommission the old server (the remaining steps of
      "Takedown").

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
