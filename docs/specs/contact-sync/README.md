# Contact sync — Gherkin specs

These `.feature` files specify behavior for integrating Tend's own contact
database (Room / `PersonEntity`) with the Android on-device Contacts
Provider (`ContactsContract`). They are written for a first contribution to
this repo and are **not wired to an executable Cucumber/Gherkin runner** —
this project currently uses plain JUnit/Espresso (see `app/build.gradle.kts`).
Treat these as acceptance criteria to implement against and/or convert to
instrumented tests; adding Cucumber-JVM + Android is a separate, later
decision.

Automated coverage lives in the app module as usual: JVM unit tests under
`app/src/test/` for use cases/ViewModels, instrumented Compose UI tests
under `app/src/androidTest/` for on-device flows. Alongside each
`NN_name.feature` file is a **Manual Test Plan**
(`NN_name.manual-tests.md`) — step-by-step procedures for a human to run by
hand on a device/emulator, covering the parts that can't be driven by an
in-process test (OS permission dialogs, the native Contacts app). Add one
per feature file as each is implemented.

## Definitions: Case 1 / Case 2 / Case 3

These labels are used throughout this README and the `.feature` files to
refer to the three relationships a person can have between Tend's database
and the device's native Contacts Provider:

- **Case 1 — Device-linked contact.** The person exists in both the native
  device contacts and Tend, and Tend has recorded a link between its
  `PersonEntity` row and that native contact. Identity fields (name, phone,
  email, photo) are owned by the native contact and read-only in Tend, with
  an "Edit in Contacts" action (`Intent.ACTION_EDIT` against the contact's
  lookup URI) so the user always has a direct path to actually change them;
  Tend additionally stores relationship-only data (frequency, notes, events,
  social links) for this person. Covered by `02_device_linked_contact.feature`.
  A Case 1 person's indicator is one of three states: **synced** (normal,
  actively verified on last foreground refresh), **sync paused** (contacts
  permission has been revoked, so Tend can no longer verify the native
  contact still matches — cached data is shown as-is with no claim it's
  current), or **broken link** (the native contact's lookup key no longer
  resolves to anything — confirmed gone, requires the user to Unlink or
  Delete).
- **Case 2 — Tend-only contact.** The person exists only in Tend's database;
  there is no native device contact and no link. Every field is owned and
  editable by Tend. A "Sync to Device" action can promote this person to
  Case 1 by creating a brand-new native contact. Covered by
  `03_tend_only_contact.feature`.
- **Case 3 — Unimported native contact.** The person exists in the native
  device contacts but has never been linked to (or imported into) Tend —
  either because the user hasn't gotten to it yet or deliberately skipped
  it. Tend does not create any record for this contact and does not track
  that it was seen or skipped. It only surfaces as a selectable row in the
  import picker (`01_manual_contact_import.feature`,
  `04_first_run_import_prompt.feature`) until the user chooses to import it,
  at which point it becomes Case 1.

## Confirmed starting state (2026-07-07)

- Tend's Room database (`AppDatabase`, single entity `PersonEntity`) is the
  **sole** current data source for people. No `ContactsContract`, contacts
  permissions, or contact-picker code exists anywhere in the repo today.
- No onboarding / first-run flag mechanism exists yet (no DataStore, no
  SharedPreferences). This will need to be added (e.g. a `Preferences
DataStore` boolean, or a small settings table) to persist "has the
  first-run import prompt been resolved."
- Current `PersonEntity` fields: `id`, `name`, `photoUri`, `phoneNumber`,
  `email`, `events`, `notes`, `socialLinks`, `frequencyDays`,
  `lastContactedAt`, `nextReminderAt`, `isArchived`.

## Linking a Tend person to a native contact

`ContactsContract.Contacts._ID` is **not** a safe long-term link — Android's
own contact aggregation can merge or split raw contacts, changing a given
contact's `_ID` without the contact having been deleted. `name` is even
worse: not unique, not stable, and can legitimately collide across different
people. Android provides a field purpose-built for durable references:
`ContactsContract.Contacts.LOOKUP_KEY`, resolved via
`Contacts.getLookupUri(id, lookupKey)` /
`Contacts.lookupContact(resolver, lookupUri)`, which is documented to remain
valid across aggregation changes that would otherwise invalidate a raw `_ID`.

This avoids the far heavier alternative — registering Tend as an
`AccountManager` account type with its own sync adapter and owning
`RawContacts` rows — which is not warranted for a read-only cache-and-link
model.

New `PersonEntity` fields needed to support Case 1:

- `nativeLookupKey: String?` — the durable link (`LOOKUP_KEY`). Null means
  Case 2 (no link).
- `nativeContactId: Long?` — a cached `_ID` used as a fast-path for direct
  lookups; re-resolved via the lookup key (not treated as authoritative) if
  the cached id no longer matches a row.
- `isDeviceLinkBroken: Boolean` — set when the lookup key itself fails to
  resolve to any native contact (i.e. the contact was actually deleted, not
  merely reorganized by aggregation).
