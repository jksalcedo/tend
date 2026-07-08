Feature: Contact categorization (tags)
  As a Tend user
  I want to tag the people I'm tending with free-form labels like "Family" or "Book Club"
  So that I can organize and filter my connections the way that makes sense to me, independent of anything native contacts do

  Background:
    Given a Tend person "Priya" exists

  Scenario: Tags are entirely Tend-owned, even for a linked contact
    Given "Priya" is linked to a native device contact
    When the user adds the tag "Family" to Priya
    Then the tag should be saved and shown, regardless of Priya's Case 1/Case 2 status
    And no data should be written to or read from the native Contacts app's own Groups feature

  Scenario: A person can have multiple tags
    Given "Priya" has the tag "Family"
    When the user adds the tag "Neighbor"
    Then Priya should show both "Family" and "Neighbor"

  Scenario: Adding a tag offers a few common starting suggestions
    When the user opens the tag picker for a person with no tags yet
    Then a short list of common suggested tags should be shown as quick-select chips (e.g. Family, Friend, Work, Acquaintance)
    And the user can also type a new, custom tag not in that list

  Scenario: A newly typed custom tag becomes available as a suggestion for other people
    Given the user has typed and saved a custom tag "Book Club" for one person
    When the user opens the tag picker for a different person
    Then "Book Club" should appear as a selectable suggestion alongside the built-in starter tags

  Scenario: Removing a tag from a person does not delete it globally
    Given both "Priya" and "Marco" have the tag "Family"
    When the user removes the tag "Family" from Priya only
    Then Priya should no longer show "Family"
    And Marco should still show "Family"
    And "Family" should still appear as a suggestion when tagging other people

  Scenario: Tags are shown on the person detail screen
    Given "Priya" has the tags "Family" and "Neighbor"
    When the user views Priya's person detail screen
    Then both tags should be visible

  Scenario: The home screen can be filtered by tag
    Given multiple connections exist with a mix of tags
    When the user filters the home screen by the tag "Family"
    Then only connections tagged "Family" should be shown
    And clearing the filter shows all connections again

  Scenario: A person can have no tags at all
    Given "Marco" has never been tagged
    When the user views Marco's person detail screen
    Then no tag section clutter is shown — tags remain fully optional
