Feature: First-run contact import prompt
  As a new Tend user with no contacts added yet
  I want to be asked, exactly once, whether I'd like to import contacts from my device
  So that I can quickly populate Tend without being nagged on every launch

  Background:
    Given the "contact import prompt" has never been resolved on this device
    And Tend has no people saved

  Scenario: Prompt is shown on first launch with no contacts
    When the user opens the app
    Then the user should be asked whether they would like to import contacts from their device

  Scenario: User accepts and grants permission, then imports a selection of contacts
    Given the user opens the app and sees the contact import prompt
    When the user selects "Yes"
    And the user grants the READ_CONTACTS permission
    Then a selective contact import picker should be shown
    And the picker should list native device contacts not already linked to a Tend person
    When the user selects one or more contacts and confirms
    Then a new Tend person should be created for each selected contact
    And each created person should be linked to its native device contact
    And each created person should be treated as a device-linked contact
    And the "contact import prompt" should be marked as resolved
    And the prompt should not be shown again on subsequent launches

  Scenario: User accepts, grants permission, but confirms zero selections
    Given the user opens the app and sees the contact import prompt
    When the user selects "Yes"
    And the user grants the READ_CONTACTS permission
    And the user confirms the picker without selecting any contacts
    Then no Tend people should be created
    And the "contact import prompt" should be marked as resolved
    And the home screen should show its normal empty state

  Scenario: User accepts but denies the contacts permission
    Given the user opens the app and sees the contact import prompt
    When the user selects "Yes"
    And the user denies the READ_CONTACTS permission
    Then no contact picker should be shown
    And a brief explanatory message should be shown
    And no Tend people should be created
    And the "contact import prompt" should be marked as resolved
    And the home screen should show its normal empty state

  Scenario: User accepts but contacts permission was already permanently denied
    Given the READ_CONTACTS permission was previously denied with "don't ask again"
    And the user opens the app and sees the contact import prompt
    When the user selects "Yes"
    Then no system permission dialog should be shown
    And a message should be shown explaining that contacts access must be enabled from system settings
    And the message should offer an action that opens the app's system settings page
    And the "contact import prompt" should be marked as resolved
    And the home screen should show its normal empty state

  Scenario: User declines the prompt
    Given the user opens the app and sees the contact import prompt
    When the user selects "No"
    Then no contact picker should be shown
    And no Tend people should be created
    And the "contact import prompt" should be marked as resolved
    And the home screen should show its normal empty state

  Scenario: User dismisses the prompt without choosing Yes or No
    Given the user opens the app and sees the contact import prompt
    When the user dismisses the prompt by tapping outside it or pressing back
    Then the dismissal should be treated the same as selecting "No"
    And the "contact import prompt" should be marked as resolved

  Scenario: Prompt is never shown again once resolved, regardless of later contact count
    Given the "contact import prompt" has already been resolved on this device
    And Tend has no people saved
    When the user opens the app
    Then the user should not be asked whether they would like to import contacts

  Scenario: Prompt is not shown if Tend already has at least one person
    Given the "contact import prompt" has never been resolved on this device
    And Tend has at least one person saved
    When the user opens the app
    Then the user should not be asked whether they would like to import contacts
    And the "contact import prompt" should be marked as resolved without being shown

  Scenario: Declining the prompt does not block the Import feature
    Given the user has previously selected "No" on the contact import prompt
    When the user opens the Import feature from elsewhere in the app
    Then the selective contact import picker should be shown as normal
