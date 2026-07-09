Feature: Externalize UI strings into string resources
  As a Tend maintainer
  I want every user-facing piece of text to live in res/values/strings.xml instead of hardcoded in Kotlin
  So that Weblate (or any translation tool) has something to actually translate, and Android's own locale fallback can work

  Background:
    Given Tend's res/values/strings.xml currently contains only the auto-generated "app_name" entry
    And every other user-facing string is a literal passed directly to Text(...) in Compose code

  Scenario: A screen's visible text is defined as a string resource, not a literal
    Given a Composable that previously called Text("Search connections...")
    When the string is externalized
    Then res/values/strings.xml should contain a named entry for that text
    And the Composable should call Text(stringResource(R.string.<name>)) instead of a literal
    And the rendered text on screen should be unchanged from before the change

  Scenario: A string with a runtime value uses a formatted resource, not string concatenation
    Given a Composable that previously built text like "Check in every ${p.frequencyDays} days" via a Kotlin string template
    When the string is externalized
    Then res/values/strings.xml should define a formatted string resource with a placeholder for the value
    And the Composable should call stringResource(R.string.<name>, p.frequencyDays) instead of concatenating

  Scenario: A missing translation for a given locale falls back to the base string, automatically
    Given a string resource exists in the base res/values/strings.xml
    And no translated value exists yet for a given locale's res/values-xx/strings.xml
    When the app runs on a device set to that locale
    Then the base (untranslated) string should be shown for that key
    And no crash, blank text, or missing-resource error should occur
    # This is native Android resource-resolution behavior — nothing to build here,
    # this scenario exists to document and verify it's actually relied upon correctly.

  Scenario: A newly added string is never introduced as a hardcoded literal
    Given a contributor is adding a new piece of UI text to any screen
    When they write the Composable code
    Then the text should be defined in res/values/strings.xml
    And referenced via stringResource(...), not written as a literal
    # Contribution-guideline scenario — worth adding a CONTRIBUTING.md note once 01 lands,
    # so new hardcoded strings don't creep back in after externalization is done.

  Scenario: Plurals use Android's plurals resource, not manual if/else string branching
    Given existing code that branches on a count to choose singular vs. plural wording (e.g. "connection" vs. "connections")
    When the string is externalized
    Then res/values/strings.xml should define a <plurals> resource for that concept
    And the Composable should call the plural-aware resource lookup instead of a manual branch
    # Not just cosmetic: different languages have different plural rules (some have more
    # than two forms), which manual singular/plural branching can't represent correctly.
