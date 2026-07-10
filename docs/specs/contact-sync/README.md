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

Resolution: **flag only, no dedicated merge/resolution flow for v1** — and
derived live rather than stored. `PersonDetailViewModel` reactively queries
for other linked people sharing the current person's `nativeLookupKey`
whenever it changes, and shows a duplicate indicator if any are found. The
indicator links to the first other person's detail screen so the user can
resolve it manually using Tend's existing person management (edit, unlink,
or delete one of them) — no new merge UI is built. This mirrors how
broken-link detection already works (flag + let the user act, never
auto-resolve silently) while avoiding the much larger scope of a real
merge-review UI.

Deliberately **not** a stored `duplicateOfPersonId`-style pairwise FK: an
earlier version tried that and it broke down for 3+-way collisions (three
people merged into one native contact can't be represented by a single
"points at one other person" field without picking an arbitrary pair) and
needed an explicit recompute step that could go stale between polls.
Querying live for "who else currently shares my lookup key" is correct for
any group size and can never be stale, at the cost of one extra query per
detail-screen view instead of a pre-computed column.

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
| Two Tend people linked to the same native contact          | Detected live by querying for other linked people sharing the same `nativeLookupKey` (not a stored pairwise flag) — correct for any group size and never goes stale. No dedicated merge/resolution UI — user resolves manually via existing edit/unlink/delete actions. |
| Account for natively-created contacts (Sync to Device)     | Accountless (no `ACCOUNT_NAME`/`ACCOUNT_TYPE`) — matches the existing "no sync-adapter" design (see Non-Goals) but means these contacts are local-only: they don't back up to or appear in the user's Google/cloud account. Documented, not hidden — see the note under "Sync to Device" in the app and the Non-Goals entry below. |
| Category/tag structure                                     | Multiple tags per person (not a single mutually-exclusive category), stored as a plain string list per person plus a separate persisted catalog of every known tag name.                                                                                            |
| Category/tag catalog                                       | Free-form, user-typed — no fixed enum. Ships with two default tags (Family/Friend) on equal footing with any user-typed tag. Persists in the pool independent of current usage — going to zero people wearing a tag does not remove it; only an explicit delete does, which also strips it from anyone still wearing it. |

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
  reads and creates plain local contacts via `ContactsContract`. This is a
  narrower claim than "contacts Tend creates are never associated with an
  account" — see the next bullet.
- **Contacts created via "Sync to Device" are accountless, not attached to
  the user's existing Google/cloud account.** A deliberate simplification,
  not an oversight: attaching to the user's default account would need
  `AccountManager` permission handling and a way to pick an account when
  multiple exist, which is out of scope for v1. The real consequence: these
  contacts are local-only — they don't back up and won't appear on the
  user's other devices. Surfaced to the user via a caption under the "Sync
  to Device" button, not just buried in this doc.
- **No `ContentObserver`-based live sync.** Case 1 refresh is a poll on app
  foreground, not a push-based live update while Tend is backgrounded.
- **Only one phone number and one email per person**, unlike native contacts
  which allow several typed values each (Home/Work/Mobile/Other, one marked
  primary). A multi-value design was drafted in
  [`05_multiple_contact_methods.feature`](./05_multiple_contact_methods.feature)
  (`Person.phoneNumbers: List<PhoneNumber>` / `Person.emails: List<Email>`,
  matching native contacts' `(value, type, isPrimary)` shape) but is
  **cancelled, not deferred** — kept out specifically to keep the UI simple,
  not for lack of a plan. The spec file is preserved as-is rather than
  deleted, in case this is ever reconsidered; it's marked `NOT IMPLEMENTED`
  at the top and should not be built against without first confirming the
  decision has actually changed. Importing from native contacts keeps only
  the contact's designated primary (or an arbitrary one if none is marked);
  any other numbers/emails are intentionally dropped, silently, by design.

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
  same lightweight JSON-list pattern already used for `socialLinks`/`events`.
  Removing a tag from a person doesn't affect anyone else tagged the same
  thing.
- **The tag pool is a persisted catalog, not derived from current usage.**
  A tag stays selectable in the picker even after the last person wearing
  it has it removed — going to zero users is not the same event as being
  deleted. This needs a small separate table (or equivalent persisted set)
  of every known tag name, since a pure `Person.tags` union would silently
  drop a tag the moment nobody currently has it. A tag leaves the pool only
  via an explicit delete action (e.g. long-press a chip in the picker),
  which also strips it from anyone still wearing it. No rename-everywhere
  or color/icon management screen for v1 — deletion is the only pool-level
  operation.
- **Seeded with two default tags.** The app ships with "Family" and
  "Friend" in the pool from first launch, on equal footing with everything
  else in it — no code path treats them differently from a user-typed tag,
  and they can be deleted like any other (see above) if the user doesn't
  want them.
- **Home screen filtering by tag** is in scope for `06` — without it, tags
  are purely decorative, which undercuts the point of a relationship-tending
  app being able to answer "who's overdue, among family?"

## Select all / none (`07`)

Specified in `07_select_all_none.feature`, not yet implemented. A single
control (label toggles between "Select All" and "Select None") above the
contact list in `ImportContactsScreen`, so importing most or all of a long
list doesn't require tapping every checkbox individually.

- **Shown or enabled only when appropriate** — hidden entirely while
  contacts are loading and when the importable list is empty (nothing to
  select). Shown as soon as the list has at least one contact, regardless
  of list length — not suppressed for short lists, since "appropriate" here
  means "there's something to act on," not "the list is long enough to
  bother."
- **Label reflects selection state, not a separate always-visible pair of
  buttons.** Reads "Select All" whenever the current selection is anything
  other than 100% of the visible list (including zero selected or a partial
  selection), and flips to "Select None" only once every visible contact is
  checked. Deselecting even one contact after a full Select All immediately
  reverts the label to "Select All."
- **Scoped to the currently visible list only.** Tapping it never touches
  contacts already imported/linked in an earlier session (they're excluded
  from the list per `01`'s existing filtering) — there's no "select
  everything on the device" behavior, only "select everything importable
  right now."

## Further future work

Still just an idea, no spec written yet — unlike `06` above (shipped) or
`05` (spec'd but cancelled — see Non-Goals), both of which have full
Gherkin coverage:

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

5. **`06_categorization.feature`** — added after `01`/`02` shipped, and
   shipped in this PR. Fully additive (one new list field plus a small
   separate table, no changes to existing fields) — the lowest-risk of the
   set to build in isolation, since it doesn't depend on `03`/`04` (Sync to
   Device and the first-run prompt) at all.

**Status (2026-07-08):** `05_multiple_contact_methods.feature` is
**cancelled, not deferred** — the team decided against it in favor of
keeping the UI simple. The spec file is kept in this directory (marked
`NOT IMPLEMENTED` at the top) rather than deleted, so the design work isn't
lost if this is ever revisited, but nothing should be built against it
without first confirming the decision has changed. See the Non-Goals
section above for the reasoning.
