package eu.drus.jpa.unit.test.cucumber;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;

import org.junit.Rule;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRule;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

// Cleanup phase=NONE because we do not want the DB to be erased after each step.
@Cleanup(phase = CleanupPhase.NONE)
public class NewAccountForExistingDepositorSteps {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    // EXTENDED because we do not want the EntityManager to be closed after each step
    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    private EntityManager manager;

    private Depositor depositor;

    @Given("^an existing customer '(.+)' with (\\d+) account$")
    @InitialDataSets("datasets/max-payne-data.json")
    public void seedDatabase(final String customerName, final int numberOfAccounts) {
        // the following is just to verify, that the initial data is indeed present

        final String[] nameParts = customerName.split(" ");
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='" + nameParts[0] + "'",
                Depositor.class);
        depositor = query.getSingleResult();

        assertThat(depositor.getAccounts().size(), equalTo(numberOfAccounts));
    }

    @When("^the customer applies for a new Giro account$")
    public void addNewGiroAccount() throws OperationNotSupportedException {
        new GiroAccount(depositor);

        // we've obtained this object in a previous transaction - so it is in a detached state.
        manager.merge(depositor);
    }

    @Then("the customer '(.+)' will have (\\d+) accounts$")
    public void verifyAmountOfAccountFound(final String customerName, final int numberOfAccounts) {
        final String[] nameParts = customerName.split(" ");

        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='" + nameParts[0] + "'",
                Depositor.class);
        depositor = query.getSingleResult();

        assertThat(depositor.getAccounts().size(), equalTo(numberOfAccounts));
    }

    @Then("account (\\d+) is a new Giro account$")
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyAccountAtPosition(final int accountPosition) {
        final Account[] accounts = depositor.getAccounts().toArray(new Account[depositor.getAccounts().size()]);
        final Account account = accounts[accountPosition - 1];
        assertThat(account, instanceOf(GiroAccount.class));
    }
}
