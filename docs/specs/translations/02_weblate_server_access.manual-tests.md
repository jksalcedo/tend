# Manual Test Plan: Weblate Server Access

Step-by-step procedures for a human tester (or auditor) to execute by
hand. Section titles match the `Scenario` names in
[`02_weblate_server_access.feature`](./02_weblate_server_access.feature)
for traceability.

Unlike `01`, this feature is almost entirely documentation and repo
hygiene, not app behavior — there's no screen to click through. Most
"testing" here is grepping the repo and checking git history, plus one
scenario that's a process description with nothing to click at all.

## Prerequisites

- A local clone of the repo with full git history available (`git log
  --all` needs to actually see history, not a shallow clone).
- No special device/emulator setup needed — this is a repo-hygiene and
  documentation check, not a UI test.

## Group A: credential hygiene (the two scenarios with something concrete to verify)

### The repository never contains a credential

**Steps:**
1. Confirm `.env` is gitignored and was never committed:
   `git check-ignore -v .env` (should report the `.gitignore` rule that
   matches it) and `git ls-files | grep '^\.env$'` (should return
   nothing).
2. Search the full commit history of `.env.example` for any non-empty
   secret value: `git log --all -p -- .env.example`, scanning every
   `WEBLATE_*` line ever added or removed.
3. Confirm the currently committed `.env.example` ships only variable
   names with empty/placeholder values, never a real token or password.

**Expected:** `.env` is untracked and ignored. Every `WEBLATE_*` line
that has ever existed in `.env.example`'s history — including
variables later removed, like the `WEBLATE_USERNAME`/`WEBLATE_PASSWORD`
pair that was added and then deliberately dropped in favor of the API
token alone — was always committed with an empty value, never a real
credential.

- [x] Pass (2026-07-09: `git check-ignore -v .env` reported
  `.gitignore:18:.env`; `git ls-files | grep '^\.env$'` returned
  nothing — `.env` has never been tracked. `git log --all -p --
  .env.example` shows every `WEBLATE_URL`/`WEBLATE_API_TOKEN`/
  historical `WEBLATE_USERNAME`/`WEBLATE_PASSWORD` line ever committed
  had an empty value; the current file only defines `WEBLATE_URL`
  (the public server address, not a secret) and an empty
  `WEBLATE_API_TOKEN=`.)

### The server's address is treated as a provisional, single-source value

**Steps:**
1. Search the full repo (tracked files) for the server hostname:
   `grep -rn "weblate.tend.farband.ca" .` (excluding `.git/`).
2. Confirm every match is in a documentation or local-config file, and
   specifically check `.github/` and `app/` for zero matches.

**Expected:** The URL appears only in
`docs/specs/translations/README.md`,
`docs/specs/translations/02_weblate_server_access.feature`, and
`.env.example` (plus a contributor's own untracked local `.env`). No
CI workflow (`.github/`) and no application code (`app/`) references
it — migrating hosts later is a doc/`.env.example` edit only.

- [x] Pass (2026-07-09: grep across the full repo returned exactly
  those three tracked files plus the local untracked `.env`; a
  separate targeted grep of `.github/` and `app/` for "weblate"
  case-insensitively returned zero matches in both.)

## Group B: process scenarios (nothing to click, documented instead)

### A team member is granted their own credentials

This scenario describes an operational process (the team lead issuing
individual Weblate logins) that happens entirely outside this repo —
there is no repo state or app behavior to click through or verify
mechanically.

**Verification performed:** Confirmed the README's "Confirmed starting
state" and "Credentials" sections state the policy explicitly (access
is per-user, no one — including maintainers — needs to know another
user's password) and that this matches how the current POC server
access was actually granted during this session (individual credential,
stored only in the requester's own local `.env`, never shared with or
known by this assistant).

- [x] Pass — process correctly documented; nothing further to test.

### Access granted to automation is minimally scoped

**Steps:**
1. Confirm no CI workflow, webhook, or bot account currently exists for
   this Weblate instance (already covered by Group A's `.github/`
   grep, and by the README's Non-Goals section).

**Expected:** Since no automation integration exists yet, there is
nothing to check for over-broad scoping today. The scenario functions
as forward guidance for whenever such automation is built, not a
current-state check.

- [x] Pass (2026-07-09: confirmed via Group A's grep — no `.github/`
  workflow or webhook references Weblate at all, matching the README's
  Non-Goals entry that this is deliberately not yet built. Revisit this
  scenario for real when that automation is proposed.)
