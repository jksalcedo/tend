# Manual Test Plan: Device-Linked Contact (Case 1)

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator. Section titles match the `Scenario` names in
[`02_device_linked_contact.feature`](./02_device_linked_contact.feature) —
use that file as the source of truth for expected behavior; this document
exists because backgrounding/foregrounding the app, editing in the native
Contacts app, and revoking permissions can't be driven by an in-process
Compose UI test. As with `01`'s plan, section order here is **run order**,
not necessarily the `.feature` file's scenario order, chosen to avoid
resets partway through.

The Gherkin file uses a placeholder person "Priya" (and a second person
"Priya Sharma" for the duplicate scenarios) — these steps use the seeded
contacts from `01`'s plan instead: **"Alex (Test)"** stands in for Priya,
and a couple of the bulk **"Test Contact N"** entries stand in for
Priya Sharma and other supporting contacts.

## Prerequisites

- Complete `01_manual_contact_import.manual-tests.md` first (or at least
  its Prerequisites) — this plan assumes `seed_test_contacts.sh` has been
  run and the Manual Contact Import feature works.
- Import **"Alex (Test)"** into Tend via the Import Contacts feature
  before starting section 1 below. It will be our primary Case 1 subject
  throughout most of this plan.
- A couple of the bulk "Test Contact N" contacts should remain unimported
  at the start — sections further down import them as needed.
- To edit a native contact directly (for the "changed outside of Tend"
  scenarios), use the device's own Contacts app.
