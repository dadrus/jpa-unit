Feature: Create new account for existing depositor
  If an existing customer would like to have a new account this is possible
 
  Scenario: Add a new account to an existing customer
    Given an existing customer 'Max Payne' with 1 instant access account
    When the customer applies for a new Giro account
    Then the customer 'Max Payne' will have 2 accounts
      And one account is a new Giro account
