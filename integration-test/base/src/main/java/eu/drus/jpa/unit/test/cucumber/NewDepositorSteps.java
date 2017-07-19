package eu.drus.jpa.unit.test.cucumber;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.junit.Rule;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitRule;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

// By default cucumber scenarios are executed with Cleanup phase=NONE.
public class NewDepositorSteps {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    private EntityManager manager;

    private Depositor depositor;

    @Given("^a new customer '(.+)', applying for a giro account$")
    public void createNewCustomer(final String customerName) throws OperationNotSupportedException {
        final String[] nameParts = customerName.split(" ");
        depositor = new Depositor(nameParts[0], nameParts[1]);
        final GiroAccount giroAccount = new GiroAccount(depositor);
        giroAccount.setCreditLimit(1000.0f);
    }

    @When("^the onboarding process completes")
    public void finalizeOnboarding() {
        manager.persist(depositor);
    }

    @Then("(\\d+) depositor object and (\\d+) giro account object are present")
    @ExpectedDataSets(value = "datasets/max-payne-after-onboarding.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    })
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyExistenceOfExpectedObjects(final int expectedDepositors, final int expectedAccounts) {
        // The check is done via @ExpectedDataSets annotation
    }
}
