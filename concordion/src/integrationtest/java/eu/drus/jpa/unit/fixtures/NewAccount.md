# Create new account for existing depositor
  
If an existing customer would like to have a new account this is possible
 
## [Examle](- "Add a new account to an existing customer")

If [Max Payne](- "#customer = findCustomer(#TEXT)"), who is an existing customer with 1 instant access account, applies 
for a new [giro account](- "#customer = applyForNewAccount(#TEXT, #customer)"), then he will have [2](- "?=getNumberOfAccounts(#customer)") 
accounts and one account is the new [giro account](- "c:assert-true=hasAccountOfType(#customer, #TEXT)").