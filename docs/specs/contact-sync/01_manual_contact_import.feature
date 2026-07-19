Feature: Manual contact import
  As a Tend user at any point in the app's lifetime
  I want an always-available way to selectively import contacts from my device
  So that I'm never limited to the one-time first-run prompt

  Background:
    Given the Import feature is accessible from the app's menu

  Scenario: Import feature is available with no contacts yet
    Given Tend has no people saved
    Then the Import feature should be visible and enabled

  Scenario: Import feature is available with existing contacts
    Given Tend has one or more people saved
    Then the Import feature should be visible and enabled

  Scenario: Import feature is available regardless of the first-run prompt's outcome
    Given the "contact import prompt" was resolved with either "Yes" or "No"
    Then the Import feature should be visible and enabled

  Scenario: Opening Import requests permission if not already granted
    Given the READ_CONTACTS permission has not been granted
    When the user opens the Import feature
    Then the READ_CONTACTS permission should be requested
    And if granted, the selective contact import picker should be shown
    And if denied, a brief explanatory message should be shown and no picker opened

  Scenario: Opening Import when contacts permission was permanently denied
    Given the READ_CONTACTS permission was previously denied with "don't ask again"
    When the user opens the Import feature
    Then no system permission dialog should be shown
    And a message should be shown explaining that contacts access must be enabled from system settings
    And the message should offer an action that opens the app's system settings page

  Scenario: Opening Import with permission already granted goes straight to the picker
    Given the contacts permission has already been granted
    When the user opens the Import feature
    Then the selective contact import picker should be shown immediately

  Scenario: Picker excludes contacts already linked to a Tend person
    Given a native device contact "Alex" is already linked to an existing Tend person
    And a native device contact "Sam" is not linked to any Tend person
    When the user opens the selective contact import picker
    Then "Sam" should appear in the picker
    And "Alex" should not appear in the picker

  Scenario: Importing selected contacts creates linked Tend people
    Given the selective contact import picker is open
    When the user selects one or more unlinked native contacts and confirms
    Then a new Tend person should be created for each selected contact
    And each created person's name, phone number, email, and photo should be populated from the native contact
    And each created person should be linked to its native device contact
    And each created person's relationship fields (frequency, notes, events, social links) should start at their defaults

  Scenario: Confirming the picker with no selections makes no changes
    Given the selective contact import picker is open
    When the user confirms without selecting any contacts
    Then no Tend people should be created or modified

  Scenario: Canceling the picker makes no changes
    Given the selective contact import picker is open
    When the user cancels or navigates away from the picker
    Then no Tend people should be created or modified

  Scenario: A contact skipped in one import session can still be imported later
    Given a native device contact "Jordan" is shown in the picker but not selected
    And the user confirms the picker without selecting "Jordan"
    When the user opens the Import feature again at a later time
    Then "Jordan" should still appear in the picker as an importable contact
