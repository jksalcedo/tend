Feature: Multiple phone numbers and emails
  As a Tend user
  I want Tend to store every phone number and email a contact has, not just one
  So that nothing is lost when a contact is imported, synced, or promoted to a native device contact

  Background:
    Given a Tend person "Priya" exists

  Scenario: Importing a native contact with multiple phone numbers keeps all of them
    Given "Priya"'s native contact has a Mobile number and a Work number, with Mobile marked as the device's default
    When "Priya" is imported into Tend
    Then Tend should store both phone numbers, each with its type
    And the Mobile number should be marked as primary in Tend

  Scenario: Foreground refresh reflects a phone number added in the native Contacts app
    Given "Priya" is linked and currently has one phone number in Tend
    When a second phone number is added to "Priya"'s native contact outside of Tend
    And the app is brought to the foreground
    Then Tend should now show both phone numbers

  Scenario: Foreground refresh reflects a phone number removed in the native Contacts app
    Given "Priya" is linked and has two phone numbers in Tend
    When one of them is removed from "Priya"'s native contact outside of Tend
    And the app is brought to the foreground
    Then Tend should now show only the remaining phone number

  Scenario: All phone numbers and emails are read-only for a linked (Case 1) contact
    Given "Priya" is linked to a native device contact
    When the user views Priya's edit screen
    Then every phone number and email field should be displayed but not editable
    And the existing "Edit in Contacts" action is the only way to change them

  Scenario: A Tend-only (Case 2) contact can have multiple phone numbers and emails added directly
    Given "Marco" is a Tend-only contact
    When the user adds a second phone number and marks it as Work
    Then Marco should have two phone numbers, each with its own type

  Scenario: Exactly one phone number and one email can be marked primary at a time
    Given "Marco" has two phone numbers, neither marked primary
    When the user marks one as primary
    Then the other should automatically become not-primary
    And Marco should never have more than one primary phone number at once

  Scenario: Syncing a Tend-only contact to device creates all of its phone numbers and emails
    Given "Marco" has two phone numbers and one email, all entered directly in Tend
    When the user chooses "Sync to Device" for Marco
    Then a new native contact should be created with all three as separate data rows, correctly typed
    And the one marked primary in Tend should be marked as the device's default

  Scenario: Person detail screen shows every phone number and email, not just one
    Given "Priya" has two phone numbers and one email
    When the user views Priya's person detail screen
    Then all three should appear under "Ways to Connect," each labeled with its type

  Scenario: QR sharing includes only the primary phone number and email
    Given "Marco" has two phone numbers and two emails
    When the user shares Marco via QR code
    Then only the primary phone number and primary email should be included in the shared data
    And the recipient importing that QR code gets a person with just those two contact methods

  Scenario: A contact with no primary set falls back to the first entry
    Given "Marco" has two phone numbers, neither explicitly marked primary
    When the user shares Marco via QR code, or syncs Marco to device
    Then the first phone number in the list should be treated as the effective default
