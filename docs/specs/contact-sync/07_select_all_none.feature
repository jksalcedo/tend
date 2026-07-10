Feature: Select all / none in the contact import picker
  As a Tend user importing many contacts at once
  I want separate "Select All" and "Select None" actions, each enabled only when useful
  So that I don't have to tap each checkbox individually when I want most or all of them

  Background:
    Given the selective contact import picker is open with contacts permission granted

  Scenario: Select-all/none buttons are hidden while contacts are loading
    Given the importable contacts list is still loading
    Then neither the "Select All" nor "Select None" button should be shown

  Scenario: Select-all/none buttons are hidden when there are no importable contacts
    Given the importable contacts list has finished loading and is empty
    Then neither the "Select All" nor "Select None" button should be shown

  Scenario: Select-all/none buttons appear once contacts are loaded
    Given the importable contacts list has finished loading and contains one or more contacts
    Then both a "Select All" button and a "Select None" button should be shown above the contact list

  Scenario: "Select All" is enabled and "Select None" is disabled when nothing is selected
    Given one or more importable contacts are shown
    And none of them are currently selected
    Then the "Select All" button should be enabled
    And the "Select None" button should be disabled

  Scenario: Both buttons are enabled when only some contacts are selected
    Given multiple importable contacts are shown
    And at least one, but not all, are currently selected
    Then the "Select All" button should be enabled
    And the "Select None" button should be enabled

  Scenario: Tapping "Select All" selects every currently visible contact
    Given multiple importable contacts are shown
    And zero or more, but not all, are currently selected
    When the user taps the "Select All" button
    Then every contact currently shown in the list should become selected
    And the Import button's count should match the total number of visible contacts

  Scenario: "Select All" becomes disabled and "Select None" becomes enabled once everything is selected
    Given multiple importable contacts are shown
    When the user taps the "Select All" button
    Then the "Select All" button should become disabled
    And the "Select None" button should be enabled

  Scenario: Tapping "Select None" clears every selection
    Given every currently visible contact is selected
    When the user taps the "Select None" button
    Then no contact should remain selected
    And the Import button's count should read 0
    And the "Select All" button should become enabled again
    And the "Select None" button should become disabled again

  Scenario: Deselecting a single contact after Select All re-enables "Select All"
    Given every currently visible contact is selected
    When the user deselects one individual contact via its own checkbox
    Then the "Select All" button should become enabled again
    And the "Select None" button should remain enabled
    And every other contact should remain selected

  Scenario: Select all only affects contacts currently in the list, not previously imported ones
    Given a contact was already imported and linked in an earlier session
    And it does not appear in the current picker list
    When the user taps "Select All"
    Then only the contacts currently shown in the list become selected
    And no already-linked contact is affected
