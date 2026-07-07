# Manual Test Plan: Manual Contact Import

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator, in the order below — this document exists because
permission dialogs and the native Contacts app can't be driven by an
in-process Compose UI test. Section titles match the `Scenario` names in
[`01_manual_contact_import.feature`](./01_manual_contact_import.feature)
for traceability, but the section order here is **run order**, not the
`.feature` file's scenario order — chosen so permission state and contact
state only ever move forward, never needing a reset or re-seed partway
through the pass. Use the `.feature` file as the source of truth for
expected behavior.

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`).
- Run [`seed_test_contacts.sh`](./seed_test_contacts.sh) against the
  connected device/emulator (requires `ANDROID_HOME` to be set) to create
  the native contacts these tests need, covering different data shapes:
  1. **"Alex (Test)"** — has a phone number and email.
  2. **"Sam (Test)"** — has only a phone number, no email.
  3. **"Jordan (Test)"** — has only an email, no phone number.

  The same run also seeds 50 generic "Test Contact N" contacts, used below
  as an always-available pool for tests that just need *some* unlinked
  contact (pass a different count, e.g. `./seed_test_contacts.sh 10`, or
  `0` to skip the bulk batch and seed only the three named ones). The
  script is idempotent — running it again is a no-op if seeded contacts
  already exist; run `./seed_test_contacts.sh --clear` first to reseed,
  which removes only what it created, never real contacts on the device.

  **Known gap:** none of the seeded contacts has a photo — photo data is a
  BLOB column and can't be set through the plain `adb shell content insert`
  CLI (verified: it only accepts scalar bind types, and `content write`
  doesn't support per-row file access on this provider either). If a test
  specifically needs photo data, add one to "Alex (Test)" manually via the
  Contacts app after seeding.
- Tend installed fresh, or with its app data cleared (Settings → Apps →
  Tend → Storage → Clear storage, equivalent to `adb shell pm clear
  com.jksalcedo.tend`), so you control the starting state of the whole
  pass. Verified on this Android version: clearing storage resets Tend's
  saved people **and** every runtime permission grant Tend holds —
  including a prior "permanently denied" (don't-ask-again) contacts state,
  which reverts to a normal, askable dialog again. Expect the
  notification-permission dialog to reappear on next launch too, since
  that's reset the same way. It does **not** touch native device contacts
  (Alex (Test)/Sam (Test)/Jordan (Test), or anything from
  `seed_test_contacts.sh`) — those live in the system Contacts Provider
  and survive across clears.
- To reset just the contacts permission without losing Tend's saved
  people: Settings → Apps → Tend → Permissions → Contacts.

## 1. Import feature is available with no contacts yet

**Precondition:** Tend has no people saved (fresh install).
**Steps:**
1. Open Tend.
2. Tap the overflow menu (⋮) on the Home screen.

**Expected:** An "Import contacts" menu item is visible and tappable.

- [ ] Pass / [ ] Fail

## 2. Import feature is available with existing contacts

**Precondition:** Tend has at least one person already (add one manually via
"New Connection" first).
**Steps:**
1. Open the overflow menu (⋮) on the Home screen.

**Expected:** "Import contacts" is still visible and tappable.

- [ ] Pass / [ ] Fail

## 3. Opening Import requests permission if not already granted

**Precondition:** Contacts permission never yet requested (fresh install,
or reset via the Permissions screen per Prerequisites).
**Steps:**
1. Tap "Import contacts."
2. Observe the system permission dialog appears automatically.
3. Tap **Deny**.

**Expected:** No contact picker appears; a short message explains contacts
access is needed, with a "Grant Access" button, no crash.

- [ ] Pass / [ ] Fail

**Do not grant here** — leave permission denied and continue directly to
the next test to reach the permanently-denied state without resetting
anything.

## 4. Opening Import when contacts permission was permanently denied

**Precondition:** Continuing directly from test 3 (one denial already
recorded).
**Steps:**
1. Tap "Grant Access."
2. The system permission dialog appears again. Tap **Deny** a second time.
3. Tap "Import contacts" once more.

**Expected:** After the second denial, no system permission dialog appears
at all anymore. A message explains that contacts access must be enabled
from system settings, with a button that opens the app's system settings
page. Tapping it navigates to Tend's app info screen.

- [ ] Pass / [ ] Fail

## 5. Opening Import with permission already granted goes straight to the picker

**Precondition:** Continuing directly from test 4 — from Tend's app info
screen (opened via "Open Settings"), grant the Contacts permission, then
return to Tend. Permission is now granted for the rest of this test pass —
no further permission changes needed below.
**Steps:**
1. Tap "Import contacts."

**Expected:** The picker appears immediately, no permission dialog.

- [ ] Pass / [ ] Fail

## 6. Importing selected contacts creates linked Tend people

**Steps:**
1. Open "Import contacts."
2. Check the boxes next to "Alex (Test)," "Sam (Test)," and "Jordan (Test)."
3. Tap "Import (3)" in the top bar.
4. Return to the Home screen.

**Expected:** Three new connections appear. "Alex (Test)" has both phone
and email populated; "Sam (Test)" has only a phone number; "Jordan (Test)"
has only an email — each matching what you set on the device. Default
check-in frequency and empty notes/events match what a manually-added
connection starts with.

- [ ] Pass / [ ] Fail

## 7. Picker excludes contacts already linked to a Tend person

**Precondition:** Continuing directly from test 6 — Alex/Sam/Jordan are
now all linked to Tend people.
**Steps:**
1. Open "Import contacts" again.

**Expected:** "Alex (Test)," "Sam (Test)," and "Jordan (Test)" no longer
appear in the list. Only the bulk "Test Contact N" contacts (still
unlinked) appear.

- [ ] Pass / [ ] Fail

## 8. Confirming the picker with no selections makes no changes

**Steps:**
1. Open "Import contacts" (the bulk "Test Contact N" pool is still
   available, no setup needed).
2. Without checking anything, tap "Import (0)."

**Expected:** Returns to Home with no new connections created.

- [ ] Pass / [ ] Fail

## 9. Canceling the picker makes no changes

**Steps:**
1. Open "Import contacts."
2. Check the box next to "Test Contact 1," then tap the back arrow (not
   the Import button).

**Expected:** Returns to Home with no new connections created — the
selection you made is discarded, and "Test Contact 1" remains unlinked and
available for later tests.

- [ ] Pass / [ ] Fail

## 10. A contact skipped in one import session can still be imported later

**Steps:**
1. Open "Import contacts."
2. Check the box next to "Test Contact 2" only, leaving "Test Contact 3"
   unselected. Tap "Import (1)."
3. Open "Import contacts" again.

**Expected:** After step 2, only "Test Contact 2" becomes a linked
connection. In step 3, "Test Contact 3" still appears in the picker,
selectable as normal — it wasn't remembered as "declined."

- [ ] Pass / [ ] Fail

## 11. Import feature is available regardless of the first-run prompt's outcome

**Note:** The first-run prompt (`04_first_run_import_prompt.feature`) isn't
implemented yet — skip this section until that feature lands. Once it does:
verify "Import contacts" is reachable whether the first-run prompt was
answered "Yes" or "No."

- [ ] N/A — first-run prompt not yet implemented
