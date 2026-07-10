# Manual Test Plan: First-run Contact Import Prompt

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator — this document exists because permission dialogs,
process restarts, and the native Contacts app can't be driven by an
in-process Compose UI test. Section titles match the `Scenario` names in
[`04_first_run_import_prompt.feature`](./04_first_run_import_prompt.feature)
for traceability.

Unlike `01`-`03`, most sections here **cannot** be chained directly one
after another: the prompt is a use-once gate (`ResolveContactImportPromptUseCase`
marks it permanently resolved the moment the user answers it, whichever way),
so re-triggering it for the next scenario requires a fresh app-data clear.
Sections are grouped by shared precondition to minimize the number of
clears needed, and each group starts a fresh pass.

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`).
- Tend's app data cleared before **each lettered group** below (Settings →
  Apps → Tend → Storage → Clear storage, equivalent to `adb shell pm clear
  com.jksalcedo.tend`) — see `01`'s manual test plan for exactly what
  clearing does and doesn't reset, including that it also resets contacts
  permission grants on this Android version.
- Run [`seed_test_contacts.sh`](./seed_test_contacts.sh) at least once
  beforehand so native contacts exist to select from when a group reaches
  the picker (idempotent — safe to run once and reuse across all groups).

## Group A: accept, grant permission, import a selection

**Precondition:** Fresh app data clear. Contacts permission not yet granted.

### 1. Prompt is shown on first launch with no contacts

**Steps:**
1. Open Tend for the first time (dismiss the unrelated notification-permission
   dialog first, if shown).

**Expected:** A dialog appears asking whether to import contacts from the
device, with "Yes" and "No" actions.

- [x] Pass (automated 2026-07-08: "Import your contacts?" dialog shown
  correctly on first launch with 0 connections)

### 2. User accepts and grants permission, then imports a selection of contacts

**Precondition:** Continuing directly from section 1.
**Steps:**
1. Tap "Yes."
2. The system contacts permission dialog appears — tap **Allow**.
3. Select one or more contacts in the picker that appears.
4. Tap "Import (N)."

**Expected:** The dialog is replaced immediately by the existing Import
Contacts picker (feature `01`'s screen, reused as-is) once permission is
granted. After confirming, the selected contacts appear as new,
device-linked connections on Home.

- [x] Pass (automated 2026-07-08: tapping Yes navigated straight to the
  Import Contacts screen with the system permission dialog auto-triggered
  — confirms the prompt correctly reuses feature `01`'s screen with no
  duplicated permission logic. Granted, selected "Test Contact 3",
  imported — appeared correctly as a new connection.)

### 8. Prompt is never shown again once resolved, regardless of later contact count

**Precondition:** Continuing directly from section 2.
**Steps:**
1. Background Tend and bring it back to the foreground (or force-stop and
   relaunch).

**Expected:** No import prompt appears again. The imported connections
from section 2 are still present.

- [x] Pass (automated 2026-07-08: backgrounded via HOME key, relaunched —
  no prompt, Test Contact 3 still present)

## Group B: accept, grant permission, confirm zero selections

**Precondition:** Fresh app data clear.

### 3. User accepts, grants permission, but confirms zero selections

**Steps:**
1. Open Tend, tap "Yes" on the import prompt.
2. Grant the contacts permission if asked.
3. Without checking any contacts, tap "Import (0)."

**Expected:** No connections are created. Home shows its normal empty
state ("No connections yet"). The prompt does not reappear if you
background/foreground the app afterward.

- [x] Pass (automated 2026-07-08: confirmed "Import (0)" before
  confirming, resulting empty state shown, no connections created;
  relaunched afterward and the prompt correctly did not reappear even
  with 0 people — confirms resolution is independent of people count)

## Group C: accept, then deny permission

**Precondition:** Fresh app data clear.

### 4. User accepts but denies the contacts permission

**Steps:**
1. Open Tend, tap "Yes" on the import prompt.
2. The system contacts permission dialog appears — tap **Deny**.

**Expected:** No contact picker is shown; the Import Contacts screen's
existing "permission denied" message appears instead (feature `01`'s
screen, reused as-is — this dialog has no permission-handling logic of
its own). Returning to Home shows the normal empty state. The prompt does
not reappear.

- [x] Pass (automated 2026-07-08: tapped Yes → denied the system dialog →
  "Contacts access is needed to show contacts you can import. You can try
  again below." with a "Grant Access" button shown, no picker — the exact
  existing feature `01` screen. Returned to Home: empty state, no
  connections created, prompt did not reappear.)

**Not independently live-tested: "accept but already permanently denied."**
The prompt's "Yes" handler unconditionally marks itself resolved and
navigates to the existing Import Contacts screen — it has no
permission-specific branching of its own, so whichever permission state
that screen finds (first denial vs. permanently denied) is handled
entirely by code `01` already covers and has passing manual tests for.
Re-verifying it here would just be re-running `01`'s section 4 through a
different entry point. Also, genuinely testing this combination live
would require the permission to already be in a "permanently denied"
state *before* the prompt is ever shown for the first time — but clearing
app data (needed to get the prompt into an unresolved state) also resets
permission grants on this Android version, so the two preconditions can't
be established independently on this test setup.

## Group D: decline (explicit "No") and the Import feature stays reachable

**Precondition:** Fresh app data clear.

### 6. User declines the prompt

**Steps:**
1. Open Tend, tap "No" on the import prompt.

**Expected:** No contact picker is shown, no connections are created,
Home shows its normal empty state. The prompt does not reappear on
relaunch.

- [x] Pass (automated 2026-07-08: tapped No, no picker shown, empty
  state, no connections created)

### 10. Declining the prompt does not block the Import feature

**Precondition:** Continuing directly from section 6.
**Steps:**
1. Open the overflow menu (⋮) → "Import contacts."

**Expected:** The Import Contacts picker opens normally, exactly as it
would if the prompt had never existed.

- [x] Pass (automated 2026-07-08: opened normally via the overflow menu,
  triggering the standard contacts permission dialog as expected)

## Group E: dismiss without choosing Yes or No

**Precondition:** Fresh app data clear.

### 7. User dismisses the prompt without choosing Yes or No

**Steps:**
1. Open Tend. With the import prompt showing, press the system **back**
   button (or tap outside the dialog).

**Expected:** The dialog closes. No contact picker is shown, no
connections are created, Home shows its normal empty state — identical
outcome to tapping "No." The prompt does not reappear on relaunch.

- [x] Pass (automated 2026-07-08: pressed system back button while the
  prompt was showing — dialog closed, no picker, empty state, no
  connections created; relaunched afterward and the prompt correctly did
  not reappear, identical to the explicit "No" outcome in section 6)

## Not covered by a manual test in this document

- **"Prompt is not shown if Tend already has at least one person."**
  Testing this live would require a person to exist in Tend's database
  *before* the app's very first launch-time check ever runs — but the
  only ways to create a person go through Home screen first, which would
  itself trigger (and resolve) the prompt before a person could be added.
  Reaching this precondition live would need writing directly into Tend's
  Room database file via adb, bypassing the UI entirely, which is
  disproportionate effort for behavior already covered by
  `MaybeShowContactImportPromptUseCaseTest`'s "does not show and silently
  resolves when people already exist" unit test. Marked not-automated
  rather than faked.
