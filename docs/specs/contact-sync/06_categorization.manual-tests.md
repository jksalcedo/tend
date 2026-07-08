# Manual Test Plan: Contact Categorization (Tags)

Step-by-step procedures for a human tester to execute by hand on a real
device or emulator. Section titles match the `Scenario` names in
[`06_categorization.feature`](./06_categorization.feature) for
traceability.

Unlike `04`, tags are not a use-once gate — most scenarios can be chained
in one continuous session. Only Group A needs a genuinely fresh install,
to verify the two default tags exist before any tag has ever been
created by hand.

## Prerequisites

- A device or emulator running the debug build (`./gradlew installDebug`).
- At least two Tend people already added (e.g. "Priya" and "Marco") before
  starting Group B onward.

## Group A: default tags on a fresh install

**Precondition:** Fresh app data clear (`adb shell pm clear
com.jksalcedo.tend`), no tags ever created.

### 3. The app ships with two default tags

**Steps:**
1. Open Tend, add a person named "Priya."
2. Open Priya's detail screen and tap "+ Add tag."

**Expected:** "Family" and "Friend" are already listed as selectable
suggestions, with no prior tag ever having been created.

- [x] Pass (automated 2026-07-08: fresh install, added Priya, opened the
  tag picker — "Family" and "Friend" already present as suggestions.
  Also visible immediately on Home's tag filter row ("All / Family /
  Friend") before any tag was ever assigned to anyone, confirming the
  `RoomDatabase.Callback.onCreate` seeding path.)

### 4. The default tags are not special — just pre-seeded

**Precondition:** A default tag exists and is applied to at least one
person (tested later in the flow, once "Family" was applied to Marco).
**Steps:**
1. Long-press the "Family" suggestion chip in a tag picker where it's
   not already applied to the current person.
2. Confirm the deletion when prompted.
3. Reopen the tag picker / check Home's filter row.

**Expected:** "Family" is deleted like any other tag would be — the
confirmation dialog and outcome are identical to deleting a custom tag
(see Group D). No special-cased behavior.

- [x] Pass (automated 2026-07-08: long-pressed "Family" from Priya's tag
  picker — confirmation dialog read exactly `Delete tag "Family"?` /
  "This removes \"Family\" from every connection that has it and takes
  it out of the tag list everywhere — this can't be undone," identical
  wording/behavior to deleting the custom tag "Neighbor" earlier in the
  same session. After confirming, "Family" disappeared from Home's
  filter row and from Marco's Tags section, where it had been applied —
  confirming the cascade and the lack of any special-casing.)

## Group B: assigning and viewing tags

**Precondition:** At least two people exist ("Priya," "Marco"). Continuing
from Group A is fine, or a normal (non-cleared) app state.

### 1. Tags are entirely Tend-owned, even for a linked contact

**Steps:**
1. Open a person's detail screen.
2. Tap "+ Add tag," select "Family," confirm it's applied.

**Expected:** "Family" appears on the person's Tend detail screen.
Nothing about it is read from or written to the native Contacts app's
own Groups/Labels.

