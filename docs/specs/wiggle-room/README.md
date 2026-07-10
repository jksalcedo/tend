# Wiggle room — softening reminder cadence

Tend currently reminds a user to reach out on a perfectly rigid schedule:
`CheckInUseCase` sets `nextReminderAt = lastContactedAt + frequencyDays`, with
no variation at all. A fixed N-day cadence is easy to reason about but reads
as mechanical — a contact who notices "he messages me every 15 days, exactly"
stops feeling like a friend and starts feeling like a CRM entry. This doc
captures the design discussion on how to soften that without turning the
reminder itself into something that feels arbitrary or unreliable.

**Status (2026-07-10):** design discussion only. Nothing in this doc is
implemented yet — no schema change, no use-case change. Treat this as the
starting point for a future spec/feature file, not a description of current
behavior.

## Confirmed starting state

- `Person.frequencyDays: Int` (`domain/model/Person.kt`) is the only cadence
  field that exists today.
- `CheckInUseCase` (`domain/usecase/CheckInUseCase.kt`) computes
  `nextReminderAt` as a pure `lastContactedAt + frequencyDays` addition — no
  randomness, no jitter, no per-contact variation of any kind.
- `DateUtils` (`utils/DateUtils.kt`) only has calendar/day-math helpers
  (`getNextOccurrence`, `daysUntil`); it has no concept of a reminder window.

## Naming

Internal terms like "jitter," "variance," and "random" describe the mechanism
correctly but are the wrong register for anything user-facing in an app whose
whole pitch is *gentle* and *personal* — a settings screen that says "±5 days
random variation" undercuts the thing it's trying to produce.

| Layer | Term | Why |
| --- | --- | --- |
| User-facing copy | **"wiggle room"** | Plain, warm, sounds like something a person would say ("every 15 days, with some wiggle room"), not a scheduler config. |
| Internal field name | `flexDays` | Sits naturally next to the existing `frequencyDays`, keeps the `Int`-days shape consistent. |

Other terms considered and rejected: "flexibility" (fine, slightly corporate
for a settings label), "give or take" (good in a sentence, awkward as a field
name), "slack"/"buffer" (read technical or deadline-flavored, wrong emotional
register for a relationship app).

**"Leeway" is still under consideration, not yet decided against.** Cleaner
and slightly more neutral/grown-up than "wiggle room" — reads well both as a
standalone settings label ("Leeway: 5 days") and in a sentence ("every 15
days, with some leeway"), without the more playful/casual tone "wiggle room"
carries. Final naming call is still open between the two.

## Nuances beyond a flat ± value

A single flat `flexDays` constant is the obvious first cut, but a few
refinements were identified that matter more than they first appear to:

- **Scale proportionally to `frequencyDays`, not as a flat constant.** ±5 days
  is reasonable padding on a 90-day cadence and absurd on a 3-day one. Derive
  a default `flexDays` as roughly 20–30% of `frequencyDays` rather than a
  single hardcoded number for every contact.
- **Skew the window early, not symmetric.** A reminder that can only ever
  arrive a few days *after* the "ideal" date effectively starts every cycle
  already overdue. Biasing the random draw toward *earlier* than the base
  date (e.g. a wider allowance before the target than after it) reads as
  proactive instead of guilt-inducing.
- **Seed jitter deterministically per contact per cycle, not freshly random
  on every read.** If `nextReminderAt` is computed more than once before it
  fires, it must not visibly move around. Seeding off something stable (e.g.
  a hash of `personId` + cycle count) rather than calling `Random()` fresh
  each time keeps the date stable within a cycle while still varying cycle to
  cycle.
- **Decorrelate contacts to avoid notification pile-up.** Several contacts
  sharing the same `frequencyDays` (e.g. everyone set to "monthly") will
  otherwise all land on the same day. Jitter that's independent per contact
  spreads reminders out — a real scheduling win on top of the "feels less
  robotic" goal.
- **Let closer relationships stay tighter.** Someone tended weekly likely
  wants more consistency (small or no wiggle room); someone tended every few
  months can tolerate a wider window. Falls mostly out of the proportional
  scaling above, but may warrant an explicit per-contact override later.
- **Feed real behavior back into the base cadence, not just the padding.**
  If a user consistently checks in a few days later than `nextReminderAt`
  across many cycles, that's a signal `frequencyDays` itself is a little off
  for that contact — worth surfacing as a suggested cadence adjustment
  eventually, rather than only ever widening the jitter to paper over it.

## Open questions / non-goals (not yet decided)

- Whether `flexDays` is user-editable per contact (a slider/stepper in
  `AddPersonScreen`/`PersonDetailScreen`) or purely a derived, invisible
  implementation detail for v1.
- Whether the early-skew behavior needs to be tunable or is just a fixed
  internal ratio.
- Whether the "learn from actual check-in timing" idea is in scope for a
  first version at all, or is future work — it implies tracking a history of
  check-in vs. reminder deltas that doesn't exist anywhere in the schema
  today.
- No decision yet on where `flexDays` lives in the schema (new `PersonEntity`
  column vs. a computed value derived purely from `frequencyDays` with no
  stored state). This determines whether a Room migration is needed at all.

## Next step

Turn the decisions above into a numbered feature spec (following the
`docs/specs/contact-sync/` convention) once the open questions have answers —
in particular, whether `flexDays` is stored or purely derived, since that
determines whether a migration is in scope.
