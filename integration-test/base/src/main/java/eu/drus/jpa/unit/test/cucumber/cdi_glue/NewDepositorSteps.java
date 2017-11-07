package eu.drus.jpa.unit.test.cucumber.cdi_glue;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.DepositorRepository;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

// By default cucumber scenarios are executed with Cleanup phase=NONE.
public class NewDepositorSteps {

    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    private EntityManager manager;

    @Inject
    private DepositorRepository repo;

    private Depositor depositor;

    @Given("^a new customer '(.+)', applying for an instant access account$")
    public void createNewCustomer(final String customerName) throws OperationNotSupportedException {
        final String[] nameParts = customerName.split(" ");
        depositor = new Depositor(nameParts[0], nameParts[1]);
        new InstantAccessAccount(depositor);
    }

    @When("^the onboarding process completes")
    public void finalizeOnboarding() {
        repo.save(depositor);
    }

    @Then("(\\d+) depositor object and (\\d+) instant access account object are present")
    @ExpectedDataSets(value = "datasets/max-payne-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION", "accounts"
    })
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyExistenceOfExpectedObjects(final int expectedDepositors, final int expectedAccounts) {
        // The check is done via @ExpectedDataSets annotation
    }
}