- To force Android's own contact aggregation to merge two native contacts
  (needed for the merge/duplicate scenarios near the end): look for
  **Link**/**Merge** in the Contacts app — location varies by device/OS
  version (contact overflow menu, edit-screen overflow menu, or an
  "Organize" → duplicates tab). **Confirmed 2026-07-07: not present in any
  of those three locations on this Pixel emulator's Contacts app build**,
  and the underlying `ContactsContract.AggregationExceptions` API isn't
  writable from an adb shell session either (requires an app-level special
  access grant). Sections 16-18 need a device/OS version where this is
  actually exposed, or an instrumented test with that permission — this is
  real device/OS behavior outside Tend's control, not something to work
  around in the app.
- To delete a native contact entirely (needed for the broken-link
  scenarios): open it in the Contacts app and delete it there, not in Tend.

## 1. Linked contact shows a device-managed indicator

**Steps:**
1. Open "Alex (Test)"'s person detail screen in Tend.

**Expected:** An indicator reads something like "Managed by your device
contacts."

- [x] Pass (automated 2026-07-07 via adb/uiautomator) / [ ] Fail

## 2. Identity fields are read-only for a linked contact

**Steps:**
1. From Alex (Test)'s detail screen, tap the edit (pencil) icon.

**Expected:** A caption explains name/phone/email are managed by device
contacts; the Name, Phone Number, and Email fields are visible but
disabled (can't be typed into).

- [x] Pass (automated 2026-07-07, confirmed via `enabled="false"` on all
  three fields in the UI hierarchy dump) / [ ] Fail

## 3. Relationship fields remain fully editable for a linked contact

**Steps:**
1. Still on the edit screen, try changing the check-in frequency, adding a
   social link, or adding an important date.
2. Go back to the detail screen and add a note.

**Expected:** All of these work normally — only identity fields are locked.

- [x] Pass (automated 2026-07-07, confirmed `enabled="true"` on
  frequency/notes fields and the Add buttons) / [ ] Fail

## 4. Read-only identity fields offer a way to edit them in the native Contacts app

**Steps:**
1. On Alex (Test)'s detail screen, find the "Edit in Contacts" button
   near the sync indicator.
2. Tap it.

**Expected:** The device's native Contacts app opens directly to Alex
(Test)'s edit screen (not just its view screen).

- [x] Pass (automated 2026-07-07, confirmed `package="com.google.android.contacts"`
  with `text="Edit contact"` after tapping) / [ ] Fail

## 5. Editing in the native Contacts app is picked up on return / Foreground refresh picks up a name change

**Steps:**
1. From the native Contacts app (still open from the previous step),
   change Alex (Test)'s phone number and also change the name to
   "Alex Rivera (Test)." Save.
2. Switch back to Tend (e.g. via the app switcher, not by relaunching).
3. View Alex's person detail screen.

**Expected:** Both the new name and new phone number appear, refreshed
automatically — no manual refresh action needed.

- [x] Pass (automated 2026-07-07, name → "Alexandra (Test)", phone →
  "555-1234", both reflected without leaving Tend's back stack) / [ ] Fail

## 6. Foreground refresh does not touch Tend-owned fields

**Precondition:** Alex (Test) has at least one note and a non-default
check-in frequency (add some now if not already present).
**Steps:**
1. Repeat the edit-in-native-Contacts-app + switch-back cycle from the
   previous section (e.g. change the email this time).

**Expected:** The email updates, but the note and check-in frequency you
set are unchanged.

- [x] Pass (automated 2026-07-07, frequency stayed "14" across two
  separate native-edit cycles) / [ ] Fail

## 7. Foreground refresh with contacts permission no longer granted

**Steps:**
1. Settings → Apps → Tend → Permissions → Contacts → revoke access.
2. Switch back to Tend and view Alex (Test)'s detail screen.

**Expected:** The indicator changes to something like "Sync paused —
contacts permission needed," with a "Grant permission" action. Previously
cached name, phone, and email are still displayed (not blank).

- [x] Pass (automated 2026-07-07 via `adb shell pm revoke`) / [ ] Fail

## 8. Edit in Contacts remains available while sync is paused

**Steps:**
1. While still in the sync-paused state from the previous section, look
   for "Edit in Contacts" on Alex (Test)'s detail screen.

**Expected:** It's still shown and still works (opens the native edit
screen) even though sync is paused.

- [x] Pass (automated 2026-07-07) / [ ] Fail

## 9. Contacts permission is re-granted after being revoked

**Steps:**
1. Tap "Grant permission" on the sync-paused indicator (or re-enable it
   via Settings → Apps → Tend → Permissions → Contacts).
2. Switch away from Tend and back (to trigger a foreground refresh).
3. View Alex (Test)'s detail screen again.

**Expected:** The indicator returns to the normal "Managed by your device
contacts" state.

- [x] Pass (automated 2026-07-07 via `adb shell pm grant` + resume) / [ ] Fail

## 10. No local photo was ever cached before permission was revoked

**Precondition:** Fresh app state (or at least: import the contact used
here without ever backgrounding the app in between). Add a photo to a
not-yet-imported contact (e.g. "Test Contact 1") via the device Contacts
app first.
**Steps:**
1. Import "Test Contact 1" via Tend's Import Contacts feature.
2. Without switching away from Tend at any point (no Home-button press,
   no app-switch), navigate straight to Test Contact 1's person detail
   screen.

**Expected:** No foreground refresh has had a chance to run yet (Compose
navigation within the app doesn't trigger one, only actually leaving and
returning to the app does), so no photo is cached yet — a placeholder
initials avatar is shown, not a broken image.

- [x] Pass (automated 2026-07-07, using "Test Contact 4" instead of "Test
  Contact 1" — the user added its photo by hand via the system picker
  since that part can't be scripted, while it was still unimported.
  Automated the rest: launched Tend, imported Test Contact 4, and
  navigated straight to its detail screen with no backgrounding in
  between — confirmed a placeholder "T" initials avatar, not the photo
  and not a broken image, matching the expected "never refreshed yet"
  state.) / [ ] Fail

## 11. Photo is cached locally so it survives a permission-paused state

**Steps:**
1. Continuing from the previous section, switch away from Tend and back
   once (triggers the first real foreground refresh).
2. View Test Contact 1's detail screen — its photo should now display.
3. Revoke the contacts permission (Settings → Apps → Tend → Permissions →
   Contacts).
4. Switch back to Tend and view Test Contact 1's detail screen again.

**Expected:** The photo still displays normally even though permission is
now revoked — it's a local copy, not a live reference to the device
photo. Re-grant permission afterward before continuing.

- [x] Pass (automated 2026-07-07, using "Test Contact 3" instead of "Test
  Contact 1" — a photo was added to its already-linked native contact by
  hand, since the picker itself can't be scripted; everything downstream
  was automated: resumed Tend, confirmed the photo rendered on the detail
  screen, revoked `READ_CONTACTS` via `adb shell pm revoke`, resumed
  again, and confirmed the photo still displayed correctly alongside the
  "Sync paused — contacts permission needed" indicator. Re-granted
  permission afterward.) / [ ] Fail

## 12. Foreground sync continues for an archived device-linked contact

**Steps:**
1. Archive Alex (Test) (overflow menu → Archive connection).
2. Edit Alex's native contact in the device Contacts app (change the
   phone number again).
3. Switch away from Tend and back.
4. Open the Archived connections list and view Alex's detail screen.

**Expected:** The phone number is refreshed even though Alex is archived,
and "Edit in Contacts" is still available on the archived detail screen.
Unarchive Alex afterward before continuing.

- [x] Pass (automated 2026-07-07, phone → "555-7777" refreshed while
  archived) / [ ] Fail

## 13. Linked native contact is deleted from the device / Edit in Contacts is not offered once broken

**Precondition:** Import two more bulk contacts, e.g. "Test Contact 2" and
"Test Contact 3," so you have two independent broken-link subjects for
this section and the next.
**Steps:**
1. In the device Contacts app, delete "Test Contact 2" and "Test Contact
   3" entirely.
2. Switch back to Tend and view Test Contact 2's detail screen.

**Expected:** A warning appears (e.g. "This device contact was removed or
is no longer found"). Identity fields stay read-only and unchanged.
"Edit in Contacts" is no longer shown — only Unlink and Delete remain as
options (via the overflow menu).

- [x] Pass (automated 2026-07-07, deleted native contacts via `content
  delete`; confirmed warning text, absent Edit-in-Contacts, and the
  overflow menu's Unlink/Delete options) / [ ] Fail

## 14. Resolving a broken link by unlinking

**Steps:**
1. On Test Contact 2's detail screen (broken, from the previous section),
   open the overflow menu and choose "Unlink from device contact."

**Expected:** Test Contact 2 keeps its last-known name/phone/email, those
fields become editable again, the indicator changes to "Not synced to
your device contacts," and a "Sync to Device" mention may or may not
appear depending on whether that feature (`03_tend_only_contact.feature`)
has been implemented yet — if not yet built, its absence here is expected
and not a bug.

- [x] Pass (automated 2026-07-07, confirmed name/phone kept, all three
  identity fields `enabled="true"` afterward, indicator changed) / [ ] Fail

## 15. Resolving a broken link by deleting the person

**Steps:**
1. On Test Contact 3's detail screen (also broken, from section 13), open
   the overflow menu and choose "Delete connection." Confirm.

**Expected:** The Tend person is gone from the connections list. (This
was already broken/deleted on the device side, so there's nothing further
to check there.)

- [x] Pass (automated 2026-07-07) / [ ] Fail

## 16. Native contact is merged or reorganized by the device's own aggregation, not deleted

**Precondition:** Import a fresh bulk contact, e.g. "Test Contact 4."
**Steps:**
1. In the device Contacts app, create a plain new native contact (not
   imported into Tend) with a similar name, e.g. "Test Contact 4 Duplicate."
2. Use the Contacts app's Link/Merge feature to merge "Test Contact 4"
   with "Test Contact 4 Duplicate."
3. Switch back to Tend and view Test Contact 4's detail screen.

**Expected:** No broken-link warning appears — the contact is still shown
as normally synced. Its cached fields refresh from the merged result if
anything changed.

- [ ] Pass / [ ] Fail
- **Not automated (2026-07-07):** this build of the device/emulator's
  Contacts app doesn't expose a manual "Link"/"Merge" action in the
  contact overflow menu, the edit-screen overflow menu, or the Organize
  tab — checked all three. The underlying mechanism (`ContactsContract
  .AggregationExceptions`, `content://com.android.contacts/aggregation_
  exceptions`) is scriptable in principle but returned
  `UnsupportedOperationException` when inserted via `adb shell content
  insert` as the shell user — it requires an app-level special access
  grant, not available from the shell. Needs a human pass on a device
  where Contacts exposes duplicate-merging, or an instrumented test
  running with the right contacts-management permission.

## 17. Two Tend people become linked to the same native contact

**Precondition:** Import two more distinct bulk contacts, e.g. "Test
Contact 5" and (seed a few more with `./seed_test_contacts.sh 60` if you
need extras) another fresh one — call it "Test Contact 51."
**Steps:**
1. In the device Contacts app, use Link/Merge to merge the native
   contacts behind "Test Contact 5" and "Test Contact 51" into one.
2. Switch back to Tend and view both of their detail screens.

**Expected:** Both show a duplicate warning (e.g. "Possibly a duplicate of
[other name] — tap to review"), and tapping it navigates to the other
person's detail screen. Neither Tend record is auto-merged or deleted.

- [ ] Pass / [ ] Fail
- **Not automated (2026-07-07):** same limitation as section 16 — no
  scriptable way found to trigger a real native merge in this session.

## 18. Duplicate flag clears once the user resolves it manually

**Steps:**
1. On one of the two duplicate people from the previous section (e.g.
   Test Contact 51), unlink or delete it.
2. Switch away from Tend and back.
3. View the remaining person's (Test Contact 5) detail screen.

**Expected:** The duplicate warning is gone — it resolved itself once the
underlying collision went away.

- [ ] Pass / [ ] Fail
- **Not automated (2026-07-07):** depends on section 17's merge setup —
  same limitation.

## 19. Deleting a normally-linked (not broken) device-linked contact never touches the native contact

**Steps:**
1. Pick a healthy, normally-synced Tend person (e.g. Alex (Test), if still
   in good shape from earlier sections).
2. Delete it from Tend (overflow menu → Delete connection).
3. Open the device's native Contacts app and search for that contact.

**Expected:** The Tend record is gone, but the native contact is still
there, completely untouched — deleting in Tend never deletes from the
device.

- [x] Pass (automated 2026-07-07, deleted Alexandra from Tend, confirmed
  "Alexandra (Test)" still present via `content query`) / [ ] Fail
