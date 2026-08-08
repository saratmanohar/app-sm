    # Example: Login.feature
    Feature: User Login Functionality

      Scenario: Successful Login with Valid Credentials
        Given the user is on the login page
        When the user enters username "standard_user" and password "secret_sauce"
        And clicks the login button
        Then the user should be redirected to the homepage