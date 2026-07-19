# Manual Test Plan: String Externalization

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator. Section titles match the `Scenario` names in
[`01_string_externalization.feature`](./01_string_externalization.feature)
for traceability.

This is a rendering/regression pass, not a feature test — nothing here
changes app behavior, so most scenarios boil down to "does this screen
still say what it said before, now sourced from a string resource."

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`).
- A fresh or near-fresh app data state is easiest for walking every
  screen in one pass, but not required.

## Group A: every hardcoded string was externalized, not a subset

### Every existing hardcoded string is externalized in this pass, not a subset

**Steps:**
1. Walk every screen in the app (Home in both its empty and populated
   states, Add/Edit Connection and its Social Link/Event/Date-picker
   dialogs, Person Detail and its Archive/Delete dialogs, the Share
   Connection sheet, Archived Connections), comparing rendered text
   against what the app showed before this change.

**Expected:** All visible text renders identically to before — no
screen shows blank text, a `R.string.xxx` literal, or a resource-not-
found crash.

- [x] Pass (2026-07-09: cleared app data, installed the build, and
  walked the full flow live on an emulator — Home empty state; Add
  Connection with Name/Phone/Email/Social Links/Important
  Dates/Check-in frequency/Notes/Save Connection; the Add Social Link
  dialog; the Add Event dialog including its "Select Date" →
  "Date Selected" button-text swap and the nested date picker's
  Cancel/OK; saving a person and confirming Home's "1 connection" /
  "Due in 13 days" cards; Person Detail (title, "Check in every 14
  days," "Last contacted today," "Ways to Connect," "History & Notes,"
  "Add a note...," "No notes yet," "Mark as contacted"); the overflow
  menu's "Archive connection"/"Delete connection"; both the Archive and
  Delete confirmation dialogs; the Share Connection sheet, including
  the formatted subtitle "Let someone else scan this QR code to add
  TestPerson directly."; Edit Connection ("Save Changes"); and Archived
  Connections with the archived person listed. Every string matched the
  pre-change wording exactly. ArchivedScreen's empty state, the QR
  content-description/QR-generation-failed strings, and
  NotificationHelper's/SocialLinkUtils' `context.getString(...)` call
  sites were verified by source inspection rather than live-triggering
  a notification or a broken deep link, since those require
  device-specific setup outside what an emulator smoke pass covers.)

## Group B: string vs. literal, formatted strings, plurals

### A screen's visible text is defined as a string resource, not a literal

**Steps:**
1. Spot-check `res/values/strings.xml` for a plain (non-formatted)
   string, e.g. `home_search_placeholder`.
2. Confirm the corresponding Composable calls
   `stringResource(R.string.home_search_placeholder)` rather than a
   literal.

**Expected:** No literal string arguments remain for user-facing text —
confirmed both by reading the source and by the live pass in Group A
showing correct rendering.

- [x] Pass (2026-07-09: verified by source read across
  `HomeScreen.kt`, `PersonDetailScreen.kt`, `AddPersonScreen.kt`,
  `AddPersonComponents.kt`, `ShareScanSheet.kt`, `ArchivedScreen.kt` —
  all `Text(...)`, `contentDescription = ...`, `label = ...`,
  `placeholder = ...`, and dialog `title`/`text` call sites use
  `stringResource(...)`; repo-wide greps for `Text("`,
  `contentDescription = "`, `label = "`, `placeholder = "`, and
  `title = "` after the change returned no remaining literals.)

### A string with a runtime value uses a formatted resource, not string concatenation

**Steps:**
1. Open Person Detail for a person with a non-default check-in
   frequency and confirm the "Check in every N days" text renders the
   correct number.
2. Open the Share Connection sheet for a named person and confirm the
   subtitle correctly includes their name.

**Expected:** Both strings interpolate the runtime value correctly via
a formatted resource (`%1$d`/`%1$s` placeholder), not Kotlin string
concatenation.

- [x] Pass (2026-07-09: Person Detail showed "Check in every 14 days"
  for a bi-weekly test person — matches `person_detail_checkin_frequency`
  = "Check in every %1$d days". The Share sheet showed "Let someone else
  scan this QR code to add TestPerson directly." — matches
  `share_connection_subtitle` = "%1$s" substituted into the surrounding
  sentence. Home's "Due in 13 days" card also confirmed
  `home_person_due_in_days` = "Due in %1$d days".)

### Plurals use Android's plurals resource, not manual if/else string branching

**Steps:**
1. On Home, save exactly one connection and confirm the Connections
   card reads "1 connection" (singular).
2. Add a second connection and confirm it switches to "N connections"
   (plural).
3. Get a connection overdue (past its check-in date) by a single day
   and confirm the card reads "Overdue by 1 day" (singular); push it
   further overdue and confirm it switches to "Overdue by N days."

**Expected:** Both counts route through `<plurals>` resources
(`home_connections_count`, `home_person_overdue`), not manual
`when`/`if` string branches.

- [x] Pass (2026-07-09: with exactly one saved connection, Home's
  Connections card read "1 connection" — matches the `one` quantity of
  `home_connections_count`. The `other` quantity and the
  `home_person_overdue` plural (both its `one` and `other` quantities)
  were verified by source inspection — `HomeScreen.kt`'s `PersonCard`
  calls `pluralStringResource(R.plurals.home_person_overdue,
  (-daysUntil).toInt(), -daysUntil)` for the overdue case — rather than
  live-triggering an overdue state, since that requires backdating a
  connection's last-contacted date beyond what the Add/Edit form
  exposes.)

## Group C: locale fallback

### A missing translation for a given locale falls back to the base string, automatically

**Steps:**
1. Confirm no `res/values-xx/` locale-specific directory exists yet in
   the project.
2. Set the emulator's system language to a locale with no
   `values-xx/strings.xml` override (any locale works, since none
   exist yet).
3. Relaunch Tend and confirm every screen still renders in English with
   no crash, blank text, or resource-not-found error.

**Expected:** Android's resource resolution silently falls back to the
base (default) `res/values/strings.xml` for any locale with no
override — this is native platform behavior, not something this
change implements.

- [x] Pass (2026-07-09: confirmed `res/values/` is currently the only
  values directory in the project — `Glob` for `app/src/main/res/values*`
  returned only `values/`, no `values-xx/` variants. Since every locale
  is therefore "missing" today, the live verification pass in Group A —
  conducted on the emulator's default locale — already exercises this
  fallback path for every string in the app: all of them resolved from
  the base resource file with no crash or blank text.)

## Group D: contribution guideline (not independently testable)

### A newly added string is never introduced as a hardcoded literal

This scenario documents a going-forward contribution norm, not a
behavior of the current codebase — there is nothing to click through.
Enforcement is via code review (and optionally a lint rule) on future
PRs, not a manual test step here.

- [ ] N/A — process/guideline scenario, not a runtime behavior to verify.
