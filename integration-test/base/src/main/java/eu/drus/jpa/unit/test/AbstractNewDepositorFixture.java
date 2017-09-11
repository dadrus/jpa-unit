package eu.drus.jpa.unit.test;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;

public abstract class AbstractNewDepositorFixture extends AbstractConcordionFixture {

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
