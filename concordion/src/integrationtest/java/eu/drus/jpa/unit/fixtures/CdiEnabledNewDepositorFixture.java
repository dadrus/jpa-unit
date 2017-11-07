package eu.drus.jpa.unit.fixtures;

import javax.inject.Inject;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.internal.ClassNameAndTypeBasedSpecificationLocator;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.DepositorRepository;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;

@RunWith(JpaUnitConcordionRunner.class)
public class CdiEnabledNewDepositorFixture extends AbstractConcordionFixture {

    @Extension
    private ConcordionExtension ext = (final ConcordionExtender concordionExtender) -> {
        concordionExtender.withSpecificationLocator(new ClassNameAndTypeBasedSpecificationLocator() {
            @Override
            public Resource locateSpecification(final Object fixtureObject, final String typeSuffix) {
                String slashedClassName = fixtureObject.getClass().getName().replaceAll("\\.", "/");
                slashedClassName = slashedClassName.substring(0, slashedClassName.indexOf("$$"));
                String fixturePath = slashedClassName.replace(CdiEnabledNewDepositorFixture.class.getSimpleName(),
                        NewDepositorFixture.class.getSimpleName());
                fixturePath = fixturePath.replaceAll("(Fixture|Test)$", "");
                return new Resource("/" + fixturePath + "." + typeSuffix);
            }
        });
    };

    private static CdiContainer cdiContainer;

    @BeforeSuite
    public static void startContainer() {
        cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
    }

    @AfterSuite
    public static void stopContainer() {
        cdiContainer.shutdown();
    }

    @Inject
    private DepositorRepository repository;

    public Depositor createNewCustomer(final String customerName) {
        final String[] nameParts = customerName.split(" ");
        final Depositor depositor = new Depositor(nameParts[0], nameParts[1]);
        new InstantAccessAccount(depositor);
        return depositor;
    }

    public void finalizeOnboarding(final Depositor depositor) {
        repository.save(depositor);
    }

    @ExpectedDataSets(value = "datasets/max-payne-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION", "accounts"
    })
    @Cleanup(phase = CleanupPhase.AFTER)
    public void verifyExistenceOfExpectedObjects() {
        // The check is done via @ExpectedDataSets annotation
    }
}
