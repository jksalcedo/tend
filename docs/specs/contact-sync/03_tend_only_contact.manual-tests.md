# Manual Test Plan: Tend-only Contact (Case 2)

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator, in the order below — this document exists because
permission dialogs and the native Contacts app can't be driven by an
in-process Compose UI test. Section titles match the `Scenario` names in
[`03_tend_only_contact.feature`](./03_tend_only_contact.feature) for
traceability, but the section order here is **run order**, not the
`.feature` file's scenario order — chosen so `WRITE_CONTACTS` permission
state only ever moves forward (never granted → denied → permanently
denied → granted), matching the same pattern used in
[`01_manual_contact_import.manual-tests.md`](./01_manual_contact_import.manual-tests.md).
Use the `.feature` file as the source of truth for expected behavior.

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`).
- Tend installed fresh, or with its app data cleared (Settings → Apps →
  Tend → Storage → Clear storage), so you control the starting state —
  see `01`'s manual test plan for exactly what clearing does and doesn't
  reset. No native contacts need to be seeded for this feature — every
  scenario here starts from a person created directly in Tend, never an
  imported one.
- `READ_CONTACTS` should already be granted before running section 9
  (see that section's note) — grant it via Settings → Apps → Tend →
  Permissions → Contacts if it isn't already, independent of the
  `WRITE_CONTACTS` flow this document walks through.

## 1. Contact created via Add Person always starts unlinked

**Steps:**
1. From the Home screen, tap "New Connection."
2. Fill in a name (e.g. "Marco") and save.

**Expected:** The new connection is created with no errors. (Verified
further in the next two sections — this one just establishes the person
used throughout the rest of this document.)

- [x] Pass (automated 2026-07-07 via adb/uiautomator: created "Marco" with
  phone 5550200 and email marco@example.com, appeared correctly on Home)

## 2. Unlinked contact shows a not-synced indicator

**Steps:**
1. Open Marco's person detail screen.

**Expected:** A status indicator states the contact is "Not synced to your
device contacts."

- [x] Pass (automated 2026-07-07: indicator text confirmed verbatim)

## 3. All fields are editable for an unlinked contact

**Steps:**
1. From Marco's detail screen, tap the edit (pencil) icon.

**Expected:** Name, phone number, and email fields are all editable text
fields — no lock icon, no disabled styling, no "Edit in Contacts" link
(that only appears for device-linked people).

- [x] Pass (automated 2026-07-07: confirmed via screenshot — normal
  editable OutlinedTextFields, no lock/disabled styling, no Edit in
  Contacts link)

## 4. Unlinked contacts never write into device contacts unless explicitly synced

**Steps:**
1. Add a note to Marco and set a check-in frequency, if not already set.
2. Background Tend (Home button) and bring it back to the foreground, to
   trigger a foreground refresh cycle.
3. Open the device's native Contacts app.

**Expected:** No contact named "Marco" (or matching Marco's data) appears
in the native Contacts app — the foreground refresh only ever touches
already-linked people, and Marco isn't one.

- [x] Pass (automated 2026-07-07: added a note, force-backgrounded and
  foregrounded the app via `am` to trigger a refresh cycle, then queried
  `content://com.android.contacts/contacts` directly — no "Marco" row
  existed. Not-synced indicator and Sync to Device button unchanged
  afterward.)

## 5. Sync to Device is offered for an unlinked contact

**Steps:**
1. Return to Marco's detail screen.

**Expected:** A "Sync to Device" text button is visible below the
not-synced indicator.

- [x] Pass (automated 2026-07-07: button present and correctly labeled)

## 6. Syncing to device without contacts permission

**Precondition:** `WRITE_CONTACTS` never yet requested (fresh install, or
reset via the Permissions screen).
**Steps:**
1. Tap "Sync to Device."
2. The system permission dialog appears. Tap **Deny**.

**Expected:** No native contact is created. A short message appears
explaining contacts access is needed to sync. Marco remains unlinked and
fully editable (the not-synced indicator and edit fields are unchanged
from sections 2–3).

- [x] Pass (automated 2026-07-07: denied via the system dialog's
  `permission_deny_button`; "Contacts access is needed to sync this
  connection to your device." message appeared; not-synced indicator
  unchanged)

**Do not grant here** — leave permission denied and continue directly to
the next test to reach the permanently-denied state without resetting
anything.

## 7. Syncing to device when contacts permission was permanently denied

**Precondition:** Continuing directly from section 6 (one denial already
recorded).
**Steps:**
1. Tap "Sync to Device" again.
2. The system permission dialog appears a second time. Tap **Deny** again.
3. Tap "Sync to Device" a third time.

**Expected:** After the second denial, no system permission dialog appears
at all on the third attempt. A message explains that contacts access must
be enabled from system settings, with an "Open Settings" button. Tapping
it navigates to Tend's app info screen. Marco remains unlinked and fully
editable throughout.

- [x] Pass (automated 2026-07-07: second denial showed Android's own
  "Deny & don't ask again" variant automatically; third tap produced no
  dialog at all, correct permanently-denied message shown, "Open
  Settings" landed on Tend's App info screen)

## 8. Syncing to device creates a brand-new native contact

**Precondition:** Continuing directly from section 7 — from Tend's app
info screen (opened via "Open Settings"), grant the Contacts permission,
then return to Tend.
**Steps:**
1. On Marco's detail screen, tap "Sync to Device."

**Expected:** No permission dialog appears (already granted). A new native
contact is created — check the device's Contacts app for an entry matching
Marco's name, phone number, and email (photo too, if one was set — see the
README's note that Case 2 people have no in-app photo picker yet, so this
will typically be empty). No existing native contact is searched for or
reused; this is always a brand-new entry.

- [x] Pass (automated 2026-07-07: granted Contacts permission via
  Settings → App permissions → Contacts → Allow, which granted both
  `READ_CONTACTS` and `WRITE_CONTACTS` together — confirmed via `dumpsys
  package`. Tapped "Sync to Device": no dialog, instant success. Verified
  via `content query` on `content://com.android.contacts/data` that a
  single new raw contact (id 197) was created with exactly one name, one
  phone (5550200), and one email (marco@example.com) row — no duplicate
  or matched-existing contact. No photo, as expected — Case 2 has no
  photo picker.)

## 9. After syncing to device, the contact behaves as device-linked

**Precondition:** Continuing directly from section 8.
**Steps:**
1. Return to Marco's detail screen (navigate back and re-open it, or wait
   for the screen to recompose).

**Expected:** The not-synced indicator is replaced by "Managed by your
device contacts," with an "Edit in Contacts" link. Opening the edit screen
shows name/phone/email as locked (read-only), matching a normal
device-linked (Case 1) contact. **Note:** this specifically requires
`READ_CONTACTS` to also be granted (see Prerequisites) — `WRITE_CONTACTS`
alone lets the sync succeed, but Tend needs `READ_CONTACTS` too to verify
and display the synced state as "Managed," not "Sync paused."

- [x] Pass (automated 2026-07-07: "Managed by your device contacts" with
  "Edit in Contacts" shown immediately after sync, no extra foreground
  cycle needed since `SyncToDeviceUseCase` sets `isDeviceLinkBroken =
  false` directly. Edit screen showed "Name, phone, and email are managed
  by your device contacts — edit them in the Contacts app," fields
  visibly disabled, matching Case 1 exactly.)
