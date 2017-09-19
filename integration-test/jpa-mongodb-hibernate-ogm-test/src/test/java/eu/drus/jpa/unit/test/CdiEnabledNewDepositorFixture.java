package eu.drus.jpa.unit.test;

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

import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;
import eu.drus.jpa.unit.suite.MongoSuite;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitConcordionRunner.class)
public class CdiEnabledNewDepositorFixture extends AbstractCdiEnabledNewDepositorFixture {

    @Extension
    private ConcordionExtension ext = new ConcordionExtension() {

        @Override
        public void addTo(final ConcordionExtender concordionExtender) {
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
        }
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

    @BeforeSuite
    public static void startMongod() {
        if (!MongoSuite.isActive()) {
            MongodManager.start(MongodConfiguration.builder().addHost("localhost", 27017).build());
        }
    }

    @AfterSuite
    public static void stopMongod() throws InterruptedException {
        if (!MongoSuite.isActive()) {
            MongodManager.stop();
        }
    }

}
