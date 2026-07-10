# Manual Test Plan: Select All / None

Step-by-step procedures for a human tester to execute by hand, in the order
below. Section titles match the `Scenario` names in
[`07_select_all_none.feature`](./07_select_all_none.feature) for
traceability; section order here is **run order**, chosen so selection state
only ever moves forward through this pass.

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`),
  with the contacts permission already granted (see
  `01_manual_contact_import.manual-tests.md` if it isn't).
- Run [`seed_test_contacts.sh`](./seed_test_contacts.sh) with a batch of at
  least 5 generic contacts (e.g. `./seed_test_contacts.sh 5`) so the list has
  enough entries to distinguish "some selected" from "all selected."
- Tend with no people saved yet (fresh install or cleared storage), so none
  of the seeded contacts start out already-linked.

## 1. Select-all/none buttons are hidden while contacts are loading

**Steps:**
1. Tap "Import contacts."

**Expected:** Momentarily, while the list is loading (spinner visible),
neither "Select All" nor "Select None" is shown anywhere on screen.

- [x] Pass / [ ] Fail

## 2. Select-all/none buttons are hidden when there are no importable contacts

**Precondition:** Every native contact is already linked to a Tend person
(import everything first, or use a fresh emulator with zero device
contacts).
**Steps:**
1. Open "Import contacts."

**Expected:** The "No more contacts to import" message is shown; neither
button appears alongside it.

- [ ] Pass / [ ] Fail

## 3. Select-all/none buttons appear once contacts are loaded

**Precondition:** At least one importable (unlinked) contact exists.
**Steps:**
1. Open "Import contacts" and wait for loading to finish.

**Expected:** Both "Select All" and "Select None" are visible above the
contact list.

- [x] Pass / [ ] Fail

## 4. "Select All" is enabled and "Select None" is disabled when nothing is selected

**Steps:**
1. With the picker open and nothing checked, observe both buttons.

**Expected:** "Select All" appears enabled (full color). "Select None"
appears disabled (greyed out).

- [x] Pass / [ ] Fail

## 5. Both buttons are enabled when only some contacts are selected

**Steps:**
1. Check the box next to exactly one contact (not all).

**Expected:** Both "Select All" and "Select None" appear enabled.

- [x] Pass / [ ] Fail

## 6. Tapping "Select All" selects every currently visible contact

**Steps:**
1. With one contact still checked from test 5, tap "Select All."

**Expected:** Every contact row in the list becomes checked. The top bar's
"Import (N)" count matches the total number of contacts in the list.

- [x] Pass / [ ] Fail

## 7. "Select All" becomes disabled and "Select None" becomes enabled once everything is selected

**Steps:**
1. Continuing directly from test 6, observe both buttons.

**Expected:** "Select All" is now disabled (greyed out). "Select None" is
enabled.

- [x] Pass / [ ] Fail

## 8. Tapping "Select None" clears every selection

**Steps:**
1. Tap "Select None."

**Expected:** Every checkbox in the list becomes unchecked. "Import (0)" is
shown in the top bar. "Select All" becomes enabled again and "Select None"
becomes disabled again.

- [x] Pass / [ ] Fail

## 9. Deselecting a single contact after Select All re-enables "Select All"

**Steps:**
1. Tap "Select All" again to re-select everything.
2. Uncheck exactly one contact's own checkbox (not either button).

**Expected:** "Select All" immediately becomes enabled again (no longer
greyed out). "Select None" remains enabled. Every other contact remains
checked — only the one you tapped was affected.

- [x] Pass / [ ] Fail

## 10. Select all only affects contacts currently in the list

**Steps:**
1. Uncheck everything, then check and import exactly one contact (tap
   "Import (1)").
2. Open "Import contacts" again.
3. Tap "Select All."
4. Tap "Import (N)" to confirm.
5. Return to Home and check the connection list.

**Expected:** The contact imported in step 1 does not reappear in the
picker in step 2 (already linked, per existing behavior), and is unaffected
by the Select All in step 3 — it was never in the list to begin with. Only
the contacts visible at step 3 become new connections.

- [ ] Pass / [ ] Fail
