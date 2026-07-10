Feature: Device-linked contact (Case 1)
  As a Tend user with a contact that exists in both my device contacts and Tend
  I want Tend to keep that contact's identity details in sync from my device contacts
  So that I never have to update the same information in two places

  Background:
    Given a Tend person "Priya" is linked to a native device contact

  Scenario: Linked contact shows a device-managed indicator
    When the user views "Priya"'s person detail screen
    Then an indicator should be shown stating the contact is managed by the device Contacts app

  Scenario: Identity fields are read-only for a linked contact
    When the user views "Priya"'s person detail screen
    Then the name, phone number, email, and photo fields should be displayed but not editable

  Scenario: Relationship fields remain fully editable for a linked contact
    When the user views "Priya"'s person detail screen
    Then the check-in frequency, notes, events, and social links should be editable as normal

  Scenario: Read-only identity fields offer a way to edit them in the native Contacts app
    When the user views "Priya"'s person detail screen
    Then an "Edit in Contacts" action should be shown near the read-only identity fields
    When the user chooses "Edit in Contacts"
    Then the device's native Contacts app should open directly to "Priya"'s contact edit screen

  Scenario: Edit in Contacts remains available while sync is paused
    Given "Priya" is showing the "sync paused, contacts permission needed" indicator
    When the user views "Priya"'s person detail screen
    Then the "Edit in Contacts" action should still be shown and usable

  Scenario: Edit in Contacts is not offered once the device link is broken
    Given "Priya" is flagged as having a broken device link
    When the user views "Priya"'s person detail screen
    Then the "Edit in Contacts" action should not be shown
    And only the Unlink and Delete actions should be offered for resolving the broken link

  Scenario: Editing in the native Contacts app is picked up on return
    Given the user opened "Priya"'s native contact via "Edit in Contacts" and changed the phone number
    When the user returns to Tend and it is brought to the foreground
    Then Tend should refresh "Priya"'s cached phone number to match the native contact

  Scenario: Foreground refresh picks up a name change made in the device Contacts app
    Given "Priya"'s native device contact's name was changed to "Priya Sharma" outside of Tend
    When the app is brought to the foreground
    Then Tend should refresh "Priya"'s cached name to "Priya Sharma"

  Scenario: Foreground refresh does not touch Tend-owned fields
    Given "Priya" has existing notes, events, and a custom check-in frequency in Tend
    And "Priya"'s native device contact was changed outside of Tend
    When the app is brought to the foreground and the refresh completes
    Then "Priya"'s notes, events, and check-in frequency in Tend should be unchanged

  Scenario: Foreground refresh with contacts permission no longer granted
    Given the READ_CONTACTS permission has since been revoked
    When the app is brought to the foreground
    Then the refresh for linked contacts should be skipped without error
    And previously cached name, phone number, and email should continue to be displayed
    And "Priya"'s person detail screen should show a "sync paused, contacts permission needed" indicator instead of the device-managed indicator
    And that indicator should offer an action to re-grant the READ_CONTACTS permission

  Scenario: Contacts permission is re-granted after being revoked
    Given "Priya" is showing the "sync paused, contacts permission needed" indicator
    When the user re-grants the READ_CONTACTS permission
    And the app is next brought to the foreground
    Then the refresh for linked contacts should resume as normal
    And "Priya"'s person detail screen should show the device-managed indicator again

  Scenario: Photo is cached locally so it survives a permission-paused state
    Given "Priya"'s native contact has a photo
    When Tend successfully refreshes "Priya" on a foreground poll
    Then a local copy of the photo should be stored in app-private storage, not just a reference to the native photo URI
    And when the READ_CONTACTS permission is later revoked
    Then "Priya"'s locally cached photo should continue to display normally, unlike a live URI reference which would become unreadable

  Scenario: No local photo was ever cached before permission was revoked
    Given "Priya" was linked but no successful foreground refresh has completed yet
    And the READ_CONTACTS permission is then revoked
    When the user views "Priya"'s person detail screen
    Then a placeholder avatar should be shown instead of a broken image

  Scenario: Foreground sync continues for an archived device-linked contact
    Given "Priya" has been archived in Tend
    When the app is brought to the foreground
    Then "Priya"'s linked native contact should still be refreshed as normal
    And the "Edit in Contacts" action should still be available if the user views her archived person detail screen

  Scenario: Linked native contact is deleted from the device
    Given "Priya"'s linked native device contact no longer exists on the device
    When the app is brought to the foreground and the refresh runs
    Then "Priya" should be flagged as having a broken device link
    And a warning should be shown on "Priya"'s person detail screen
    And "Priya"'s identity fields should remain read-only and unchanged until the user takes action

  Scenario: Native contact is merged or reorganized by the device's own aggregation, not deleted
    Given "Priya"'s linked native device contact was merged with another contact by the device
    And the native contact is still resolvable by its lookup key under a new contact id
    When the app is brought to the foreground and the refresh runs
    Then "Priya" should not be flagged as having a broken device link
    And "Priya"'s cached identity fields should be refreshed from the reorganized native contact
    And "Priya"'s stored link should be updated to the native contact's new id

  Scenario: Two Tend people become linked to the same native contact
    Given a second Tend person "Priya Sharma" is separately linked to a native device contact
    And the device's own aggregation merges "Priya"'s and "Priya Sharma"'s native contacts into one
    When the app is brought to the foreground and the refresh runs for both people
    Then "Priya" should be flagged as a duplicate of "Priya Sharma"
    And "Priya Sharma" should be flagged as a duplicate of "Priya"
    And a duplicate indicator should be shown on both people's person detail screens
    And the duplicate indicator should link to the other person's detail screen
    And no automatic merge or deletion should occur

  Scenario: Duplicate flag clears once the user resolves it manually
    Given "Priya" and "Priya Sharma" are flagged as duplicates of each other
    When the user unlinks or deletes one of them
    And the app is next brought to the foreground
    Then the remaining person should no longer be flagged as a duplicate

  Scenario: Resolving a broken link by unlinking
    Given "Priya" is flagged as having a broken device link
    When the user chooses to unlink "Priya" from the device contact
    Then "Priya" should keep her last-known name, phone number, email, and photo
    And those identity fields should become editable
    And "Priya" should no longer show the device-managed indicator
    And "Priya" should show the "not synced to your device contacts" indicator instead
    And a "Sync to Device" action should become available for "Priya"

  Scenario: Resolving a broken link by deleting the person
    Given "Priya" is flagged as having a broken device link
    When the user chooses to delete "Priya"
    Then "Priya"'s Tend person record should be permanently removed

  Scenario: Deleting a normally-linked (not broken) device-linked contact never touches the native contact
    Given "Priya" is linked to a native device contact and the link is not broken
    When the user deletes "Priya" from Tend
    Then "Priya"'s Tend person record should be permanently removed
    And "Priya"'s native device contact should remain completely untouched
    And no data should be deleted from the device's Contacts Provider as a result of this action
