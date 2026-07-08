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
| Multi-value phone/email shape                              | Full metadata per entry — `(value, type, isPrimary)` — not a bare list of strings, so it round-trips losslessly with native contacts' own shape.                                                                                                                    |
| Sync to Device with multiple phone numbers/emails           | All entries are written to the new native contact, not just the primary one — full fidelity, consistent with the 1:1 sync goal.                                                                                                                                    |
| Category/tag structure                                     | Multiple tags per person (not a single mutually-exclusive category), stored as a plain string list — no separate `Tag` entity/table.                                                                                                                                |
| Category/tag catalog                                       | Free-form, user-typed — no fixed enum. Seeded with a small built-in set of common suggestions (Family/Friend/Work/Acquaintance) plus every tag already used elsewhere, both offered as quick-select chips alongside free typing.                                    |

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

## Multiple phone numbers and emails (`05`)

`Person.phoneNumber`/`Person.email` were singular fields (a pre-existing
constraint predating contact-sync), silently dropping every number/email on
a native contact beyond the one `NativeContactsDataSource` picked as
default. `05_multiple_contact_methods.feature` replaces this:

- `Person.phoneNumbers: List<PhoneNumber>` / `Person.emails: List<Email>`,
  where `PhoneNumber`/`Email` are `(value, type, isPrimary)` — matching
  native contacts' own shape (type + a default flag), not a bare
  `List<String>`, so re-syncing later doesn't lose which was Home vs Work
  or which was the default. Stored the same way `socialLinks`/`events`
  already are: a Room `TypeConverter`-backed JSON column, no new entity
  table needed.
- **Case 1 (linked):** still fully read-only in Tend, same as today —
  `NativeContactsDataSource` now reads every phone/email row instead of
  just the default one; "Edit in Contacts" remains the only way to change
  them.
- **Case 2 (Tend-only):** freely add/remove/edit entries and mark one
  primary, the same add/remove-list UI pattern `AddPersonScreen` already
  uses for social links and events.
- **Sync to Device** writes every entry as its own native data row (not
  just the primary one) — full fidelity in both directions, matching the
  "synced back and forth 1:1" goal.
- **QR sharing** is the one place this is intentionally *not* 1:1: only the
  primary phone/email go into the shared payload, to keep the QR code from
  growing with every extra number/email a contact has. If nothing is marked
  primary, the first entry in the list is the effective default for both
  QR sharing and Sync to Device.

## Contact categorization / tags (`06`)

Free-form tags (e.g. "Family", "Book Club"), specified in
`06_categorization.feature`:

- **Deliberately not synced with native Contacts Groups.** Android Groups
  are heterogeneous and often account-driven (a work directory's org unit,
  Google's auto-populated "Starred", a SIM import group) — frequently not
  even user-editable, and inconsistent device to device. That's a poor
  match for "how I want to organize the relationships I'm tending," which
  is what this feature is actually for. Tags are a relationship field, like
  frequency/notes/events — Tend-owned regardless of Case 1/2 status, never
  read from or written to `ContactsContract.Groups`.
- **Multiple tags per person**, stored as `Person.tags: List<String>` —
  same lightweight pattern as phone numbers/emails above, no separate `Tag`
  entity/table. A tag is just a string; there's no rename-everywhere or
  color/icon management screen for v1 (removing a tag from a person doesn't
  affect anyone else tagged the same thing).
- **Free-form creation, seeded with starter suggestions.** The user can
  type any new tag; a small built-in list (e.g. Family, Friend, Work,
  Acquaintance) plus every tag already used across other people appear as
  quick-select chips, so you're rarely typing the same tag twice by hand.
- **Home screen filtering by tag** is in scope for `06` — without it, tags
  are purely decorative, which undercuts the point of a relationship-tending
  app being able to answer "who's overdue, among family?"

## Further future work

Still just an idea, no spec written yet — unlike `05`/`06` above, which
have full Gherkin coverage:

- **A real merge-review UI for duplicate Tend people**, as an upgrade from
  the flag-only v1 behavior described under "Post-hoc native merges" —
  letting the user pick which fields survive from each of the two records
  instead of manually editing/deleting one themselves.

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

5. **`05_multiple_contact_methods.feature`** and
   **`06_categorization.feature`** — added after `01`/`02` shipped. Neither
   depends on `03`/`04` (Sync to Device and the first-run prompt), so they
   don't have to wait in line behind those — `05`/`06` could ship before,
   after, or interleaved with `03`/`04` without rework. `05` does touch the
   same `Person`/`PersonEntity` model `01`/`02` already extended, and
   `ShareScanSheet`'s `SharedPerson` QR payload shape, so it's a moderate
   migration regardless of when it lands. `06` is fully additive (one new
   list field, no changes to existing fields) and is the lowest-risk of the
   six to build in isolation.