- `localPhotoPath: String?` — a locally-cached copy of the contact's photo
  bytes (app-private storage), refreshed on each successful foreground poll.
  Unlike name/phone/email (plain text, cheap to store inline), the native
  photo is otherwise only reachable via a `content://` URI gated by
  `READ_CONTACTS` — if that permission is revoked, a URI-only reference would
  go unreadable even though this README's "sync paused" state promises
  previously-cached data keeps displaying. A local byte copy is the one
  additional justified duplication beyond the read-through cache described
  below, specifically to make that promise true for photos too.

### Post-hoc native merges across two Tend people (duplicate flag)

Android's contact aggregation can merge two previously-separate native
contacts into one at any time (independent of anything Tend does). If two
different Tend people were each independently linked or synced to what turns
out to be the same real person under two different raw contacts, an
aggregation merge could leave two `PersonEntity` rows resolving to the same
`LOOKUP_KEY`.

Resolution: **flag only, no dedicated merge/resolution flow for v1.** During
the existing foreground poll, when resolving a person's `nativeLookupKey`,
check whether that lookup key is already claimed by another Tend person. If
so, set `duplicateOfPersonId: Long?` on both rows (referencing each other)
and show a duplicate indicator on each. The indicator links to the other
person's detail screen so the user can resolve it manually using Tend's
existing person management (edit, unlink, or delete one of them) — no new
merge UI is built. The flag is recomputed on every poll, so it clears itself
once the underlying collision goes away (e.g. the user unlinks or deletes
one of the two). This mirrors how broken-link detection already works
(flag + let the user act, never auto-resolve silently) while avoiding the
much larger scope of a real merge-review UI.

New `PersonEntity` field: `duplicateOfPersonId: Long?` — set on both sides
when a lookup-key collision is detected; `null` otherwise.

## Design decisions (from stakeholder interview)

| Question                                                  | Decision                                                                                                                                                                                                                                                            |
| --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Field ownership for linked (Case 1) contacts              | Native Contacts Provider owns identity fields (name, phone, email, photo) — read-only in Tend, editable only in the system Contacts app. Tend owns all relationship fields (frequency, notes, events, social links, `lastContactedAt`, `nextReminderAt`, archived). |
| Sync trigger for Case 1                                   | Poll/refresh on app foreground (no `ContentObserver`).                                                                                                                                                                                                              |
| Sync-to-device action for Case 2                          | Always creates a **new** native contact and links it — no fuzzy match/search against existing native contacts.                                                                                                                                                      |
| Case 3 tracking                                           | None. The import picker always queries the live native contact list and excludes only contacts already linked to a Tend person. There is no "declined" memory per contact.                                                                                          |
| Permission denied after tapping "Yes" on first-run prompt | Treated identically to tapping "No."                                                                                                                                                                                                                                |
| Native contact deleted after being linked (Case 1)        | **Not** auto-demoted. Flagged as a broken link; user must explicitly choose "Unlink" (→ becomes Case 2, last-known data retained and becomes editable) or "Delete" (removes the Tend person entirely).                                                              |
| Duplicate prevention in import pickers                    | The picker (first-run and manual) always excludes native contacts already linked to an existing Tend person.                                                                                                                                                        |
| Deleting a device-linked (Case 1) Tend person             | Tend-side only — removes the Tend record and the link, never touches or deletes the underlying native contact, consistent with never destroying data Tend doesn't own.                                                                                              |
| Photo caching for Case 1                                  | A local byte copy is cached (`localPhotoPath`), not just a URI reference — the one field where extra duplication is justified, so photos survive a permission-revoked "sync paused" state the same way text fields already do.                                      |
| Syncing an archived device-linked contact                 | Sync keeps running as normal while archived — archiving only affects visibility in Tend's lists, not data freshness.                                                                                                                                                |
| Permission permanently denied ("don't ask again")         | Detected and handled distinctly from a one-off denial: no repeated OS dialog: the user is guided to the app's system settings page instead. Applies to both `READ_CONTACTS` (import/refresh) and `WRITE_CONTACTS` (Sync to Device).                                 |
| Two Tend people linked to the same native contact          | Flag only for v1 (`duplicateOfPersonId`), detected during the existing foreground poll. No dedicated merge/resolution UI — user resolves manually via existing edit/unlink/delete actions.                                                                          |

## Non-Goals (v1)

Things considered and deliberately **not** built, at least for now — if
you're wondering "why doesn't this handle X," check here before assuming
it's an oversight. (See the decision table above for the reasoning behind
each.)

- **No fuzzy-matching an existing native contact on "Sync to Device."** A
  Case 2 → Case 1 promotion always creates a brand-new native contact,
  never searches for a possible existing match.
- **No memory of "declined" Case 3 contacts.** The import picker always
  shows the live, current set of unlinked native contacts — skipping a
  contact once doesn't hide it from future picker sessions.
