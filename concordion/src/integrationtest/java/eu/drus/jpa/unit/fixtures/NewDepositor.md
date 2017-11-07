# Create New Depositor

During the on-boarding of a new banking customer for an instant access account a new depositor as well as a new instant access account
have to be created. 
 

### [Example](- "Onboard a new customer")

Given a new customer *[Max Payne](- "#customer = createNewCustomer(#TEXT)")*, applying for an instant access account

When the [onboarding process completes](- "finalizeOnboarding(#customer)")

Then [a new depositor object and a new instant access account object are present in the system](- "verifyExistenceOfExpectedObjects()").