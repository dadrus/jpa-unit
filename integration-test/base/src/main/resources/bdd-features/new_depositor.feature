Feature: Create new depositor
  During the on-boarding of a new banking customer for a giro account a new depositor as well as a new giro account
  have to be created. 
 
  Scenario: Onboard a new customer
    Given a new customer 'Max Payne', applying for a giro account
    When the onboarding process completes
    Then 1 depositor object and 1 giro account object are present in the system.
    
  Scenario: Onboard a new customer 2
    Given a new customer 'Max Payne', applying for a giro account
    When the onboarding process completes
    Then 1 depositor object and 1 giro account object are present in the system.