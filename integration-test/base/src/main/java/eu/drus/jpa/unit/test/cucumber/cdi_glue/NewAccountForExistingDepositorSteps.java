package eu.drus.jpa.unit.test.cucumber.cdi_glue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.DepositorRepository;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

// By default cucumber scenarios are executed with Cleanup phase=NONE.
public class NewAccountForExistingDepositorSteps {

    // EXTENDED because we do not want the EntityManager to be closed after each step
    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    private EntityManager manager;

    @Inject
    private DepositorRepository repo;

    private Depositor depositor;

    @Given("^an existing customer '(.+)' with (\\d+) instant access account$")
    @InitialDataSets("datasets/max-payne-data.json")
    public void seedDatabase(final String customerName, final int numberOfAccounts) {
        // the following is just to verify, that the initial data is indeed present

        final String[] nameParts = customerName.split(" ");

        final List<Depositor> depositors = repo.findByName(nameParts[0]);
        depositor = depositors.get(0);

        assertThat(depositor.getAccounts().size(), equalTo(numberOfAccounts));
    }

    @When("^the customer applies for a new Giro account$")
    public void addNewGiroAccount() throws OperationNotSupportedException {
        new GiroAccount(depositor);

        // we've obtained this object in a previous transaction - so it is in a detached state.
        repo.save(depositor);
    }

    @Then("the customer '(.+)' will have (\\d+) accounts$")
    public void verifyAmountOfAccountFound(final String customerName, final int numberOfAccounts) {
        final String[] nameParts = customerName.split(" ");

        depositor = repo.findByName(nameParts[0]).get(0);

        assertThat(depositor.getAccounts().size(), equalTo(numberOfAccounts));
    }

    @Then("one account is a new Giro account$")
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyAccountAtPosition() {
        final Optional<Account> giroAccount = depositor.getAccounts().stream().filter(a -> a instanceof GiroAccount).findFirst();
        assertThat(giroAccount.isPresent(), equalTo(Boolean.TRUE));
    }
}
