package eu.drus.jpa.unit.fixtures;

import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;

@RunWith(JpaUnitConcordionRunner.class)
public class NewDepositorFixture extends AbstractConcordionFixture {

    public Depositor createNewCustomer(final String customerName) {
        final String[] nameParts = customerName.split(" ");
        final Depositor depositor = new Depositor(nameParts[0], nameParts[1]);
        new InstantAccessAccount(depositor);
        return depositor;
    }

    public void finalizeOnboarding(final Depositor depositor) {
        manager.persist(depositor);
    }

    @ExpectedDataSets(value = "datasets/max-payne-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION", "accounts"
    })
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyExistenceOfExpectedObjects() {
        // The check is done via @ExpectedDataSets annotation
    }
}
