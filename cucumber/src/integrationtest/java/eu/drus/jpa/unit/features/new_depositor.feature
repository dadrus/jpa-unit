Feature: Create new depositor
  During the on-boarding of a new banking customer for an instant access account a new depositor as well as a new instant access account
  have to be created. 
 
  Scenario: Onboard a new customer
    Given a new customer 'Max Payne', applying for an instant access account
    When the onboarding process completes
    Then 1 depositor object and 1 instant access account object are present in the system.
