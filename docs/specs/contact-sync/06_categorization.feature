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

  Scenario: The app ships with two default tags
    Given a fresh install with no tags ever created by the user
    When the user opens the tag picker for any person
    Then "Family" and "Friend" should already be available as selectable quick-select chips
    And the user can also type a new, custom tag not in that list

  Scenario: The default tags are not special — just pre-seeded
    Given the app's default tags "Family" and "Friend"
    Then they should behave exactly like any user-created tag (assignable, renamable, deletable)
    And no code path should treat them differently from a custom tag like "Book Club"

  Scenario: A newly typed custom tag becomes available as a suggestion for other people
    Given the user has typed and saved a custom tag "Book Club" for one person
    When the user opens the tag picker for a different person
    Then "Book Club" should appear as a selectable suggestion alongside "Family" and "Friend"

  Scenario: Removing a tag from a person does not delete it globally
    Given both "Priya" and "Marco" have the tag "Family"
    When the user removes the tag "Family" from Priya only
    Then Priya should no longer show "Family"
    And Marco should still show "Family"
    And "Family" should still appear as a suggestion when tagging other people

  Scenario: A tag stays in the pool even when no one has it anymore
    Given "Priya" is the only person with the custom tag "Book Club"
    When the user removes the tag "Book Club" from Priya
    Then no person should have the tag "Book Club" anymore
    But "Book Club" should still appear as a selectable suggestion in the tag picker
    # The pool is a persisted catalog of every tag ever created, not something
    # derived from who currently has what — removing the last usage of a tag
    # is not the same action as deleting the tag itself.

  Scenario: A tag is only removed from the pool by explicit deletion
    Given the tag "Book Club" exists in the pool but is attached to no one
    When the user explicitly deletes "Book Club" from tag management
    Then "Book Club" should no longer appear as a suggestion in the tag picker
    And any person still wearing "Book Club" should have it removed too

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
