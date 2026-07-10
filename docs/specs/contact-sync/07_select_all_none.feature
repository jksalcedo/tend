Feature: Select all / none in the contact import picker
  As a Tend user importing many contacts at once
  I want a single control to select or clear every visible contact
  So that I don't have to tap each checkbox individually when I want most or all of them

  Background:
    Given the selective contact import picker is open with contacts permission granted

  Scenario: Select-all control is hidden while contacts are loading
    Given the importable contacts list is still loading
    Then no select-all/none control should be shown

  Scenario: Select-all control is hidden when there are no importable contacts
    Given the importable contacts list has finished loading and is empty
    Then no select-all/none control should be shown

  Scenario: Select-all control appears once contacts are loaded
    Given the importable contacts list has finished loading and contains one or more contacts
    Then a select-all/none control should be shown above the contact list

  Scenario: Control reads "Select All" when nothing is selected
    Given one or more importable contacts are shown
    And none of them are currently selected
    Then the control should read "Select All"

  Scenario: Control reads "Select All" when only some contacts are selected
    Given multiple importable contacts are shown
    And at least one, but not all, are currently selected
    Then the control should read "Select All"

  Scenario: Tapping "Select All" selects every currently visible contact
    Given multiple importable contacts are shown
    And zero or more, but not all, are currently selected
    When the user taps the "Select All" control
    Then every contact currently shown in the list should become selected
    And the Import button's count should match the total number of visible contacts

  Scenario: Control reads "Select None" once every visible contact is selected
    Given multiple importable contacts are shown
    When the user taps the "Select All" control
    Then the control should read "Select None"

  Scenario: Tapping "Select None" clears every selection
    Given every currently visible contact is selected
    And the control reads "Select None"
    When the user taps the "Select None" control
    Then no contact should remain selected
    And the Import button's count should read 0
    And the control should read "Select All" again

  Scenario: Deselecting a single contact after Select All reverts the control to "Select All"
    Given every currently visible contact is selected
    And the control reads "Select None"
    When the user deselects one individual contact via its own checkbox
    Then the control should read "Select All" again
    And every other contact should remain selected

  Scenario: Select all only affects contacts currently in the list, not previously imported ones
    Given a contact was already imported and linked in an earlier session
    And it does not appear in the current picker list
    When the user taps "Select All"
    Then only the contacts currently shown in the list become selected
    And no already-linked contact is affected