- [x] Pass (automated 2026-07-08: applied "Family" to Priya via the tag
  picker — appeared correctly as a chip on her detail screen. The
  "nothing written to native Groups" half of this scenario is verified
  by code inspection rather than live-testing against the native
  Contacts app: `AddTagToPersonUseCase`/`RemoveTagFromPersonUseCase`/
  `DeleteTagUseCase` only ever touch `PersonRepository`/`TagRepository`
  — no code path in the tag feature references `ContactsContract.Groups`
  or any native contacts API at all, so there is nothing for a live
  check against the native Contacts app to catch that static inspection
  doesn't already rule out.)

### 2. A person can have multiple tags

**Steps:**
1. On the same person, tap "+ Add tag" again and add a second tag.

**Expected:** Both tags are shown as chips on the detail screen.

- [x] Pass (automated 2026-07-08: added the custom tag "Neighbor" to
  Priya in addition to "Family" — both "Family" and "Neighbor" chips
  displayed correctly together.)

### 9. Tags are shown on the person detail screen

**Expected:** Confirmed by the two prior sections — both tags remain
visible on the detail screen without needing to reopen the picker.

- [x] Pass (automated 2026-07-08: confirmed via the same screenshot as
  section 2 — both chips visible directly on the detail screen.)

### 11. A person can have no tags at all

**Steps:**
1. Open a different person ("Marco") who has never been tagged.

**Expected:** The Tags section shows no chips (just the "+ Add tag"
affordance) — no placeholder clutter.

- [x] Pass (automated 2026-07-08: Marco's Tags section showed only the
  "+ Add tag" chip with no placeholder text before he was ever tagged;
  also re-confirmed for Priya at the end of the session after her tags
  were removed/deleted.)

## Group C: pool behavior — custom tags and removal

### 5. A newly typed custom tag becomes available as a suggestion for other people

**Steps:**
1. On Priya's detail screen, tap "+ Add tag," type a new tag into the
   text field, and tap "Add."
2. Open Marco's detail screen and tap "+ Add tag."

**Expected:** The new tag appears as a selectable suggestion for Marco,
alongside whatever else is in the pool — without Marco ever having had
it applied.

- [x] Pass (automated 2026-07-08: typed and added the custom tag
  "Neighbor" for Priya, then opened Marco's tag picker — "Neighbor"
  appeared as a suggestion alongside "Family"/"Friend," despite Marco
  never having had it applied.)

### 6. Removing a tag from a person does not delete it globally

**Precondition:** Both Priya and Marco have the tag "Family."
**Steps:**
1. On Priya's detail screen, tap the "x" on the "Family" chip to remove
   it from her only.
2. Open the tag picker for Marco.

**Expected:** Priya no longer shows "Family." Marco still shows it.
"Family" still appears as a pool suggestion.

- [x] Pass (automated 2026-07-08: both Priya and Marco had "Family";
  removed it from Priya via her chip's "x" — she was left with only
  "Neighbor," Marco's "Family" chip was unaffected, and "Family"
  remained selectable in the pool.)

### 7. A tag stays in the pool even when no one has it anymore

**Steps:**
1. Ensure a custom tag is applied to exactly one person.
2. Remove it from that person via its chip's "x."
3. Open the tag picker for any person.

**Expected:** No person has the tag anymore, but it still appears as a
selectable suggestion in the picker — going to zero users does not
remove it from the pool.

- [x] Pass (automated 2026-07-08: "Neighbor" was applied only to Priya;
  removed it from her via the chip's "x," leaving zero people with it.
  Reopening the tag picker still showed "Neighbor" as a selectable
  suggestion.)

## Group D: explicit tag deletion

### 8. A tag is only removed from the pool by explicit deletion

**Precondition:** A tag exists in the pool attached to no one (from
section 7).
**Steps:**
1. Open the tag picker for any person.
2. Long-press the tag's suggestion chip.
3. Confirm the deletion in the dialog that appears.
4. Check the pool elsewhere (another person's picker, Home's filter row).

**Expected:** A confirmation dialog appears before deletion, explaining
the tag will be removed everywhere. After confirming, the tag no longer
appears as a suggestion for anyone, and no person still shows it as an
applied tag.

- [x] Pass (automated 2026-07-08: long-pressed "Neighbor" (zero users) in
  the tag picker — confirmation dialog appeared with the expected
  explanatory text; confirmed deletion; "Neighbor" no longer appeared in
  the picker's suggestions afterward. Also separately re-verified with
  "Family" while it was still applied to Marco — see section 4's
  evidence for the cascade-removal confirmation.)

## Group E: home screen filtering

### 10. The home screen can be filtered by tag

**Precondition:** At least two people exist with a mix of tags (Marco
had "Family," Priya did not, at the point this was tested).
**Steps:**
1. On Home, locate the row of tag filter chips below the search bar.
2. Tap the "Family" chip.
3. Tap "All" to clear the filter.

**Expected:** Selecting "Family" narrows the connections list to only
those tagged "Family." Clearing the filter shows every connection again.

- [x] Pass (automated 2026-07-08: with only Marco tagged "Family,"
  tapping the "Family" filter chip on Home narrowed the list to Marco
  only, hiding Priya. Tapping "All" restored both to the list.)