- **No dedicated merge-review UI for duplicate Tend people.** When two
  Tend people end up linked to the same native contact, they're flagged
  and left for the user to resolve manually (edit/unlink/delete) — no
  side-by-side merge screen.
- **No account-type / sync-adapter integration.** Tend never registers
  itself as an `AccountManager` account or owns `RawContacts` rows; it only
  reads and creates plain local contacts via `ContactsContract`.
- **No `ContentObserver`-based live sync.** Case 1 refresh is a poll on app
  foreground, not a push-based live update while Tend is backgrounded.

## Future Implementation

Known gaps that are out of scope for the current contact-sync work, not
because they were rejected, but because nobody's built them yet. Each has
an inline `TODO` comment at the referenced file(s).

- **Multiple phone numbers/emails per person.** `Person.phoneNumber` and
  `Person.email` are singular fields, not lists — this predates
  contact-sync entirely, it's how Tend already stored these before this
  feature existed. Native contacts allow arbitrarily many typed phone
  numbers/emails (Home/Work/Mobile/Other), optionally with one marked as
  the contact's own default (`IS_SUPER_PRIMARY`). `NativeContactsDataSource`
  picks that designated default when one exists, falling back to an
  arbitrary row otherwise — any additional numbers/emails on the native
  contact are silently dropped on import, and there's no way to add a
  second phone/email to a Tend person at all today, imported or not.
  Supporting this would need `phoneNumber`/`email` to become lists (a Room
  migration) plus UI changes to `AddPersonScreen`/`PersonDetailScreen` for
  multi-value entry. `TODO` comments: `Person.kt`, `PersonEntity.kt`,
  `NativeContactsDataSource.kt`.
- **A real merge-review UI for duplicate Tend people**, as an upgrade from
  the flag-only v1 behavior above — letting the user pick which fields
  survive from each of the two records instead of manually editing/deleting
  one themselves.

## Why cache identity fields locally at all instead of just querying live? (duplication justification)

Caching a read-only copy of `name`/`phoneNumber`/`email` (plain text) plus a
locally-stored photo byte copy (`localPhotoPath`, see above) inside
`PersonEntity` for linked (Case 1) contacts — rather than joining against
`ContactsContract` on every read — is a deliberate, bounded duplication:

- It is a **read-through cache with a single system of record** (the device
  Contacts Provider), refreshed on a defined trigger (app foreground), not an
  independently-editable second copy. This is the same pattern Android's own
  documentation recommends for apps that extend contact data (see
  `ContactsContract.RawContacts` / `ContactsContract.Data`'s "sync adapter"
  model), and the same approach used by mainstream contact-syncing apps
  (WhatsApp, Signal, Telegram) — cache display name/photo locally for
  offline-first rendering and query performance, but never treat the local
  copy as authoritative.
- It preserves this app's **offline-first** guarantee (per `README.md`):
  `ContactsContract` queries require the content resolver and (rarely) can be
  slow or momentarily unavailable; a local cache means the home/list screens
  never block on it.
- The cache is never a source of divergence because Case 1 identity fields
  are **read-only in Tend UI** — there is exactly one place a user can edit
  them (the system Contacts app), and Tend's copy is overwritten, never
  merged, on the next successful poll.

## Feature files (numbered in suggested implementation order)

1. **`01_manual_contact_import.feature`** — build this first. It establishes
   the core mechanism everything else depends on: the `deviceContactId` link
   field on `PersonEntity` (a Room migration), the `READ_CONTACTS` permission
   flow, the selective picker, and "create a linked Tend person from a
   native contact." It's reachable from a plain menu item, so it's demoable
   and testable in isolation before anything else touches it.
2. **`02_device_linked_contact.feature`** — once contacts can become linked
   via step 1, implement Case 1's steady-state behavior: read-only identity
   fields, the device-managed indicator, foreground-poll refresh, and
   broken-link detection/resolution (Unlink / Delete).
3. **`03_tend_only_contact.feature`** — Case 2's indicator is nearly free
   once the link field exists (it's just "link is null"), but "Sync to
   Device" is a new reverse-direction path (Tend → native, `WRITE_CONTACTS`)
   that reuses the create-and-link plumbing from step 1.
4. **`04_first_run_import_prompt.feature`** — build this last. It's the
   thinnest layer: a persisted "resolved" flag plus a launch-time trigger
   condition, wrapping the exact picker already built in step 1. Doing it
   last avoids building the flag/trigger logic against a picker that
   doesn't exist yet.

All four ultimately need the same foundational schema change (the
`deviceContactId` / broken-link fields on `PersonEntity`), so that migration
should land as part of step 1 rather than being deferred — everything after
it assumes the field exists. Implementing all four in parallel isn't
recommended: steps 2–4 all build on data-model and picker code introduced in
step 1, so parallel work would mean guessing at that plumbing's shape (or
redoing it) rather than reusing what step 1 actually produces.
