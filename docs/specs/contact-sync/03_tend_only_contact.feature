Feature: Tend-only contact (Case 2)
  As a Tend user with a contact that only exists in Tend
  I want a clear indication that it's not linked to my device contacts
  And the option to sync it to my device contacts when I choose to
  So that Tend can manage the contact entirely on its own until I decide otherwise

  Background:
    Given a Tend person "Marco" was created directly in Tend
    And "Marco" is not linked to any native device contact

  Scenario: Contact created via Add Person always starts unlinked
    When the user creates a new person through the "Add Person" flow
    Then the new person should not be linked to any native device contact

  Scenario: Unlinked contact shows a not-synced indicator
    When the user views "Marco"'s person detail screen
    Then an indicator should be shown stating the contact is not synced to the device's contacts

  Scenario: All fields are editable for an unlinked contact
    When the user views "Marco"'s person detail screen
    Then the name, phone number, email, photo, and all relationship fields should be editable

  Scenario: Sync to Device is offered for an unlinked contact
    When the user views "Marco"'s person detail screen
    Then a "Sync to Device" action should be available

  Scenario: Syncing to device creates a brand-new native contact
    Given the WRITE_CONTACTS permission has been granted
    When the user chooses "Sync to Device" for "Marco"
    Then a new native device contact should be created with "Marco"'s name, phone number, email, and photo
    And no existing native contact should be searched for or matched against
    And "Marco" should be linked to the newly created native device contact

  Scenario: After syncing to device, the contact behaves as device-linked
    Given "Marco" was just synced to a newly created native device contact
    When the user views "Marco"'s person detail screen
    Then the device-managed indicator should be shown instead of the not-synced indicator
    And the name, phone number, email, and photo fields should become read-only

  Scenario: Syncing to device without contacts permission
    Given the WRITE_CONTACTS permission has not been granted
    When the user chooses "Sync to Device" for "Marco"
    Then the WRITE_CONTACTS permission should be requested
    And if denied, "Marco" should remain unlinked and fully editable
    And a brief explanatory message should be shown

  Scenario: Syncing to device when contacts permission was permanently denied
    Given the WRITE_CONTACTS permission was previously denied with "don't ask again"
    When the user chooses "Sync to Device" for "Marco"
    Then no system permission dialog should be shown
    And a message should be shown explaining that contacts access must be enabled from system settings
    And the message should offer an action that opens the app's system settings page
    And "Marco" should remain unlinked and fully editable

  Scenario: Unlinked contacts never write into device contacts unless explicitly synced
    Given "Marco" has notes, events, and a check-in frequency defined in Tend
    When the app performs its foreground device-contact refresh
    Then no data for "Marco" should be written to or read from the device contacts
