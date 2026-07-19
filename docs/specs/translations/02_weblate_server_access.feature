Feature: Weblate server access
  As a Tend contributor or translator
  I want a documented, individually-credentialed way to reach the project's Weblate instance
  So that I can translate strings without any credential ever being stored in this repository

  Background:
    Given a Weblate server exists at https://weblate.tend.farband.ca
    And it is a proof-of-concept instance, not the project's permanent home for translations

  Scenario: A team member is granted their own credentials
    Given a new translator or contributor needs access
    When the team issues them credentials for the Weblate server
    Then those credentials should be specific to that individual
    And no one else, including the repo maintainers, should need to know another user's password

  Scenario: The repository never contains a credential
    Given the Weblate server URL and access process are documented in this spec folder
    When a contributor looks for how to authenticate
    Then they should find no username, password, or API token committed anywhere in the repo
    And .env.example should show the shape of any locally-stored value (e.g. an API token) without a real value in it

  Scenario: The server's address is treated as a provisional, single-source value
    Given the current server is explicitly not the permanent home for translations
    When the team migrates to a different host in the future
    Then updating the documented URL in this spec folder (and .env.example) should be the only change needed
    And no application code, build script, or CI configuration should have depended on the old URL
    # Nothing in the Android app itself talks to Weblate at runtime — this is a repo/tooling
    # concern only, which is what makes this migration cheap if we keep it that way.

  Scenario: Access granted to automation is minimally scoped
    Given some future integration may need the Weblate server (or this repo) to grant it access
    When that access is provisioned
    Then it should use a dedicated, narrowly-scoped credential (e.g. a deploy key or bot account)
    And not a personal account's token or password
    # Matters more than usual here because the server is explicitly temporary — a scoped,
    # single-purpose credential is trivial to revoke; a personal token is not.
